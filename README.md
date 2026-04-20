# CurrencyExchangeJB

Небольшой гайд, чтобы быстро поднять проект локально или на сервере.

## О проекте

`CurrencyExchangeJB` — это REST-сервис для работы с валютами и курсами:
- можно добавлять и смотреть валюты;
- создавать, смотреть и обновлять курсы;
- конвертировать суммы (прямой, обратный и кросс-курс).

## Deploy

- Demo [Currency Exchange Service](http://31.56.208.168/)

## Технологии

- Java 17
- Jakarta Servlet API
- Maven
- SQLite
- HikariCP
- MapStruct
- Jackson
- Lombok
- Tomcat
- Nginx (reverse proxy для публичного запуска)

## Требования

- Java 17
- Maven 3.9+
- Apache Tomcat 10/11
- Docker + Docker Compose (для публичного запуска)

## Сборка

```bash
mvn clean package
```

После сборки получите `target/CurrencyExchangeJB.war`.

## Локальный запуск

Tomcat + WAR

1. Соберите проект:
   ```bash
   mvn clean package
   ```
2. Проверьте настройки SQLite:
   - файл БД: `src/main/resources/db/currency_exchange.db`
   - конфиг подключения: `src/main/resources/datasource.properties`
   - JDBC URL по умолчанию: `jdbc:sqlite:src/main/resources/db/currency_exchange.db`
3. Если нужен другой путь к БД — поменяйте `jdbcUrl` в `datasource.properties`.
4. Фронт использует относительные API URL (`currencies`, `exchangeRates`, `exchange`) и автоматически определяет context path:
   - локально в Tomcat запросы уходят под `/CurrencyExchangeJB`;
   - за `nginx` reverse proxy — в корень `/`.
5. Разверните `target/CurrencyExchangeJB.war` в локальный Tomcat и запустите его.
6. После изменения `js` делайте `Ctrl+F5` (hard refresh), чтобы браузер не использовал старый кэш.
7. Быстрая проверка:
   - `/currencies`
   - `/exchangeRates`
   - `/exchange?from=USD&to=EUR&amount=10`

## Публичный запуск

Docker + nginx (reverse proxy, port hiding, unified routing)

1. Соберите WAR:
   ```bash
   mvn clean package
   ```
2. Подготовьте директории, которые использует `docker-compose.yml`:
   - `./tomcat/webapps`
   - `./logs`
   - `./db`
3. Положите WAR в `./tomcat/webapps` (например, `CurrencyExchangeJB.war`).
4. Запустите контейнеры:
   ```bash
   docker compose up -d
   ```
5. Для SQLite в публичном окружении:
   - храните рабочую БД в постоянном томе/директории (`./db`);
   - делайте регулярные бэкапы файла `currency_exchange.db`;
   - не публикуйте файл БД через HTTP.
6. Проверьте, что API доступен:
   - `/currencies`
   - `/exchangeRates`
   - `/exchange?from=USD&to=EUR&amount=10`

## Настройка SQLite

- Приложение использует SQLite через JDBC + HikariCP.
- Локальный путь по умолчанию: `src/main/resources/db/currency_exchange.db`.
- Для публичного окружения используйте персистентное хранилище (volume), а не файловую систему контейнера.
- Держите `datasource.properties` синхронизированным с фактическим путем к БД.

## Endpoint’ы

- `GET /currencies` — получить список всех валют.
- `POST /currencies` — создать новую валюту (`name`, `code`, `sign`).
- `GET /currency/{CODE}` — получить валюту по коду.
- `GET /exchangeRates` — получить список всех курсов.
- `POST /exchangeRates` — создать курс (`baseCurrencyCode`, `targetCurrencyCode`, `rate`).
- `GET /exchangeRate/{BASE}{TARGET}` — получить курс для пары (например, `/exchangeRate/USDRUB`).
- `PATCH /exchangeRate/{BASE}{TARGET}` — обновить курс для пары (`rate`).
- `GET /exchange?from={CODE}&to={CODE}&amount={VALUE}` — выполнить конвертацию суммы.

## Мини-примеры curl

Базовый URL в примерах:
- `http://localhost:8080/CurrencyExchangeJB`

Получить все валюты:
```bash
curl -X GET "http://localhost:8080/CurrencyExchangeJB/currencies"
```

Создать валюту:
```bash
curl -X POST "http://localhost:8080/CurrencyExchangeJB/currencies" \
  -d "name=US Dollar&code=USD&sign=\$"
```

Получить валюту по коду:
```bash
curl -X GET "http://localhost:8080/CurrencyExchangeJB/currency/USD"
```

Получить все курсы:
```bash
curl -X GET "http://localhost:8080/CurrencyExchangeJB/exchangeRates"
```

Создать курс:
```bash
curl -X POST "http://localhost:8080/CurrencyExchangeJB/exchangeRates" \
  -d "baseCurrencyCode=USD&targetCurrencyCode=EUR&rate=0.93"
```

Получить курс по паре:
```bash
curl -X GET "http://localhost:8080/CurrencyExchangeJB/exchangeRate/USDEUR"
```

Обновить курс:
```bash
curl -X PATCH "http://localhost:8080/CurrencyExchangeJB/exchangeRate/USDEUR" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "rate=0.95"
```

Выполнить конвертацию:
```bash
curl -X GET "http://localhost:8080/CurrencyExchangeJB/exchange?from=USD&to=EUR&amount=100"
```

