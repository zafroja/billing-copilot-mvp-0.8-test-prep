export type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type BillingRule = { id:number; service:string; rate:number; unit:string; reviewStatus:ReviewStatus; evidence:{page:number; quote:string} };
export type Contract = { contractId:number; fileName?:string; rules:BillingRule[] };
export type Discrepancy = { type:string; service:string; message:string; potentialImpact:number };
export type AuditResult = { discrepancies:Discrepancy[]; totalPotentialImpact:number };
export type JobberConnectionStatus = { configured:boolean; connected:boolean; accountName?:string; accountId?:string; mode:string };
