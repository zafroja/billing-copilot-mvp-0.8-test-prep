// Credentials are intentionally kept only in this page's memory. Never persist owner passwords.
const BASE=(import.meta.env.VITE_API_BASE_URL || window.location.origin).replace(/\/$/,'');
let authorization='';
export function clearOwnerCredentials(){authorization='';}
function authHeaders():Record<string,string>{return authorization?{'Authorization':authorization}:{};}
function toBasic(username:string,password:string){
 const bytes=new TextEncoder().encode(`${username}:${password}`);
 let binary='';for(const b of bytes)binary+=String.fromCharCode(b);
 return `Basic ${btoa(binary)}`;
}
export async function authenticateOwner(username:string,password:string):Promise<{currency:string; authenticated:boolean}>{
 const attempt=toBasic(username,password);
 const response=await fetch(`${BASE}/api/session`,{headers:{'Authorization':attempt}});
 if(!response.ok)throw new Error(response.status===401?'Incorrect username or password':`Login unavailable (${response.status})`);
 const data=await response.json();authorization=attempt;return data;
}
async function request(path:string,options:RequestInit={}){
 const r=await fetch(`${BASE}${path}`,{...options,headers:{...authHeaders(),...(options.headers||{})}});
 if(!r.ok){const error=await r.json().catch(()=>null);if(r.status===401)throw new Error('Login expired; reload and sign in again');throw new Error(error?.error||error?.message||`Request failed (HTTP ${r.status})`);}
 return r.json();
}
export async function uploadContract(file:File){const fd=new FormData();fd.append('file',file);return request('/api/contracts/upload',{method:'POST',body:fd});}
export async function reviewRule(id:number,status:string){return request(`/api/contracts/rules/${id}`,{method:'PATCH',headers:{'Content-Type':'application/json'},body:JSON.stringify({status})});}
export async function runAudit(contractId:number,work:any[],invoice:any[]){return request(`/api/audits/contracts/${contractId}`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({work,invoice})});}
export async function getJobberStatus(){return request('/api/integrations/jobber/status');}
export async function startJobberConnect(){const x=await request('/api/integrations/jobber/connect');window.location.assign(x.authorizationUrl);}
export async function runJobberJobAudit(jobId:string,contractId:number){return request(`/api/audits/jobs/${encodeURIComponent(jobId)}`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({contractId})});}
export async function getAuditHistory(){return request('/api/audits/history');}
export async function getAuditEvidence(auditId:string){return request(`/api/audits/${encodeURIComponent(auditId)}/evidence`);}

export async function createManualRate(service:string,rate:number,unit:string,source:string){
 return request('/api/contracts/manual',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({service,rate,unit,source,confirmed:true})});
}

export async function getContracts(){return request('/api/contracts');}
