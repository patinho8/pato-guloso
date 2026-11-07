from pathlib import Path
import json,zipfile,hashlib,shutil
r=Path(__file__).resolve().parent
if r.name=='REBUILD_0210':p=r.parent;r=p.parent
else:p=r/'MareEletrica'
e=p/'tests/evidence_0.21.0';jar=p/'build/libs/mare-eletrica-0.21.0.jar'
expected=json.loads((e/'tested-production-classes.json').read_text())
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 names=set(z.namelist());assert not any('ArmoryIntegration' in n or 'Integration.class' in n for n in names)
 actual={n:hashlib.sha256(z.read(n)).hexdigest() for n in names if n.endswith('.class')}
 assert actual==expected,{'missing':list(expected.keys()-actual.keys()),'extra':list(actual.keys()-expected.keys()),'changed':[n for n in expected.keys()&actual.keys() if expected[n]!=actual[n]]}
 assert '0.21.0' in z.read('META-INF/neoforge.mods.toml').decode()
 for src in (p/'src/main/resources').rglob('*'):
  if src.is_file() and '.rsync-tmp' not in src.parts:assert z.read(str(src.relative_to(p/'src/main/resources')))==src.read_bytes(),src
 for n in ['vanilla-test.txt']:
  log=(e/n).read_text();assert 'PASS visual cycle' in log and 'FAIL' not in log,n
 assert (e/'VALIDACAO_FINAL.md').is_file()
 print('JAR verified:',len(actual),'production classes; resources match; no test fixture',flush=True)
install=p/'INSTALAR'
for old in install.glob('mare-eletrica-*.jar'):old.unlink()
shutil.copy(jar,install/jar.name);sha=hashlib.sha256(jar.read_bytes()).hexdigest();(install/'SHA256.txt').write_text(sha+'  '+jar.name+'\n')
(p/'CODIGO_FONTE.txt').write_text('Código de produção completo em src. O JAR em INSTALAR já está pronto para jogar. Java 21 / NeoForge 1.21.1. Compilar: COMPILAR_WINDOWS.bat ou Gradle 8.10.2 build. Testes e evidências de 0.21.0 em tests/evidence_0.21.0. Gerador de modelos em tools/generate_armory21.py.\n')
(p/'PROGRESSO_0.21.0.md').write_text('Atualização 0.21.0 concluída. JAR final em INSTALAR. Código verificado em cliente real. Fonte, recursos, scripts e evidências incluídos. Detalhes e limites da evidência em tests/evidence_0.21.0/VALIDACAO_FINAL.md.\n')
(e/'release.json').write_text(json.dumps({'version':'0.21.0','minecraft':'1.21.1','tested_neoforge':['21.1.172','21.1.229'],'optional_mod_tested':'Punchy 2.8g enabled; see VALIDACAO_FINAL.md for evidence limitations','assembly':'verified saved production bytecode plus final resources','jar_sha256':sha,'production_class_count':len(expected),'jar_bytes':jar.stat().st_size},indent=2)+'\n')
(e/'README.md').write_text('''# Verificação 0.21.0

Cliente real e servidor integrado de sobrevivência. `vanilla-test.txt`: NeoForge 21.1.172, verificações funcionais nas oito famílias e 84 cenas. Capturas selecionadas incluídas. `VALIDACAO_FINAL.md` distingue os testes completos, as inspeções finais com Punchy e os limites dos registros preservados.

Os testes verificam abastecimento, consumo de cartuchos, incompatibilidade de acessórios/pentes, recarga pendente, cancelamento, recarga parcial, NBT, disparo/cooldown, migração única e cancelamento ao abrir a oficina. Também verificam o tempo real de 42 ticks para recarregar a Desert.

Os 302 arquivos de classe de produção no JAR correspondem, byte por byte, aos executados no teste base; a classe de teste não foi incluída. Recursos nativos validados: 296 modelos, 56 atlas diferentes, 104 receitas distintas e 248 adaptações de acessórios antigos. Ajustes finais de formato das ópticas e enquadramento dos ícones foram inspecionados em jogo; a conclusão do último ciclo rápido não foi preservada.

Não inclui nem exige Punchy. O perfil com esse mod usa a versão de NeoForge exigida por ele. Outros conjuntos de mods e shaders não foram testados.
''')
out=r/'Mare_Eletrica_0.21.0_NeoForge_1.21.1.zip';temp=out.with_suffix('.tmp')
with zipfile.ZipFile(temp,'w',zipfile.ZIP_DEFLATED,6) as z:
 for f in p.rglob('*'):
  if not f.is_file():continue
  rel=f.relative_to(p)
  if any(x in {'build','.gradle','run-armory21','run-camera-test','run','__pycache__','.rsync-tmp'} for x in rel.parts):continue
  if f.suffix=='.png' and 'evidence_0.20.0' in str(rel):continue
  z.write(f,'MareEletrica/'+str(rel))
with zipfile.ZipFile(temp) as z:
 assert z.testzip() is None
 for n in ['INSTALAR/'+jar.name,'tools/build.ps1','COMPILAR_WINDOWS.bat','src/main/java/br/mareeletrica/Magazines.java','NOVIDADES_0.21.0.md']:assert 'MareEletrica/'+n in z.namelist()
 assert hashlib.sha256(z.read('MareEletrica/INSTALAR/'+jar.name)).hexdigest()==sha
temp.replace(out);print('RELEASE_READY',out.resolve(),out.stat().st_size,sha,flush=True)
