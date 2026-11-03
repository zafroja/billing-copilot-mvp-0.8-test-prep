#!/usr/bin/env python3
"""Prepare a private-pilot .env without printing credentials or storing plaintext password."""
import base64
import getpass
import os
import pathlib
import re
import shutil
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
OUT = ROOT / '.env'


def secure_input(prompt):
    s = getpass.getpass(prompt)
    if not s.strip():
        raise ValueError(f'{prompt.strip()} cannot be blank')
    return s


def quote_for_compose(v):
    # Single-quoted Compose .env values are literal: no bcrypt $ interpolation.
    # All accepted secrets are single-line; reject the rare single-quote character.
    if '\n' in v or '\r' in v or "'" in v:
        raise ValueError('Unsupported quote or newline in configuration value')
    return "'"+v+"'"


def main():
    if OUT.exists():
        raise SystemExit('Refusing to overwrite existing .env. Back it up and remove it manually if needed.')
    if not shutil.which('htpasswd'):
        raise SystemExit('htpasswd is required to hash your password. Install apache2-utils on your server.')
    domain = input('Pilot HTTPS domain (e.g. pilot.example.com): ').strip().lower()
    if not re.fullmatch(r'(?=.{4,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,63}', domain):
        raise ValueError('Enter a valid fully qualified domain name')
    username = input('Owner username [owner]: ').strip() or 'owner'
    if not re.fullmatch(r'[A-Za-z0-9_.-]{3,48}',username):
        raise ValueError('Owner username must be 3–48 simple characters')
    password = secure_input('New unique owner password (not your Jobber password): ')
    if len(password) < 16:
        raise ValueError('Use at least 16 characters for the owner password')
    if password != secure_input('Confirm owner password: '):
        raise ValueError('Passwords do not match')
    result = subprocess.run(['htpasswd', '-niB', '-C', '12', username],input=password+'\n',text=True,capture_output=True,check=True)
    hashed = result.stdout.strip().split(':',1)[1]
    del password
    client_id = input('Jobber Client ID (not the secret): ').strip()
    if not client_id or '\n' in client_id:
        raise ValueError('Jobber Client ID is required')
    client_secret = secure_input('Jobber Client Secret (never share in chat): ')
    if ' ' in client_id:
        raise ValueError('Unexpected space in client ID')
    fields={
      'APP_DOMAIN': domain,
      'DATABASE_PASSWORD': base64.urlsafe_b64encode(os.urandom(36)).decode('ascii').rstrip('='),
      'PILOT_USERNAME': username,
      'PILOT_PASSWORD_BCRYPT': hashed,
      'TOKEN_ENCRYPTION_KEY_B64': base64.b64encode(os.urandom(32)).decode('ascii'),
      'JOBBER_CLIENT_ID': client_id,
      'JOBBER_CLIENT_SECRET': client_secret,
      'PILOT_CURRENCY': 'EUR',
      'JOBBER_API_VERSION': '2026-05-12',
    }
    content='\n'.join(f'{k}={quote_for_compose(v)}' for k,v in fields.items())+'\n'
    fd=os.open(str(OUT), os.O_WRONLY|os.O_CREAT|os.O_EXCL, 0o600)
    with os.fdopen(fd,'w',encoding='utf-8') as f:
        f.write(content)
    print('Saved .env with permissions 0600. Keep it in a private secrets manager; do not share or commit it.')
    print('Set the Jobber app callback URL to: https://'+domain+'/api/integrations/jobber/callback')


if __name__=='__main__':
    try: main()
    except (ValueError,subprocess.CalledProcessError) as e:
        sys.exit('Setup failed: '+str(e))
