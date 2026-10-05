# Fylvix — PostgreSQL Database Schema

Fylvix uses a normalized PostgreSQL relational schema separating catalog metadata, taxonomy, user personalization, and authorized media delivery.

## Core DDL Schema

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'user', -- 'user', 'moderator', 'admin'
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL,
    avatar_key VARCHAR(120) NOT NULL,
    is_kids BOOLEAN NOT NULL DEFAULT FALSE,
    allow_adult_content BOOLEAN NOT NULL DEFAULT FALSE,
    preferred_language VARCHAR(16) NOT NULL DEFAULT 'en',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash TEXT NOT NULL,
    user_agent TEXT,
    ip_address VARCHAR(64),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE content_classifications (
    id VARCHAR(32) PRIMARY KEY, -- 'G', 'PG', 'PG-13', 'R', 'TV-MA', '18+'
    label VARCHAR(64) NOT NULL,
    min_age INT NOT NULL DEFAULT 0,
    is_adult BOOLEAN NOT NULL DEFAULT FALSE,
    backend_enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE regions (
    code VARCHAR(8) PRIMARY KEY, -- ISO 3166-1 alpha-2
    name VARCHAR(100) NOT NULL,
    allow_adult_catalog BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE genres (
    id VARCHAR(64) PRIMARY KEY,
    slug VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_adult_genre BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE countries (
    code VARCHAR(8) PRIMARY KEY,
    slug VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    flag_emoji VARCHAR(16)
);

CREATE TABLE languages (
    code VARCHAR(16) PRIMARY KEY,
    slug VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100)
);

CREATE TABLE movies (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    original_title VARCHAR(255),
    alternative_titles TEXT[],
    tagline TEXT,
    overview TEXT NOT NULL,
    release_year INT NOT NULL,
    release_date DATE,
    runtime_minutes INT NOT NULL,
    rating NUMERIC(3,1) NOT NULL DEFAULT 0.0,
    vote_count INT NOT NULL DEFAULT 0,
    country_code VARCHAR(8) REFERENCES countries(code),
    language_code VARCHAR(16) REFERENCES languages(code),
    certification VARCHAR(32),
    classification_id VARCHAR(32) REFERENCES content_classifications(id),
    quality_badge VARCHAR(32) NOT NULL DEFAULT '4K UHD',
    poster_url TEXT,
    backdrop_url TEXT,
    logo_url TEXT,
    trailer_url TEXT,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_trending BOOLEAN NOT NULL DEFAULT FALSE,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tv_shows (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    original_title VARCHAR(255),
    overview TEXT NOT NULL,
    release_year INT NOT NULL,
    status VARCHAR(32) NOT NULL, -- 'Airing Today', 'On Air', 'Completed'
    rating NUMERIC(3,1) NOT NULL DEFAULT 0.0,
    country_code VARCHAR(8) REFERENCES countries(code),
    language_code VARCHAR(16) REFERENCES languages(code),
    classification_id VARCHAR(32) REFERENCES content_classifications(id),
    season_count INT NOT NULL DEFAULT 1,
    poster_url TEXT,
    backdrop_url TEXT,
    trailer_url TEXT,
    is_published BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE seasons (
    id VARCHAR(64) PRIMARY KEY,
    tv_show_id VARCHAR(64) NOT NULL REFERENCES tv_shows(id) ON DELETE CASCADE,
    season_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    overview TEXT,
    air_date DATE,
    episode_count INT NOT NULL DEFAULT 0,
    poster_url TEXT
);

CREATE TABLE episodes (
    id VARCHAR(64) PRIMARY KEY,
    tv_show_id VARCHAR(64) NOT NULL REFERENCES tv_shows(id) ON DELETE CASCADE,
    season_id VARCHAR(64) NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    season_number INT NOT NULL,
    episode_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    air_date DATE,
    runtime_minutes INT NOT NULL,
    rating NUMERIC(3,1) NOT NULL DEFAULT 0.0,
    thumbnail_url TEXT
);

CREATE TABLE anime (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    original_title VARCHAR(255),
    overview TEXT NOT NULL,
    anime_type VARCHAR(32) NOT NULL, -- 'Series', 'Movie'
    status VARCHAR(32) NOT NULL, -- 'Ongoing', 'Completed'
    is_dubbed BOOLEAN NOT NULL DEFAULT TRUE,
    is_subbed BOOLEAN NOT NULL DEFAULT TRUE,
    release_year INT NOT NULL,
    rating NUMERIC(3,1) NOT NULL DEFAULT 0.0,
    classification_id VARCHAR(32) REFERENCES content_classifications(id),
    is_published BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE movie_genres (
    movie_id VARCHAR(64) REFERENCES movies(id) ON DELETE CASCADE,
    genre_id VARCHAR(64) REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE TABLE tv_genres (
    tv_show_id VARCHAR(64) REFERENCES tv_shows(id) ON DELETE CASCADE,
    genre_id VARCHAR(64) REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (tv_show_id, genre_id)
);

CREATE TABLE anime_genres (
    anime_id VARCHAR(64) REFERENCES anime(id) ON DELETE CASCADE,
    genre_id VARCHAR(64) REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (anime_id, genre_id)
);

CREATE TABLE people (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    primary_role VARCHAR(64) NOT NULL, -- 'Actor', 'Director', 'Writer', 'Voice Actor'
    country_code VARCHAR(8),
    bio TEXT,
    avatar_url TEXT
);

CREATE TABLE "cast" (
    id SERIAL PRIMARY KEY,
    content_id VARCHAR(64) NOT NULL,
    person_id VARCHAR(64) NOT NULL REFERENCES people(id) ON DELETE CASCADE,
    character_name VARCHAR(255),
    billing_order INT NOT NULL DEFAULT 0
);

CREATE TABLE crew (
    id SERIAL PRIMARY KEY,
    content_id VARCHAR(64) NOT NULL,
    person_id VARCHAR(64) NOT NULL REFERENCES people(id) ON DELETE CASCADE,
    department VARCHAR(64) NOT NULL,
    job VARCHAR(64) NOT NULL
);

CREATE TABLE channels (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    logo_url TEXT,
    country_code VARCHAR(8) REFERENCES countries(code),
    category VARCHAR(64) NOT NULL,
    stream_url TEXT NOT NULL,
    is_live BOOLEAN NOT NULL DEFAULT TRUE,
    is_published BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE channel_programs (
    id VARCHAR(64) PRIMARY KEY,
    channel_id VARCHAR(64) NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL
);

CREATE TABLE media (
    id VARCHAR(64) PRIMARY KEY,
    content_id VARCHAR(64) NOT NULL,
    episode_id VARCHAR(64),
    container_format VARCHAR(32) NOT NULL DEFAULT 'HLS',
    duration_seconds INT NOT NULL,
    drm_protected BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE video_sources (
    id VARCHAR(64) PRIMARY KEY,
    media_id VARCHAR(64) NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    quality VARCHAR(32) NOT NULL, -- '4K UHD', '1080p', '720p', '480p'
    bitrate_kbps INT NOT NULL,
    manifest_path TEXT NOT NULL
);

CREATE TABLE subtitles (
    id VARCHAR(64) PRIMARY KEY,
    media_id VARCHAR(64) NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    language_code VARCHAR(16) NOT NULL,
    label VARCHAR(64) NOT NULL,
    vtt_url TEXT NOT NULL
);

CREATE TABLE audio_tracks (
    id VARCHAR(64) PRIMARY KEY,
    media_id VARCHAR(64) NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    language_code VARCHAR(16) NOT NULL,
    label VARCHAR(64) NOT NULL,
    channels VARCHAR(16) NOT NULL DEFAULT '5.1'
);

CREATE TABLE watch_history (
    id VARCHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL,
    profile_id UUID NOT NULL,
    content_id VARCHAR(64) NOT NULL,
    episode_id VARCHAR(64),
    playback_position_sec BIGINT NOT NULL DEFAULT 0,
    duration_sec BIGINT NOT NULL DEFAULT 0,
    percentage_watched NUMERIC(5,2) NOT NULL DEFAULT 0.0,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    last_watched_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE watchlist (
    profile_id UUID NOT NULL,
    content_id VARCHAR(64) NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (profile_id, content_id)
);

CREATE TABLE favorites (
    profile_id UUID NOT NULL,
    content_id VARCHAR(64) NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (profile_id, content_id)
);

CREATE TABLE ratings (
    profile_id UUID NOT NULL,
    content_id VARCHAR(64) NOT NULL,
    score NUMERIC(3,1) NOT NULL,
    review TEXT,
    rated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (profile_id, content_id)
);

CREATE TABLE recommendations (
    id VARCHAR(64) PRIMARY KEY,
    profile_id UUID NOT NULL,
    content_id VARCHAR(64) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    score NUMERIC(5,2) NOT NULL
);

CREATE TABLE notifications (
    id VARCHAR(64) PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(64) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE reports (
    id VARCHAR(64) PRIMARY KEY,
    user_id UUID,
    content_id VARCHAR(64) NOT NULL,
    reason VARCHAR(128) NOT NULL,
    details TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'open',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE settings (
    key VARCHAR(128) PRIMARY KEY,
    value_json JSONB NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```
