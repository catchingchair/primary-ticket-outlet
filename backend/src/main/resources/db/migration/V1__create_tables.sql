CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE venues (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE events (
    id UUID PRIMARY KEY,
    venue_id UUID NOT NULL REFERENCES venues (id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    face_value_cents INTEGER NOT NULL,
    tickets_total INTEGER NOT NULL DEFAULT 0,
    tickets_sold INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    role_id BIGINT NOT NULL REFERENCES roles (id),
    venue_id UUID REFERENCES venues (id),
    UNIQUE (user_id, role_id, venue_id)
);

CREATE TABLE purchases (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events (id),
    user_id UUID NOT NULL REFERENCES users (id),
    quantity INTEGER NOT NULL,
    total_amount_cents INTEGER NOT NULL,
    payment_reference VARCHAR(255),
    idempotency_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (event_id, idempotency_key)
);

CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES events (id),
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    code VARCHAR(24) NOT NULL UNIQUE,
    purchase_id UUID REFERENCES purchases (id)
);

CREATE INDEX idx_ticket_event_status ON tickets (event_id, status);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_email VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    details VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_actor ON audit_logs (actor_email);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at);
