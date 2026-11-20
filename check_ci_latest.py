import json
import subprocess
import urllib.request

def get_token():
    try:
        p = subprocess.run(
            ['git', 'credential', 'fill'],
            input='protocol=https\nhost=github.com\n\n',
            text=True, capture_output=True, check=True, cwd='/opt/data/chatplayground-app'
        )
        for line in p.stdout.strip().splitlines():
            if line.startswith('password='):
                return line.split('=', 1)[1]
    except Exception as e:
        pass
    return None

def main():
    token = get_token()
    if not token:
        print("No token found")
        return

    # Get latest runs
    url = 'https://api.github.com/repos/sxiii/chatplayground-app/actions/runs?per_page=5'
    req = urllib.request.Request(
        url, 
        headers={
            'Authorization': f'Bearer {token}',
            'Accept': 'application/vnd.github+json',
            'X-GitHub-Api-Version': '2022-11-28',
            'User-Agent': 'Hermes-Agent'
        }
    )
    try:
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode())
            runs = data.get('workflow_runs', [])
            for r in runs:
                print(f"Run #{r.get('run_number', 'N/A')} | Conclusion: {r.get('conclusion', r.get('status'))} | Status: {r.get('status')} | SHA: {r.get('head_sha', 'N/A')[:8]}")
    except Exception as e:
        print(f"API Error: {e}")

if __name__ == '__main__':
    main()