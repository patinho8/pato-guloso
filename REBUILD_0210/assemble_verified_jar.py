"""Reconstruct the release from the exact production classes already tested.

Does not compile source. A source change requires a fresh Gradle build and tests.
Run after restoring a checkpoint when the build cache is no longer available.
"""
from pathlib import Path
import hashlib
import json
import zipfile

project = Path(__file__).resolve().parents[1]
evidence = project / 'tests/evidence_0.21.0'
expected = json.loads((evidence / 'tested-production-classes.json').read_text())
entries = {}
with zipfile.ZipFile(evidence / 'tested-production-bytecode.zip') as saved:
    assert saved.testzip() is None
    for name in saved.namelist():
        assert name.endswith('.class') and 'ArmoryIntegration' not in name
        data = saved.read(name)
        assert data[:4] == b'\xca\xfe\xba\xbe' and int.from_bytes(data[6:8], 'big') == 65
        entries[name] = data
assert {n: hashlib.sha256(b).hexdigest() for n, b in entries.items()} == expected

resources = project / 'src/main/resources'
for source in sorted(resources.rglob('*')):
    if source.is_file() and '.rsync-tmp' not in source.parts:
        name = source.relative_to(resources).as_posix()
        assert name not in entries
        entries[name] = source.read_bytes()
entries['META-INF/MANIFEST.MF'] = (
    'Manifest-Version: 1.0\r\n'
    'Implementation-Title: Mare Eletrica\r\n'
    'Implementation-Version: 0.21.0\r\n\r\n'
).encode('utf-8')
assert 'version="0.21.0"' in entries['META-INF/neoforge.mods.toml'].decode('utf-8')

output = project / 'build/libs/mare-eletrica-0.21.0.jar'
output.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as jar:
    for name in sorted(entries):
        info = zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        info.external_attr = 0o644 << 16
        jar.writestr(info, entries[name])
with zipfile.ZipFile(output) as jar:
    assert jar.testzip() is None
    assert all(jar.read(name) == data for name, data in entries.items())
print('Verified JAR:', output, len(expected), 'tested classes;', len(entries), 'entries')
