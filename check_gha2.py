import urllib.request, json, os

# GitHub API check
req = urllib.request.Request(
    'https://api.github.com/repos/sxiii/chatplayground-app/actions/runs?per_page=8',
    headers={'User-Agent': 'Python', 'Accept': 'application/vnd.github+json'}
)
try:
    with urllib.request.urlopen(req, timeout=30) as resp:
        data = json.loads(resp.read().decode())
        runs = data.get('workflow_runs', [])
        print(f"== {len(runs)} recent runs ==")
        for run in runs:
            print(f"{run['id']} | {run['name']} | {run['status']} | {run['conclusion']} | {run['head_branch']} | {run['created_at']}")
            print(f"   {run['html_url']}")
except Exception as e:
    print('Error:', type(e).__name__, e)

# Status page
try:
    req2 = urllib.request.Request('https://www.githubstatus.com/api/v2/status.json',
                                  headers={'User-Agent': 'Python'})
    with urllib.request.urlopen(req2, timeout=15) as resp:
        s = json.loads(resp.read().decode())
        print("\nGitHub status:", s['status']['description'], '|', s['status']['indicator'])
except Exception as e:
    print('Status error:', e)