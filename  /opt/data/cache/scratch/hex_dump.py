import sys, os
path='gradle_debug_log.zip'
with open(path,'rb') as f:
    data=f.read(64)
    sys.stdout.buffer.write(data)
