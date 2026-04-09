INSERT INTO currency (code, name, sign) VALUES
('USD', 'US Dollar', '$'),
('EUR', 'Euro', '€'),
('JPY', 'Japanese Yen', '¥');


INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) VALUES
(1, 2, 0.92),
(1, 3, 140.5);