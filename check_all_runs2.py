#!/usr/bin/env python3
import urllib.request, json
try:
    repo = 'sxiii/chatplayground-app'
    
    url = f'https://api.github.com/repos/{repo}/actions/runs?per_page=10'
    req = urllib.request.Request(url, headers={'User-Agent': 'Hermes'})
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        
        for run in data.get('workflow_runs', []):
            run_id = run['id']
            status = run.get('status')
            conclusion = run.get('conclusion')
            sha = run.get('head_sha', '')[:7]
            created = run.get('created_at')
            print(f"Run {run_id}: status={status}, conclusion={conclusion}, sha={sha}")
            
except Exception as e:
    import traceback
    print('Error:', e)
    traceback.print_exc()
