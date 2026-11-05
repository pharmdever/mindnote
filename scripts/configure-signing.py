"""Run locally after gh auth login. Never prints passwords or writes to the repo."""
import base64, pathlib, subprocess, sys
if len(sys.argv)!=3:
    raise SystemExit('Usage: python scripts/configure-signing.py OWNER/REPO /path/to/mindnote-signing')
repo,folder=sys.argv[1],pathlib.Path(sys.argv[2])
values={'ANDROID_KEYSTORE_BASE64':base64.b64encode((folder/'mindnote-release.jks').read_bytes()),'ANDROID_KEYSTORE_PASSWORD':(folder/'password.txt').read_bytes().strip(),'ANDROID_KEY_PASSWORD':(folder/'password.txt').read_bytes().strip(),'ANDROID_KEY_ALIAS':b'mindnote'}
for key,value in values.items():
    subprocess.run(['gh','secret','set',key,'--repo',repo],input=value,check=True)
    print('Configured '+key)
