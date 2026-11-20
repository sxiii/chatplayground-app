#!/usr/bin/env python3
import urllib.request, json
import urllib.error
import os

# Get token from environment or git-credential
token = os.environ.get('GITHUB_TOKEN')
if not token:
    with open('/opt/data/home/.chatplayground-git-credentials', 'r') as f:
        line = f.read().strip()
        if 'https://' in line and ':' in line:
            token = line.split('://')[1].split(':')[1].split('@')[0]

if not token:
    print("No token found")
    exit(1)

print(f"Token found: {token[:4]}...")

repo = 'sxiii/chatplayground-app'
run_id = '44804905975'

# Get job details with auth
url = f'https://api.github.com/repos/{repo}/actions/runs/{run_id}/jobs'
req = urllib.request.Request(url, headers={
    'User-Agent': 'Hermes',
    'Authorization': f'token {token}'
})
try:
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
        for job in data.get('jobs', []):
            print(f"Job: {job['name']} - {job['conclusion']}")
except urllib.error.HTTPError as e:
    print(f"HTTP Error: {e.code} - {e.read().decode()}")
except Exception as e:
    print(f"Error: {e}")
