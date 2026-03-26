DROP TABLE IF EXISTS clothes_attributes_selectable_values CASCADE;
DROP TABLE IF EXISTS feeds_likes CASCADE;
DROP TABLE IF EXISTS feeds_clothes CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS direct_messages CASCADE;
DROP TABLE IF EXISTS user_weather_notifications CASCADE;
DROP TABLE IF EXISTS user_active_locations CASCADE;
DROP TABLE IF EXISTS follows CASCADE;
DROP TABLE IF EXISTS clothes_attributes CASCADE;
DROP TABLE IF EXISTS selectable_values CASCADE;
DROP TABLE IF EXISTS feeds CASCADE;
DROP TABLE IF EXISTS clothes CASCADE;
DROP TABLE IF EXISTS weather_forecast CASCADE;
DROP TABLE IF EXISTS profiles CASCADE;
DROP TABLE IF EXISTS attributes CASCADE;
DROP TABLE IF EXISTS regions CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users
(
    id          UUID PRIMARY KEY,
    email       VARCHAR(255)             NOT NULL UNIQUE,
    password    VARCHAR(100) NULL,
    role        VARCHAR(20)              NOT NULL DEFAULT 'USER',
    locked      BOOLEAN                  NOT NULL DEFAULT false,
    provider    VARCHAR NULL,
    provider_id VARCHAR(300) NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT chk_users_role
        CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT uk_users_provider_provider_id
        UNIQUE (provider, provider_id)
);

CREATE TABLE profiles
(
    id                      UUID PRIMARY KEY,
    name                    VARCHAR                  NOT NULL,
    latitude                DOUBLE PRECISION NULL,
    longitude               DOUBLE PRECISION NULL,
    x                       INTEGER NULL,
    y                       INTEGER NULL,
    region_1depth_name      VARCHAR(50) NULL,
    region_2depth_name      VARCHAR(50) NULL,
    region_3depth_name      VARCHAR(50) NULL,
    gender                  VARCHAR NULL,
    birth_date              DATE NULL,
    temperature_sensitivity INTEGER                  NOT NULL DEFAULT 3,
    profile_image_url       VARCHAR(1000) NULL,
    follower_count          INTEGER                  NOT NULL DEFAULT 0,
    followee_count          INTEGER                  NOT NULL DEFAULT 0,
    user_id                 UUID                     NOT NULL UNIQUE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT fk_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_profiles_gender
        CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT chk_profiles_temperature_sensitivity
        CHECK (temperature_sensitivity >= 1 AND temperature_sensitivity <= 5),
    CONSTRAINT chk_profiles_follower_count
        CHECK (follower_count >= 0),
    CONSTRAINT chk_profiles_followee_count
        CHECK (followee_count >= 0)
);

CREATE TABLE regions
(
    id                 UUID PRIMARY KEY,
    region_code        VARCHAR(10)              NOT NULL UNIQUE,
    region_full_name   VARCHAR(255)             NOT NULL UNIQUE,
    region_1depth_name VARCHAR(50)              NOT NULL,
    region_2depth_name VARCHAR(50)              NOT NULL,
    region_3depth_name VARCHAR(50)              NOT NULL,
    region_4depth_name VARCHAR(50) NULL,
    latitude           DOUBLE PRECISION         NOT NULL,
    longitude          DOUBLE PRECISION         NOT NULL,
    x                  INTEGER                  NOT NULL,
    y                  INTEGER                  NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE attributes
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(100)             NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NULL
);

CREATE INDEX idx_attributes_created_at ON attributes (created_at);

CREATE TABLE selectable_values
(
    id           UUID PRIMARY KEY,
    type         VARCHAR(100)             NOT NULL,
    display_order INT NOT                 NULL,
    is_deleted   BOOLEAN                  NOT NULL DEFAULT false,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NULL,
    attribute_id UUID                     NOT NULL,
    CONSTRAINT fk_selectable_values_attribute
        FOREIGN KEY (attribute_id) REFERENCES attributes (id) ON DELETE CASCADE,
    CONSTRAINT uk_selectable_values_attribute_type
        UNIQUE (attribute_id, type)
);

CREATE INDEX idx_selectable_values_attribute_order ON selectable_values (attribute_id, display_order);

CREATE TABLE weather_forecast
(
    id                                 UUID PRIMARY KEY,
    forecast_at                        TIMESTAMP WITH TIME ZONE NOT NULL,
    forecasted_at                      TIMESTAMP WITH TIME ZONE NOT NULL,
    sky_status                         VARCHAR                  NOT NULL,
    precipitation_type                 VARCHAR                  NOT NULL,
    precipitation_amount               DOUBLE PRECISION         NOT NULL,
    precipitation_probability          DOUBLE PRECISION         NOT NULL,
    humidity_current                   DOUBLE PRECISION         NOT NULL,
    humidity_compared_to_day_before    DOUBLE PRECISION         NOT NULL,
    temperature_current                DOUBLE PRECISION         NOT NULL,
    temperature_compared_to_day_before DOUBLE PRECISION         NOT NULL,
    temperature_min                    DOUBLE PRECISION         NOT NULL,
    temperature_max                    DOUBLE PRECISION         NOT NULL,
    wind_speed                         DOUBLE PRECISION         NOT NULL,
    wind_strength_word                 VARCHAR                  NOT NULL,
    created_at                         TIMESTAMP WITH TIME ZONE NOT NULL,
    region_id                          UUID                     NOT NULL,
    CONSTRAINT fk_weather_forecast_region
        FOREIGN KEY (region_id) REFERENCES regions (id),
    CONSTRAINT chk_weather_forecast_sky_status
        CHECK (sky_status IN ('CLEAR', 'MOSTLY_CLOUDY', 'CLOUDY')),
    CONSTRAINT chk_weather_forecast_precipitation_type
        CHECK (precipitation_type IN ('NONE', 'RAIN', 'RAIN_SNOW', 'SNOW', 'SHOWER')),
    CONSTRAINT chk_weather_forecast_wind_strength_word
        CHECK (wind_strength_word IN ('WEAK', 'MODERATE', 'STRONG')),
    CONSTRAINT chk_weather_forecast_precipitation_amount
        CHECK (precipitation_amount >= 0),
    CONSTRAINT chk_weather_forecast_precipitation_probability
        CHECK (precipitation_probability >= 0 AND precipitation_probability <= 100),
    CONSTRAINT chk_weather_forecast_humidity_current
        CHECK (humidity_current >= 0 AND humidity_current <= 100),
    CONSTRAINT chk_weather_forecast_temp_range
        CHECK (temperature_min <= temperature_max),
    CONSTRAINT chk_weather_forecast_wind_speed
        CHECK (wind_speed >= 0),
    CONSTRAINT uk_weather_forecast_region_forecasted_at
        UNIQUE (region_id, forecasted_at)
);

CREATE TABLE clothes
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(200)             NOT NULL,
    image_url  VARCHAR(1000) NULL,
    type       VARCHAR                  NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NULL,
    user_id    UUID                     NOT NULL,
    CONSTRAINT fk_clothes_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_clothes_type
        CHECK (type IN (
                        'ALL', 'TOP', 'BOTTOM', 'DRESS', 'OUTER',
                        'UNDERWEAR', 'SHOES', 'SOCKS', 'HAT',
                        'BAG', 'ACCESSORY', 'SCARF', 'ETC'
            ))
);

CREATE TABLE clothes_attributes
(
    id           UUID PRIMARY KEY,
    clothes_id   UUID                     NOT NULL,
    attribute_id UUID                     NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_clothes_attributes_clothes
        FOREIGN KEY (clothes_id) REFERENCES clothes (id) ON DELETE CASCADE,
    CONSTRAINT fk_clothes_attributes_attribute
        FOREIGN KEY (attribute_id) REFERENCES attributes (id) ON DELETE CASCADE,
    CONSTRAINT uk_clothes_attributes
        UNIQUE (clothes_id, attribute_id)
);

CREATE TABLE clothes_attributes_selectable_values
(
    id              UUID PRIMARY KEY,
    clothes_attr_id UUID                     NOT NULL,
    value_id        UUID                     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_casv_clothes_attr
        FOREIGN KEY (clothes_attr_id) REFERENCES clothes_attributes (id) ON DELETE CASCADE,
    CONSTRAINT fk_casv_value
        FOREIGN KEY (value_id) REFERENCES selectable_values (id) ON DELETE CASCADE,
    CONSTRAINT uk_clothes_attr
        UNIQUE (clothes_attr_id)
);

CREATE TABLE feeds
(
    id            UUID PRIMARY KEY,
    content       VARCHAR(300)             NOT NULL,
    comment_count INTEGER                  NOT NULL DEFAULT 0,
    like_count    INTEGER                  NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NULL,
    weather_forecast_id    UUID                     NOT NULL,
    user_id       UUID                     NOT NULL,
    CONSTRAINT fk_feeds_weather
        FOREIGN KEY (weather_forecast_id) REFERENCES weather_forecast (id),
    CONSTRAINT fk_feeds_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_feeds_comment_count
        CHECK (comment_count >= 0),
    CONSTRAINT chk_feeds_like_count
        CHECK (like_count >= 0)
);

CREATE TABLE feeds_clothes
(
    id         UUID PRIMARY KEY,
    feed_id    UUID                     NOT NULL,
    clothes_id UUID                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_feeds_clothes_feed
        FOREIGN KEY (feed_id) REFERENCES feeds (id) ON DELETE CASCADE,
    CONSTRAINT fk_feeds_clothes_clothes
        FOREIGN KEY (clothes_id) REFERENCES clothes (id) ON DELETE CASCADE,
    CONSTRAINT uk_feeds_clothes
        UNIQUE (feed_id, clothes_id)
);

CREATE TABLE comments
(
    id         UUID PRIMARY KEY,
    feed_id    UUID                     NOT NULL,
    content    VARCHAR(255)             NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id    UUID                     NOT NULL,
    CONSTRAINT fk_comments_feed
        FOREIGN KEY (feed_id) REFERENCES feeds (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE feeds_likes
(
    id         UUID PRIMARY KEY,
    feed_id    UUID                     NOT NULL,
    user_id    UUID                     NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_feeds_likes_feed
        FOREIGN KEY (feed_id) REFERENCES feeds (id) ON DELETE CASCADE,
    CONSTRAINT fk_feeds_likes_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_feeds_likes
        UNIQUE (feed_id, user_id)
);

CREATE TABLE follows
(
    id          UUID PRIMARY KEY,
    follower_id UUID                     NOT NULL,
    followee_id UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_follows_follower
        FOREIGN KEY (follower_id) REFERENCES users (id),
    CONSTRAINT fk_follows_followee
        FOREIGN KEY (followee_id) REFERENCES users (id),
    CONSTRAINT uk_follows
        UNIQUE (follower_id, followee_id),
    CONSTRAINT chk_follows_self
        CHECK (follower_id <> followee_id)
);

CREATE TABLE user_active_locations
(
    id             UUID PRIMARY KEY,
    user_id        UUID                     NOT NULL,
    region_id      UUID                     NOT NULL,
    last_active_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT fk_user_active_locations_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_active_locations_region
        FOREIGN KEY (region_id) REFERENCES regions (id),
    CONSTRAINT uk_user_active_locations_user
        UNIQUE (user_id)
);

CREATE TABLE user_weather_notifications
(
    id          UUID PRIMARY KEY,
    notice_type VARCHAR                  NOT NULL,
    sent_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id     UUID                     NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT fk_user_weather_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_user_weather_notifications_notice_type
        CHECK (notice_type IN ('RAIN'))
);

CREATE TABLE notifications
(
    id         UUID PRIMARY KEY,
    level      VARCHAR                  NOT NULL,
    title      VARCHAR(100)             NOT NULL,
    content    VARCHAR(500)             NOT NULL,
    type       VARCHAR(50)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    user_id    UUID                     NOT NULL,
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_notifications_level
        CHECK (level IN ('INFO', 'WARN', 'ERROR')),
    CONSTRAINT chk_notifications_type
        CHECK (type IN (
                        'DM', 'FOLLOWED', 'WEATHER_ALERT', 'FEED_LIKED',
                        'FEED_COMMENTED', 'FOLLOWER_NEW_FEED',
                        'ATTRIBUTE_UPDATED', 'ATTRIBUTE_ADDED', 'ATTRIBUTE_DELETED'
            ))
);

CREATE TABLE direct_messages
(
    id          UUID PRIMARY KEY,
    sender_id   UUID                     NOT NULL,
    receiver_id UUID                     NOT NULL,
    content     VARCHAR(255)             NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_direct_messages_self
        CHECK (sender_id <> receiver_id)
);

