#!/usr/bin/env python3
import urllib.request, json
try:
    repo = 'sxiii/chatplayground-app'
    run_id = '44804905975'
    
    url = f'https://api.github.com/repos/{repo}/actions/runs/{run_id}/jobs'
    req = urllib.request.Request(url, headers={'User-Agent': 'Hermes'})
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        
        for job in data.get('jobs', []):
            print(f"Job: {job['name']} (ID: {job['id']}) - Conclusion: {job['conclusion']}")
            
            for step in job.get('steps', []):
                step_name = step.get('name')
                conclusion = step.get('conclusion')
                if conclusion == 'failure':
                    print(f"  FAILED STEP: {step_name}")
                    # Try to get output
                    if 'log' in step:
                        print(f"  Log URL: {step.get('log')}")
            
except Exception as e:
    import traceback
    print('Error:', e)
    traceback.print_exc()
