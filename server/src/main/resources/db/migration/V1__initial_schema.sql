-- Baseline schema, transcribed from the Exposed table objects that `SchemaUtils.create`
-- used to generate: Users, Items, ItemEvents, UserSettings.
--
-- Timestamps are BIGINT epoch milliseconds rather than TIMESTAMPTZ because the Kotlin models
-- carry them as `Long` and the mobile clients compare them directly for delta sync.

CREATE TABLE users (
    id              SERIAL       PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    display_name    VARCHAR(255) NOT NULL,
    is_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    -- Populated only between registration and successful verification, then cleared.
    otp_code        VARCHAR(6)   NULL,
    otp_expires_at  BIGINT       NULL
);

CREATE UNIQUE INDEX users_email_unique ON users (email);

CREATE TABLE items (
    -- Client-generated UUID, so a retried create after a flaky network cannot duplicate.
    id           VARCHAR(36) PRIMARY KEY,
    user_id      INT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    kind         VARCHAR(16) NOT NULL,
    name         VARCHAR(120) NOT NULL,
    category     VARCHAR(60) NOT NULL,
    price_cents  BIGINT      NOT NULL,
    currency     VARCHAR(3)  NOT NULL DEFAULT 'EUR',
    purchased_on BIGINT      NOT NULL,
    archived_at  BIGINT      NULL,
    deleted_at   BIGINT      NULL,
    created_at   BIGINT      NOT NULL,
    updated_at   BIGINT      NOT NULL
);

CREATE INDEX items_user_id ON items (user_id);

CREATE TABLE item_events (
    id          VARCHAR(36) PRIMARY KEY,
    item_id     VARCHAR(36) NOT NULL REFERENCES items (id) ON DELETE CASCADE,
    -- Discriminator (wear/wash/use/repair) so cost-per-use and cost-per-month derive from
    -- one table instead of per-kind counter columns on items.
    type        VARCHAR(16) NOT NULL,
    occurred_at BIGINT      NOT NULL,
    cost_cents  BIGINT      NULL,
    note        VARCHAR(255) NULL,
    created_at  BIGINT      NOT NULL,
    deleted_at  BIGINT      NULL
);

CREATE INDEX item_events_item_id_occurred_at ON item_events (item_id, occurred_at);

CREATE TABLE user_settings (
    -- One row per user; a user who never saved has no row and the routes answer defaults.
    user_id         INT         PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    currency        VARCHAR(8)  NOT NULL,
    custom_currency VARCHAR(12) NOT NULL,
    tool_basis      VARCHAR(8)  NOT NULL,
    updated_at      BIGINT      NOT NULL
);
