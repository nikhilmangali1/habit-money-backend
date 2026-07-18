CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

CREATE INDEX idx_categories_user_id ON categories(user_id);

CREATE INDEX idx_habits_user_id ON habits(user_id);

CREATE INDEX idx_habits_user_status ON habits(user_id, status);
