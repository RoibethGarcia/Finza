CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_categories_name_not_blank CHECK (char_length(btrim(name)) > 0)
);

CREATE INDEX idx_categories_user_id ON categories (user_id);
CREATE INDEX idx_categories_user_id_created_at ON categories (user_id, created_at DESC);
CREATE INDEX idx_categories_user_archived ON categories (user_id, is_archived);
