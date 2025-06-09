CREATE TABLE urls (
    id SERIAL PRIMARY KEY,
    original_url TEXT NOT NULL,
    short_code TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expiry TIMESTAMPTZ,
    click_count INTEGER NOT NULL
);