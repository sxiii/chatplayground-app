import zipfile, sys, os
zip_path='job_logs.zip'
if not os.path.exists(zip_path):
    print('missing')
    sys.exit(1)
with zipfile.ZipFile(zip_path) as z:
    for name in z.namelist():
        print('---', name, '---')
        data = z.read(name).decode(errors='replace')
        print(data[:2000])
