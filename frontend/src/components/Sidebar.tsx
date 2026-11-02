import React from 'react';
const items=['Dashboard','Contracts','Audits','Discrepancies','Integrations','Settings'];
export default function Sidebar({active,setActive}:{active:string,setActive:(x:string)=>void}){return <aside className="sidebar"><div className="brand"><div className="mark">BC</div><div><b>Billing Copilot</b><small>Revenue QA</small></div></div><nav>{items.map(i=><button key={i} className={active===i?'nav active':'nav'} onClick={()=>setActive(i)}>{i}</button>)}</nav><div className="sidebar-foot"><small>READ-ONLY MODE</small><span>Human approval required</span></div></aside>}
