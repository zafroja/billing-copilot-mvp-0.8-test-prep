import React,{useEffect,useMemo,useState}from'react';import{createRoot}from'react-dom/client';import'./style.css';
import Sidebar from './components/Sidebar';import StatCard from './components/StatCard';import JobberCard from './components/JobberCard';
import{authenticateOwner,clearOwnerCredentials,createManualRate,getAuditHistory,getAuditEvidence,getContracts,getJobberStatus,reviewRule,runAudit,runJobberJobAudit,startJobberConnect,uploadContract}from'./api/client';
import type{AuditResult,Contract,JobberConnectionStatus}from'./types';
const work=[{service:'Salt application',quantity:7},{service:'Emergency cleanup',quantity:3},{service:'Monthly maintenance',quantity:1}];
const invoice=[{service:'Salt application',quantity:6,unitPrice:130},{service:'Emergency cleanup',quantity:3,unitPrice:150},{service:'Monthly maintenance',quantity:1,unitPrice:1400}];
let displayCurrency='EUR';
function fmt(value:number|string){return new Intl.NumberFormat('en-IE',{style:'currency',currency:displayCurrency}).format(Number(value))}
function App(){const[owner,setOwner]=useState<{currency:string}|null>(null),[active,setActive]=useState('Dashboard'),[contract,setContract]=useState<Contract|null>(null),[result,setResult]=useState<AuditResult|null>(null),[busy,setBusy]=useState(false),[jobber,setJobber]=useState<JobberConnectionStatus|null>(null),[error,setError]=useState('');
useEffect(()=>{if(owner){getJobberStatus().then(setJobber).catch(e=>setError(String(e.message||e)));getContracts().then((all:Contract[])=>{if(all.length)setContract(all[0])}).catch(e=>setError(String(e.message||e))) }},[owner]);
const approved=contract?.rules.filter(r=>r.reviewStatus==='APPROVED').length||0;const pending=contract?.rules.filter(r=>r.reviewStatus==='PENDING').length||0;const impact=result?.totalPotentialImpact||0;
async function upload(e:any){const f=e.target.files?.[0];if(!f)return;setBusy(true);setError('');try{setContract(await uploadContract(f));setResult(null);setActive('Contracts')}catch(x:any){setError(x.message)}finally{setBusy(false)}}
async function review(id:number,status:string){try{const x=await reviewRule(id,status);setContract(c=>c?({...c,rules:c.rules.map(q=>q.id===id?x:q)}):c)}catch(x:any){setError(x.message)}}
async function run(){if(!contract)return;try{setResult(await runAudit(contract.contractId,work,invoice));setActive('Discrepancies')}catch(x:any){setError(x.message)}}
const content=useMemo(()=>{if(active==='Contracts')return <Contracts contract={contract} busy={busy} upload={upload} review={review} run={run} onManual={c=>{setContract(c);setError('')}} onError={setError}/>;if(active==='Discrepancies')return <Discrepancies result={result}/>;if(active==='Integrations')return <section><PageTitle eyebrow="DATA SOURCES" title="Integrations" text="Local development only. Connect a Jobber test account, upload an approved contract, then audit one job without editing invoices."/><JobberCard status={jobber} onConnect={startJobberConnect}/><JobberAuditPanel connected={!!jobber?.connected} contract={contract} onComplete={r=>{setResult(r);setActive('Discrepancies')}} onError={setError}/></section>;if(active==='Audits')return <AuditHistoryPage/>;if(active==='Settings')return <section><PageTitle eyebrow="CONTROLS" title="Settings" text="The MVP remains read-only. Financial rules require explicit approval before use."/><div className="panel"><h3>Safety controls</h3><p>✓ Human approval for extracted contract rates</p><p>✓ No automatic invoice edits or sends</p><p>✓ Jobber credentials remain server-side</p></div></section>;return <Dashboard approved={approved} pending={pending} result={result} contract={contract} busy={busy} upload={upload} jobber={jobber} go={setActive}/>},[active,contract,result,busy,jobber,approved,pending]);
if(!owner)return <Login onLogin={v=>{displayCurrency=v.currency||'EUR';setOwner(v)}}/>;
return <div className="app-shell"><Sidebar active={active} setActive={setActive}/><main className="content"><header className="topbar"><div><span className="live-dot"></span> MVP 1.0 dev</div><div className="mode">Read-only pilot · {owner.currency} <button className="secondary" style={{marginLeft:12,padding:"7px 12px"}} onClick={()=>{clearOwnerCredentials();setOwner(null);setJobber(null);setContract(null);setResult(null)}}>Sign out</button></div></header>{error&&<div className="error">{error}</div>}{content}</main></div>}
function PageTitle({eyebrow,title,text}:{eyebrow:string,title:string,text:string}){return <div className="page-title"><div className="eyebrow">{eyebrow}</div><h1>{title}</h1><p>{text}</p></div>}
function Dashboard({approved,pending,result,contract,busy,upload,jobber,go}:any){return <section><PageTitle eyebrow="OVERVIEW" title="Billing quality control" text="Catch quantity, rate and missing-item discrepancies before money is missed."/><div className="stats-grid"><StatCard label="CONTRACT RULES APPROVED" value={approved}/><StatCard label="RULES PENDING REVIEW" value={pending}/><StatCard label="EXCEPTIONS" value={result?.discrepancies.length||0}/><StatCard label="POTENTIAL IMPACT" value={fmt(result?.totalPotentialImpact||0)}/></div><div className="two-col"><article className="panel"><div className="panel-head"><div><div className="eyebrow">START HERE</div><h3>Audit a contract</h3></div></div><p>Upload a PDF contract. Billing Copilot extracts candidate rates and keeps them pending until you approve them.</p><label className="upload">{busy?'Extracting…':'Upload contract PDF'}<input type="file" accept="application/pdf" onChange={upload}/></label>{contract&&<button className="link-btn" onClick={()=>go('Contracts')}>Review {contract.rules.length} extracted rules →</button>}</article><JobberCard status={jobber} onConnect={startJobberConnect}/></div><article className="panel workflow"><div><span>1</span><b>Contract</b><small>Expected rates & terms</small></div><i>→</i><div><span>2</span><b>Work records</b><small>What actually happened</small></div><i>→</i><div><span>3</span><b>Draft invoice</b><small>What will be charged</small></div><i>→</i><div><span>4</span><b>Exceptions</b><small>Evidence-backed review</small></div></article></section>}
function Contracts({contract,busy,upload,review,run,onManual,onError}:any){return <section><PageTitle eyebrow="CONTRACT CONTROL" title="Contracts & billing rules" text="Extract rates from a PDF or enter one approved rate for your test account."/><ManualRule onManual={onManual} onError={onError}/>{!contract?<Empty text="No contract uploaded yet." action={<label className="upload">{busy?'Extracting…':'Upload contract PDF'}<input type="file" accept="application/pdf" onChange={upload}/></label>}/>:<><div className="contract-head"><div><b>Contract #{contract.contractId}</b><span>{contract.rules.length} extracted rules</span></div><button className="primary" disabled={!contract.rules.some((r:any)=>r.reviewStatus==='APPROVED')} onClick={run}>Run example-only audit</button></div>{contract.rules.map((r:any)=><article className="rule" key={r.id}><div><div className="rule-title"><b>{r.service}</b><span className={'pill '+r.reviewStatus.toLowerCase()}>{r.reviewStatus}</span></div><p><strong>{fmt(r.rate)}</strong> per {r.unit} · page {r.evidence.page}</p><blockquote>“{r.evidence.quote}”</blockquote></div><div className="rule-actions"><button onClick={()=>review(r.id,'APPROVED')}>Approve</button><button className="secondary" onClick={()=>review(r.id,'REJECTED')}>Reject</button></div></article>)}</>}</section>}
function Discrepancies({result,compact=false}:{result:AuditResult|null,compact?:boolean}){return <section>{!compact&&<PageTitle eyebrow="REVIEW QUEUE" title="Discrepancies" text="Billing Copilot flags evidence for review; it never changes the invoice automatically."/>}{!result?<Empty text="No audit results yet."/>:<><div className="stats-grid two"><StatCard label="EXCEPTIONS" value={result.discrepancies.length}/><StatCard label="POTENTIAL IMPACT" value={fmt(result.totalPotentialImpact)}/></div><div className="table"><div className="tr th"><span>Service</span><span>Issue</span><span>Evidence</span><span>Impact</span></div>{result.discrepancies.map((d,i)=><div className="tr" key={i}><span><b>{d.service}</b></span><span><span className="pill warning">{d.type.replaceAll('_',' ')}</span></span><span>{d.message}</span><span className="money">{fmt(d.potentialImpact)}</span></div>)}</div></>}</section>}
function Empty({text,action}:{text:string,action?:React.ReactNode}){return <div className="empty"><p>{text}</p>{action}</div>}

function JobberAuditPanel({connected,contract,onComplete,onError}:{connected:boolean,contract:Contract|null,onComplete:(r:AuditResult)=>void,onError:(error:string)=>void}){
 const[jobId,setJobId]=useState('SYNTHETIC_NODE_3');
 const[loading,setLoading]=useState(false);
 async function runLive(){if(!contract)return;setLoading(true);onError('');try{
  const data=await runJobberJobAudit(jobId.trim(),contract.contractId);
  onComplete(data.result as AuditResult);
 }catch(e:any){onError(String(e?.message||e))}finally{setLoading(false)}}
 return <article className="panel" style={{marginTop:20}}><div className="eyebrow">DEVELOPMENT AUDIT</div><h3>Audit a Jobber job</h3>
 <p>Requires the Spring backend running with the <code>jobber</code> profile, server-side credentials, and a stored contract rule explicitly approved for each billable service. The GraphiQL session alone cannot authorize our application.</p>
 <p><label>Jobber encoded job ID<br/><input style={{width:'100%',padding:12,marginTop:6,border:'1px solid #d5dfe8',borderRadius:8}} value={jobId} onChange={e=>setJobId(e.target.value)}/></label></p>
 <p>Contract: {contract?`#${contract.contractId} (${contract.rules.filter(r=>r.reviewStatus==='APPROVED').length} approved rules)`:'Upload and approve a contract first.'}</p>
 <button className="primary" disabled={!connected||!contract||!contract.rules.some(r=>r.reviewStatus==='APPROVED')||!jobId.trim()||loading} onClick={runLive}>{loading?'Auditing…':'Run read-only Jobber audit'}</button>
 <p style={{fontSize:12}}>Potential impact is denominated in the connected account’s currency; confirm the currency independently before external reporting. No invoices are sent or edited.</p>
 </article>;
}

function AuditHistoryPage(){
 const [items,setItems]=useState<any[]>([]),[selected,setSelected]=useState(''),[evidence,setEvidence]=useState<any[]>([]),[error,setError]=useState('');
 useEffect(()=>{getAuditHistory().then(setItems).catch((e:any)=>setError(e.message))},[]);
 async function inspect(id:string){setSelected(id);setEvidence([]);try{setEvidence(await getAuditEvidence(id))}catch(e:any){setError(e.message)}}
 return <section><PageTitle eyebrow="AUDIT HISTORY" title="Saved Jobber audits" text="Read-only audit history stored in the backend. Results remain review findings, not automatically recoverable revenue."/>
 {error&&<div className="error">{error}</div>}
 {!items.length?<Empty text="No persisted Jobber audits. Connect Jobber and run a job audit to create one."/>:
 <div className="table">{items.map(a=><div className="tr" key={a.auditId} style={{gridTemplateColumns:'2fr 2fr 1fr 1fr'}}><b>Job {a.externalJobId}</b><span>{new Date(a.createdAt).toLocaleString()}</span><b>{fmt(a.potentialImpact)}</b><button className="secondary" onClick={()=>inspect(a.auditId)}>Evidence</button></div>)}</div>}
 {selected&&<article className="panel" style={{marginTop:16}}><h3>Evidence for audit {selected.slice(0,8)}…</h3>
 {evidence.map((e,i)=><p key={i}><b>{e.discrepancyType}</b> · {e.sourceType}: {e.sourceId}<br/>{e.explanation} — {fmt(e.potentialImpact)}</p>)}
 {!evidence.length&&<p>No evidence records returned.</p>}</article>}</section>
}
function ManualRule({onManual,onError}:{onManual:(c:Contract)=>void;onError:(error:string)=>void}){
 const [service,setService]=useState('Salt Application'),[rate,setRate]=useState('75'),[unit,setUnit]=useState('application');
 const [source,setSource]=useState('Test data: one completed salt application is billed at EUR 75 per application');
 const [confirmed,setConfirmed]=useState(false),[busy,setBusy]=useState(false);
 async function submit(e:React.FormEvent){e.preventDefault();if(!confirmed)return;setBusy(true);onError('');try{onManual(await createManualRate(service,Number(rate),unit,source))}catch(err:any){onError(err.message||String(err))}finally{setBusy(false)}}
 return <form className="panel" style={{marginBottom:18}} onSubmit={submit}>
 <div className="eyebrow">TEST ACCOUNT RATE SHEET</div><h3>Approve a manual billing rate</h3>
 <p>For your Jobber test case, no PDF is needed. This is an owner assertion, not proof of contractual collectibility.</p>
 <div className="manual-grid"><label>Service<input required maxLength={160} value={service} onChange={e=>setService(e.target.value)}/></label>
 <label>Unit rate ({displayCurrency})<input required type="number" min="0.01" step="0.01" value={rate} onChange={e=>setRate(e.target.value)}/></label>
 <label>Unit<select value={unit} onChange={e=>setUnit(e.target.value)}>{['application','visit','hour','month','unit'].map(u=><option key={u} value={u}>{u}</option>)}</select></label></div>
 <label>Evidence / source note<textarea required maxLength={1000} value={source} onChange={e=>setSource(e.target.value)}/></label>
 <label className="manual-confirm"><input type="checkbox" checked={confirmed} onChange={e=>setConfirmed(e.target.checked)}/> I have verified this billing rule for the test job.</label>
 <button className="primary" disabled={!confirmed||busy}>{busy?'Saving…':'Save approved test rate'}</button></form>;
}
createRoot(document.getElementById('root')!).render(<App/>);

function Login({onLogin}:{onLogin:(owner:{currency:string})=>void}){
 const [username,setUsername]=useState(''),[password,setPassword]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false);
 async function submit(e:React.FormEvent){e.preventDefault();setBusy(true);setError('');try{onLogin(await authenticateOwner(username,password))}catch(err:any){setError(err.message||'Unable to sign in')}finally{setBusy(false)}}
 return <div className="owner-login"><form className="panel login-panel" onSubmit={submit}><div className="eyebrow">PRIVATE PILOT</div><h1>Billing Copilot</h1><p>Sign in to access your Jobber audit dashboard. Credentials are held in memory only.</p>
 <label>Username<input autoComplete="username" required value={username} onChange={e=>setUsername(e.target.value)}/></label>
 <label>Password<input type="password" autoComplete="current-password" required value={password} onChange={e=>setPassword(e.target.value)}/></label>
 {error&&<div className="error">{error}</div>}<button className="primary" type="submit" disabled={busy}>{busy?'Signing in…':'Sign in'}</button>
 <small>Use your configured pilot owner login. Connect Jobber only after secure server setup.</small></form></div>
}
