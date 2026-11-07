#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
if ! command -v javac >/dev/null || ! javac -version 2>&1 | grep -q '^javac 21\.'; then
    echo 'Instale um JDK 21 (Java 21 para desenvolvimento), curl e unzip.' >&2
    exit 1
fi
mkdir -p .tools
if [ ! -x .tools/gradle-8.10.2/bin/gradle ]; then
    curl --fail --location --output .tools/gradle.zip https://services.gradle.org/distributions/gradle-8.10.2-bin.zip
    curl --fail --location --output .tools/gradle.sha256 https://services.gradle.org/distributions/gradle-8.10.2-bin.zip.sha256
    python3 - <<'PY'
from pathlib import Path
import hashlib
p=Path('.tools/gradle.zip')
assert hashlib.file_digest(p.open('rb'),'sha256').hexdigest() == Path('.tools/gradle.sha256').read_text().strip(), 'Checksum invalido'
PY
    unzip -q .tools/gradle.zip -d .tools
fi
.tools/gradle-8.10.2/bin/gradle --no-daemon --console=plain build 2>&1 | tee COMPILACAO.log
