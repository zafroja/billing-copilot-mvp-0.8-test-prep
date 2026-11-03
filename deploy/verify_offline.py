#!/usr/bin/env python3
"""Honest offline checks; not a substitute for Maven, npm, PostgreSQL or Docker."""
import pathlib
import shutil
import subprocess
import sys
import tempfile
import yaml

root=pathlib.Path(__file__).resolve().parents[1]
checks=[]
def check(name,fn):
    try: fn(); checks.append((name,'PASS'))
    except Exception as e: checks.append((name,'FAIL: '+str(e)))

def java_demo():
    with tempfile.TemporaryDirectory() as d:
        subprocess.run(['javac','--release','17','-d',d,str(root/'standalone-demo/src/BillingCopilotDemo.java')],check=True,capture_output=True,text=True)
        r=subprocess.run(['java','-cp',d,'BillingCopilotDemo','--test'],cwd=root/'standalone-demo',capture_output=True,text=True,check=True)
        assert 'PASS: 8 numeric/structural' in r.stdout

def deployment_yaml():
    d=yaml.safe_load((root/'docker-compose.private-pilot.yml').read_text())
    assert set(d['services'])=={'postgres','backend','frontend','edge'}
    assert not any(d['services'][k].get('ports') for k in ('postgres','backend','frontend'))
    assert len(d['services']['edge']['ports'])==3
    assert 'https://${APP_DOMAIN' in d['services']['backend']['environment']['JOBBER_REDIRECT_URI']
    env=(root/'deploy/.env.example').read_text()
    assert 'REPLACE_WITH' in env
    assert not (root/'.env').exists(),'Secrets file present in checked source: package it separately!'

def snapshot():
    import json
    d=json.loads((root/'backend/src/test/resources/fixtures/jobber-real-sanitized-job1.json').read_text())
    j=d['data']['job']; visits=j['visits']['nodes']; invoices=j['invoices']['nodes']
    assert len(visits)==7 and all(v['isComplete'] for v in visits)
    assert len(invoices)==1 and invoices[0]['invoiceStatus']=='draft'
    work=sum(li['quantity'] for v in visits for li in v['lineItems']['nodes'])
    billed=sum(li['quantity'] for li in invoices[0]['lineItems']['nodes'])
    unit=invoices[0]['lineItems']['nodes'][0]['unitPrice']
    assert (work,billed,unit,(work-billed)*unit)==(7,6,75,75)

def source_safety():
    s=(root/'backend/src/main/java/com/billingcopilot/security/PilotSecurityConfig.java').read_text()
    assert '.anyRequest().hasRole("OWNER")' in s
    assert 'SessionCreationPolicy.STATELESS' in s
    src=(root/'frontend/src/api/client.ts').read_text()
    assert 'window.location.origin' in src
    assert 'localStorage' not in src
    assert 'JOBBER_CLIENT_SECRET' not in src

check('Java real-snapshot tests',java_demo)
check('Real GraphQL snapshot quantity',snapshot)
check('Private HTTPS deployment topology',deployment_yaml)
check('Source access control/secret separation',source_safety)
for name,status in checks: print(status,'-',name)
if any(s!='PASS' for _,s in checks): sys.exit(1)
print('OFFLINE CHECKS PASSED. Full builds and live OAuth still require external dependencies/accounts.')
