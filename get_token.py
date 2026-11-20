import subprocess
result = subprocess.run(
    ['git', 'credential', 'fill'],
    input='protocol=https\nhost=github.com\n',
    capture_output=True, text=True, cwd='/opt/data/chatplayground-app'
)
print('stdout:', result.stdout)
print('stderr:', result.stderr)
for line in result.stdout.splitlines():
    if line.startswith('password='):
        token = line.replace('password=', '').strip()
        print('Token:', token[:20] + '...' if len(token) > 20 else token)