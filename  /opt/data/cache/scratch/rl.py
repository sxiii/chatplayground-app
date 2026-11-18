import shutil, os, zipfile
base = '/opt/data/chatplayground-app/ '
if os.path.isdir(base):
    shutil.rmtree(base)
os.chdir('/opt/data/chatplayground-app')
with zipfile.ZipFile('job_logs.zip') as z:
    for name in z.namelist():
        print('---', name, '---')
        print(z.read(name).decode(errors='replace')[:2000])
