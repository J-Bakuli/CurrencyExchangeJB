CREATE TABLE IF NOT EXISTS currency (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    code TEXT NOT NULL,
    sign TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS exchange_rate (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    base_currency_id INTEGER NOT NULL,
    target_currency_id INTEGER NOT NULL,
    rate NUMERIC(10, 6) NOT NULL,

    CONSTRAINT uc_exchange_pair UNIQUE (base_currency_id, target_currency_id),

    FOREIGN KEY (base_currency_id) REFERENCES currency(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    FOREIGN KEY (target_currency_id) REFERENCES currency(id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_exchange_rate_base_id ON exchange_rate(base_currency_id);
CREATE INDEX IF NOT EXISTS idx_exchange_rate_target_id ON exchange_rate(target_currency_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_currency_code_unique ON currency(code COLLATE NOCASE);