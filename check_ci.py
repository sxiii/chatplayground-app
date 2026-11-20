import urllib.request, json
try:
    repo = 'sxiii/chatplayground-app'
    
    # Get most recent workflow runs
    url = f'https://api.github.com/repos/{repo}/actions/runs?per_page=5'
    req = urllib.request.Request(url, headers={'User-Agent': 'Hermes'})
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        
        for run in data.get('workflow_runs', []):
            run_id = run['id']
            status = run.get('status')
            conclusion = run.get('conclusion')
            sha = run.get('head_sha', '')[:7]
            created = run.get('created_at')
            print(f"Run {run_id}: status={status}, conclusion={conclusion}, sha={sha}, created={created}")
            
except Exception as e:
    import traceback
    print('Error:', e)
    traceback.print_exc()
