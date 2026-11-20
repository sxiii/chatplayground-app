#!/usr/bin/env python3
import urllib.request, json
try:
    repo = 'sxiii/chatplayground-app'
    
    url = f'https://api.github.com/repos/{repo}/actions/runs/44804905975'
    req = urllib.request.Request(url, headers={'User-Agent': 'Hermes'})
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        print("Run 44804905975:")
        print(f"  status: {data.get('status')}")
        print(f"  conclusion: {data.get('conclusion')}")
        print(f"  updated_at: {data.get('updated_at')}")
            
except Exception as e:
    import traceback
    print('Error:', e)
    traceback.print_exc()
