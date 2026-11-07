from pathlib import Path
import subprocess,os,sys,secrets
root=Path(__file__).resolve().parent;g=root/'graphics';env=os.environ.copy();env['LD_LIBRARY_PATH']=str(g/'usr/lib/x86_64-linux-gnu')+':'+env.get('LD_LIBRARY_PATH','');env['LIBGL_ALWAYS_SOFTWARE']='1';env['ALSOFT_DRIVERS']='null';env['DISPLAY']='127.0.0.1:91';auth=root/'xauthority';env['XAUTHORITY']=str(auth)
subprocess.run([str(g/'usr/bin/xauth'),'-f',str(auth),'add',env['DISPLAY'],'.',secrets.token_hex(16)],env=env,check=True)
with (root/'xvfb.log').open('w') as log:
 x=subprocess.Popen([str(g/'usr/bin/Xvfb'),':91','-screen','0','1280x720x24','-listen','tcp','-auth',str(auth),'-noreset'],env=env,stdout=log,stderr=subprocess.STDOUT)
 try:sys.exit(subprocess.call([sys.executable,str(root/'run_gradle.py'),'runClient','-Pupdate0210Integration']+sys.argv[1:],env=env))
 finally:x.terminate();x.wait()
