-- Create filestorage activity audit table
CREATE TABLE filestorage_activity
(
    activity_id BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(255)             NOT NULL,
    action      VARCHAR(100)             NOT NULL,
    entity_type VARCHAR(50),
    entity_id   VARCHAR(100),
    metadata    JSONB,
    timestamp   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_filestorage_activity_user_timestamp ON filestorage_activity (user_id, timestamp DESC);
CREATE INDEX idx_filestorage_activity_user_action ON filestorage_activity (user_id, action);