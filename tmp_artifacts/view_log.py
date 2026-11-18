import zipfile, sys, os; zp='gradle-build-debug-log.zip'; z=zipfile.ZipFile(zp); 
for n in z.namelist():
    if 'gradle_test.log' in n:
        data=z.read(n).decode('utf-8', errors='replace'); print(data[:2000])
