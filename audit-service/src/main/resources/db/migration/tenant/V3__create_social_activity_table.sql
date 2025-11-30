-- Create social activity audit table
CREATE TABLE social_activity
(
    activity_id BIGSERIAL PRIMARY KEY,
    user_id     VARCHAR(255)             NOT NULL,
    action      VARCHAR(100)             NOT NULL,
    entity_type VARCHAR(50),
    entity_id   BIGINT,
    media_id    BIGINT,
    trip_id    BIGINT,
    metadata    JSONB,
    timestamp   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_social_activity_user_timestamp ON social_activity (user_id, timestamp DESC);
CREATE INDEX idx_social_activity_user_action ON social_activity (user_id, action);
CREATE INDEX idx_social_activity_media ON social_activity (media_id, timestamp DESC);
CREATE INDEX idx_social_activity_trip_media ON social_activity (trip_id, media_id, timestamp DESC);

