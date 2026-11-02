import React from 'react';
export default function StatCard({label,value,sub}:{label:string,value:string|number,sub?:string}){return <article className="stat-card"><small>{label}</small><strong>{value}</strong>{sub&&<span>{sub}</span>}</article>}
