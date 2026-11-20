import json
import subprocess
import urllib.request

def get_token():
    try:
        # Use git credential fill to get the token
        p = subprocess.run(
            ['git', 'credential', 'fill'],
            input='protocol=https\nhost=github.com\n\n',
            text=True, capture_output=True, check=True
        )
        for line in p.stdout.strip().splitlines():
            if line.startswith('password='):
                return line.split('=', 1)[1]
    except Exception as e:
        print(f"Error getting token: {e}")
    return None

def main():
    token = get_token()
    if not token:
        print("No token found")
        return

    url = 'https://api.github.com/repos/sxiii/chatplayground-app/actions/runs?branch=main&per_page=1'
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
            if not runs:
                print("No runs found")
                return
            latest = runs[0]
            print(f"Run #{latest['run_number']} | Conclusion: {latest['conclusion']} | Status: {latest['status']} | SHA: {latest['head_sha']}")
    except Exception as e:
        print(f"API Error: {e}")

if __name__ == '__main__':
    main()
