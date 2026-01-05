CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    parent_category_id UUID,
    FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE CASCADE
);
CREATE INDEX idx_categories_user_id ON categories(user_id);

CREATE TABLE recipes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    source_url VARCHAR(2048),
    title VARCHAR(500) NOT NULL,
    ingredients TEXT[],
    steps TEXT[],
    thumbnail_url VARCHAR(2048),
    prep_time VARCHAR(100),
    cook_time VARCHAR(100),
    total_time VARCHAR(100),
    servings VARCHAR(100),
    raw TEXT,
    rating INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_recipes_user_id ON recipes(user_id);

CREATE TABLE recipes_categories (
    recipe_id UUID NOT NULL,
    category_id UUID NOT NULL,
    PRIMARY KEY (recipe_id, category_id),
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);
CREATE INDEX idx_recipes_categories_recipe ON recipes_categories(recipe_id);
CREATE INDEX idx_recipes_categories_category ON recipes_categories(category_id);