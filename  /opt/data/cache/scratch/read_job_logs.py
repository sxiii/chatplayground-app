import zipfile, sys, os, json
zip_path='job_logs.zip'
if not os.path.exists(zip_path):
    print('missing')
    sys.exit()
with zipfile.ZipFile(zip_path) as z:
    for name in z.namelist():
        data = z.read(name).decode(errors='replace')
        print('---',name,'---')
        print(data[:2000])
