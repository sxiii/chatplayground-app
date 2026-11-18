#!/bin/sh
# Extract git token from credential store and fetch artifact logs
CRED=$(git config credential.helper | sed 's/.*--file=//')
# CRED format: credential.helper=store --file=PATH
TOKEN_FILE=/opt/data/home/.chatplayground-git-credentials
LINE=$(head -n1 "$TOKEN_FILE")
TOKEN=***"${LINE#https://sxiii:}" | sed 's/@github.com.*//')
echo "got token len ${#TOKEN}"
curl -s -H "Authorization: token $TOKEN" -L -o job_logs.zip "https://api.github.com/repos/sxiii/chatplayground-app/actions/jobs/135097351007/logs"
ls -l job_logs.zip
curl -s -H "Authorization: token $TOKEN" -L -o gradle_debug_log.zip "https://api.github.com/repos/sxiii/chatplayground-app/actions/artifacts/14466897466/zip"
ls -l gradle_debug_log.zip
