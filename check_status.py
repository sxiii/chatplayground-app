import urllib.request
import json

req = urllib.request.Request(
    'https://api.github.com/repos/sxiii/chatplayground-app/actions/runs?per_page=5',
    headers={'User-Agent': 'Python'}
)
try:
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        for run in data.get('workflow_runs', []):
            print(run['id'], run['name'], run['status'], run['conclusion'], run['html_url'])
except Exception as e:
    print('Error:', e)
