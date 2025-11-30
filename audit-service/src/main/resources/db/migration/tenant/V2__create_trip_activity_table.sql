-- Create trip activity audit table with Event Sourcing support
CREATE TABLE trip_activity
(
    activity_id    BIGSERIAL PRIMARY KEY,
    trip_id        BIGINT                   NOT NULL,
    user_id        VARCHAR(255)             NOT NULL,
    action         VARCHAR(100)             NOT NULL,
    entity_type    VARCHAR(50),
    entity_id      BIGINT,
    metadata       JSONB,
    state_snapshot JSONB,
    timestamp      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    event_version  INT                      NOT NULL DEFAULT 1
);

CREATE INDEX idx_trip_activity_trip_timestamp ON trip_activity (trip_id, timestamp DESC);
CREATE INDEX idx_trip_activity_trip_action ON trip_activity (trip_id, action);
CREATE INDEX idx_trip_activity_user_trip ON trip_activity (user_id, trip_id, timestamp DESC);
CREATE INDEX idx_trip_activity_trip_version ON trip_activity (trip_id, event_version);

