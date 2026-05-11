CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'review_task_status') THEN
        CREATE TYPE review_task_status AS ENUM (
            'PENDING',
            'RUNNING',
            'SUPERVISING',
            'SPECIALISTS_RUNNING',
            'SUMMARIZING',
            'SECOND_ROUND',
            'COMPLETED',
            'FAILED'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'agent_role') THEN
        CREATE TYPE agent_role AS ENUM (
            'SUPERVISOR',
            'STRUCTURE',
            'LOGIC',
            'RISK'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'agent_run_status') THEN
        CREATE TYPE agent_run_status AS ENUM (
            'PENDING',
            'RUNNING',
            'COMPLETED',
            'FAILED'
        );
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS review_task (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255),
    original_text TEXT NOT NULL,
    requirement TEXT NOT NULL,
    content_type VARCHAR(80),
    status review_task_status NOT NULL DEFAULT 'PENDING',
    second_round_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    langfuse_trace_id VARCHAR(255),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS document_chunk (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_task_id UUID NOT NULL REFERENCES review_task(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    token_count INTEGER,
    embedding vector(1536),
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (review_task_id, chunk_index)
);

CREATE TABLE IF NOT EXISTS agent_run (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_task_id UUID NOT NULL REFERENCES review_task(id) ON DELETE CASCADE,
    parent_agent_run_id UUID REFERENCES agent_run(id) ON DELETE SET NULL,
    role agent_role NOT NULL,
    round_no INTEGER NOT NULL DEFAULT 1,
    status agent_run_status NOT NULL DEFAULT 'PENDING',
    input_summary TEXT,
    output_summary TEXT,
    raw_input JSONB NOT NULL DEFAULT '{}'::jsonb,
    raw_output JSONB NOT NULL DEFAULT '{}'::jsonb,
    langfuse_observation_id VARCHAR(255),
    error_message TEXT,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS critique_result (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_task_id UUID NOT NULL REFERENCES review_task(id) ON DELETE CASCADE,
    agent_run_id UUID NOT NULL REFERENCES agent_run(id) ON DELETE CASCADE,
    role agent_role NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 0 AND score <= 100),
    feedback TEXT NOT NULL,
    suggestions JSONB NOT NULL DEFAULT '[]'::jsonb,
    confidence NUMERIC(5,4) CHECK (confidence >= 0 AND confidence <= 1),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS critique_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    critique_result_id UUID NOT NULL REFERENCES critique_result(id) ON DELETE CASCADE,
    document_chunk_id UUID REFERENCES document_chunk(id) ON DELETE SET NULL,
    quote TEXT,
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS review_report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_task_id UUID NOT NULL UNIQUE REFERENCES review_task(id) ON DELETE CASCADE,
    overall_score INTEGER CHECK (overall_score >= 0 AND overall_score <= 100),
    executive_summary TEXT NOT NULL,
    strengths JSONB NOT NULL DEFAULT '[]'::jsonb,
    weaknesses JSONB NOT NULL DEFAULT '[]'::jsonb,
    prioritized_actions JSONB NOT NULL DEFAULT '[]'::jsonb,
    second_round_performed BOOLEAN NOT NULL DEFAULT FALSE,
    final_markdown TEXT NOT NULL,
    report_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS token_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_task_id UUID NOT NULL REFERENCES review_task(id) ON DELETE CASCADE,
    agent_run_id UUID REFERENCES agent_run(id) ON DELETE SET NULL,
    model_name VARCHAR(120) NOT NULL,
    prompt_tokens INTEGER NOT NULL DEFAULT 0,
    completion_tokens INTEGER NOT NULL DEFAULT 0,
    total_tokens INTEGER NOT NULL DEFAULT 0,
    estimated_cost NUMERIC(12,8) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS review_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_task_id UUID NOT NULL REFERENCES review_task(id) ON DELETE CASCADE,
    event_type VARCHAR(80) NOT NULL,
    message TEXT NOT NULL,
    payload_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS prompt_variant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    role agent_role NOT NULL,
    version VARCHAR(40) NOT NULL,
    prompt_template TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (name, role, version)
);

CREATE INDEX IF NOT EXISTS idx_review_task_status_created_at
    ON review_task (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_document_chunk_review_task
    ON document_chunk (review_task_id, chunk_index);

CREATE INDEX IF NOT EXISTS idx_document_chunk_embedding
    ON document_chunk
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_agent_run_review_task_role
    ON agent_run (review_task_id, role, round_no);

CREATE INDEX IF NOT EXISTS idx_critique_result_review_task_role
    ON critique_result (review_task_id, role);

CREATE INDEX IF NOT EXISTS idx_critique_evidence_result
    ON critique_evidence (critique_result_id);

CREATE INDEX IF NOT EXISTS idx_token_usage_review_task
    ON token_usage (review_task_id);

CREATE INDEX IF NOT EXISTS idx_review_event_task_created_at
    ON review_event (review_task_id, created_at);

CREATE INDEX IF NOT EXISTS idx_prompt_variant_active_role
    ON prompt_variant (role, active);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_review_task_updated_at ON review_task;
CREATE TRIGGER trg_review_task_updated_at
BEFORE UPDATE ON review_task
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
