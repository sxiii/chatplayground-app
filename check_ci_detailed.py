import json
import subprocess
import urllib.request

def get_token():
    try:
        # Use git credential fill to get the token
        p = subprocess.run(
            ['git', 'credential', 'fill'],
            input='protocol=https\nhost=github.com\n\n',
            text=True, capture_output=True, check=True, cwd='/opt/data/chatplayground-app'
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

    # Run #136 details
    url = 'https://api.github.com/repos/sxiii/chatplayground-app/actions/runs/136'
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
            run_data = json.loads(response.read().decode())
            print("Run Details:")
            print(json.dumps(run_data, indent=2))
            
            # Get jobs for this run
            jobs_url = run_data['jobs_url']
            req_jobs = urllib.request.Request(
                jobs_url, 
                headers={
                    'Authorization': f'Bearer {token}',
                    'Accept': 'application/vnd.github+json',
                    'X-GitHub-Api-Version': '2022-11-28',
                    'User-Agent': 'Hermes-Agent'
                }
            )
            with urllib.request.urlopen(req_jobs) as response_jobs:
                jobs_data = json.loads(response_jobs.read().decode())
                print("\nJobs:")
                print(json.dumps(jobs_data, indent=2))
    except Exception as e:
        print(f"API Error: {e}")

if __name__ == '__main__':
    main()
