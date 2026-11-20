#!/usr/bin/env python3
import zipfile

with zipfile.ZipFile('/opt/data/chatplayground-app/build_logs.zip', 'r') as z:
    # List all files in the archive
    for name in z.namelist():
        if 'lint' in name.lower():
            print(name)
    
    # Try to read the main log file if it exists
    print("\n--- CONTAINS ---")
    with z.open('build_logs.zip') as f:
        content = f.read(1000000)  # Read up to 1MB
        # Check if it's text
        try:
            text = content.decode('utf-8', errors='replace')
            # Find lint-related lines
            for line in text.split('\n'):
                if 'lint' in line.lower() or 'error' in line.lower() or 'warning' in line.lower():
                    print(line[:500])
        except:
            print("Binary content")