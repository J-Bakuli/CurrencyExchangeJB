CREATE TABLE currency (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    code TEXT NOT NULL UNIQUE,
    sign TEXT NOT NULL
);

CREATE TABLE exchange_rate (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    base_currency_id INTEGER NOT NULL,
    target_currency_id INTEGER NOT NULL,
    rate NUMERIC(10, 6) NOT NULL,

    UNIQUE (base_currency_id, target_currency_id),

    CONSTRAINT uc_exchange_pair UNIQUE (base_currency_id, target_currency_id),

    FOREIGN KEY (base_currency_id) REFERENCES currency(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    FOREIGN KEY (target_currency_id) REFERENCES currency(id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE INDEX idx_exchange_rate_base_id ON exchange_rate(base_currency_id);
CREATE INDEX idx_exchange_rate_target_id ON exchange_rate(target_currency_id);

CREATE UNIQUE INDEX idx_currency_code_unique ON currency(code COLLATE NOCASE);

INSERT INTO currency (name, code, sign) VALUES
('US Dollar', 'USD', '$'),
('Euro', 'EUR', '€'),
('Japanese Yen', 'JPY', '¥');

INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) VALUES
(1, 2, 0.92),
(1, 3, 140.5);