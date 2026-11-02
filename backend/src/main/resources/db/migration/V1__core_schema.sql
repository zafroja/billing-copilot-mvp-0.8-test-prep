CREATE TABLE tenants (id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL);
CREATE TABLE billing_audits (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL, external_job_id VARCHAR(255) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL, potential_impact NUMERIC(19,2) NOT NULL DEFAULT 0
);
CREATE INDEX idx_billing_audits_tenant_created ON billing_audits(tenant_id, created_at DESC);
CREATE TABLE audit_evidence (
 id UUID PRIMARY KEY, audit_id UUID NOT NULL, discrepancy_type VARCHAR(80) NOT NULL,
 source_type VARCHAR(80) NOT NULL, source_id VARCHAR(255), explanation TEXT NOT NULL,
 potential_impact NUMERIC(19,2) NOT NULL DEFAULT 0,
 CONSTRAINT fk_audit_evidence_audit FOREIGN KEY(audit_id) REFERENCES billing_audits(id) ON DELETE CASCADE
);
CREATE INDEX idx_audit_evidence_audit ON audit_evidence(audit_id);
