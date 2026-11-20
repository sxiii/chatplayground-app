#!/usr/bin/env python3
import zipfile

with zipfile.ZipFile('/opt/data/chatplayground-app/build_logs.zip', 'r') as z:
    # List all files in the archive
    for name in z.namelist():
        if 'lint' in name.lower():
            print(name)