from pathlib import Path
import os,subprocess,sys,urllib.parse,shutil
root=Path(__file__).resolve().parent;jdk=root/'toolchain/jdk-21.0.2';trust=root/'toolchain/truststore'
if not trust.exists():
 shutil.copy(jdk/'lib/security/cacerts',trust)
 if os.environ.get('SSL_CERT_FILE'):subprocess.run([str(jdk/'bin/keytool'),'-importcert','-noprompt','-alias','workspace-ca','-file',os.environ['SSL_CERT_FILE'],'-keystore',str(trust),'-storepass','changeit'],check=True)
env=os.environ.copy();env['GRADLE_USER_HOME']=str(root/'gradle-cache');env['JAVA_HOME']=str(jdk);env['PATH']=str(jdk/'bin')+':'+env['PATH']
opts=['-Djavax.net.ssl.trustStore='+str(trust),'-Djavax.net.ssl.trustStorePassword=changeit'];p=urllib.parse.urlparse(env.get('HTTPS_PROXY',env.get('https_proxy','')))
if p.hostname:
 for scheme in ['http','https']:opts+=['-D'+scheme+'.proxyHost='+p.hostname,'-D'+scheme+'.proxyPort='+str(p.port or 80)]
agent=root/'toolchain/nfr-path-agent.jar';env['JAVA_TOOL_OPTIONS']=' '.join(opts+(['-javaagent:'+str(agent)] if agent.exists() else []))
sys.exit(subprocess.call([str(root/'toolchain/gradle-8.10.2/bin/gradle'),'--console=plain','--no-daemon','--max-workers=2','--no-watch-fs']+opts+sys.argv[1:],cwd=root/'MareEletrica',env=env))
