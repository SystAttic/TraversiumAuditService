-- Create user activity audit table
CREATE TABLE user_activity
(
    activity_id BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(255)             NOT NULL,
    action      VARCHAR(100)             NOT NULL,
    entity_type VARCHAR(50),
    entity_id   BIGINT,
    metadata    JSONB,
    timestamp   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_activity_user_timestamp ON user_activity (user_id, timestamp DESC);
CREATE INDEX idx_user_activity_user_action ON user_activity (user_id, action);

