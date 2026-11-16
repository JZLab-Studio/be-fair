-- The cropped profile picture lives on the user's settings row. Both columns are NULL until the
-- user uploads one; content_type is set together with the bytes.
ALTER TABLE user_settings
    ADD COLUMN profile_picture BYTEA NULL,
    ADD COLUMN profile_picture_type VARCHAR(32) NULL;
