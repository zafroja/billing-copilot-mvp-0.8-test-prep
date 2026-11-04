import React from 'react';
type ConnectionStatus = {configured:boolean;connected:boolean;accountName?:string} | null;
export default function JobberCard({status,onConnect,error,onRetry}:{
  status:ConnectionStatus;
  onConnect:()=>void;
  error?:string;
  onRetry?:()=>void;
}){
  const unavailable = Boolean(error);
  const checking = !status && !unavailable;
  const description = unavailable
    ? 'Could not load the current Jobber connection status. This does not mean the credentials are missing.'
    : checking
      ? 'Checking Jobber connection settings…'
      : status?.connected
        ? `Connected to ${status.accountName || 'Jobber account'}`
        : status?.configured
          ? 'Client credentials detected. Ready to connect with Jobber OAuth.'
          : 'The server reports that Jobber client credentials are missing. Check the Render environment settings.';
  const badge = unavailable ? 'STATUS UNAVAILABLE'
    : checking ? 'CHECKING'
    : status?.connected ? 'CONNECTED'
    : status?.configured ? 'READY'
    : 'NOT CONFIGURED';
  const badgeClass = status?.connected ? 'ok' : status?.configured ? 'ready' : 'muted-pill';
  return <article className="integration-card">
    <div><div className="eyebrow">INTEGRATION</div><h3>Jobber</h3><p>{description}</p></div>
    <div className="integration-actions">
      <span className={`pill ${badgeClass}`}>{badge}</span>
      {unavailable
        ? <button onClick={onRetry}>Retry status</button>
        : <button disabled={!status?.configured || !!status.connected}
            onClick={onConnect}>{status?.connected ? 'Connected' : 'Connect Jobber'}</button>}
    </div>
  </article>;
}
