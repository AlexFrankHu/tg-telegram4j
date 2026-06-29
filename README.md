# tg-telegram4j

Spring Boot application that uses [Telegram4J](https://github.com/Telegram4J/Telegram4J) to login to Telegram accounts directly from Telethon `.session` files — no verification code or 2FA password needed.

## How it works

1. Telethon `.session` files are SQLite databases containing a 256-byte **MTProto auth_key**
2. This app reads the auth_key and injects it into Telegram4J's session store
3. Telegram4J connects using the existing auth_key and skips the entire auth flow
4. After the first successful connection, the session is persisted as a `t4j.bin` file

## Prerequisites

- Java 17+
- Maven 3.8+
- A Telethon `.session` file (that's it — no API credentials needed)

### Build Telegram4J locally (required — not published to Maven Central)

```bash
# 1. Build tl-parser
git clone https://github.com/telegram4j/tl-parser.git
cd tl-parser
./gradlew publishToMavenLocal -x test -x updateSchemas
cd ..

# 2. Build Telegram4J
git clone https://github.com/Telegram4J/Telegram4J.git
cd Telegram4J
./gradlew publishToMavenLocal -x test
cd ..
```

## Build & Run

```bash
mvn clean package -DskipTests
java -jar target/tg-telegram4j-1.0.0-SNAPSHOT.jar
```

Optionally override default API credentials or data directory:

```bash
java -jar target/tg-telegram4j-1.0.0-SNAPSHOT.jar \
  --telegram.api-id=YOUR_API_ID \
  --telegram.api-hash=YOUR_API_HASH \
  --telegram.data-dir=./data
```

## API

### Login by uploading .session file

```bash
# Only session file is required — apiId/apiHash use built-in defaults
curl -X POST http://localhost:8080/api/session/upload \
  -F "file=@/path/to/account.session" \
  -F "sessionName=my_account"
```

### Login by server-side file path

```bash
curl -X POST http://localhost:8080/api/session/import \
  -H "Content-Type: application/json" \
  -d '{
    "sessionName": "my_account",
    "sessionFilePath": "/data/account.session"
  }'
```

### List active sessions

```bash
curl http://localhost:8080/api/session/list
```

### Get session info

```bash
curl http://localhost:8080/api/session/my_account
```

### Disconnect session

```bash
curl -X POST http://localhost:8080/api/session/my_account/disconnect
```

## Response format

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "sessionName": "my_account",
    "userId": 123456789,
    "firstName": "John",
    "lastName": "Doe",
    "username": "johndoe",
    "phone": "+1234567890",
    "connected": true
  }
}
```

## Important notes

- `apiId`/`apiHash` are optional — defaults to Telegram Desktop's public credentials (same as Telethon)
- You can override them per-request or via `application.yml` if needed
- If the session has expired on Telegram's side, login will fail with an auth error
- Session data is persisted to `{data-dir}/{sessionName}.t4j.bin` after the first successful login
- Subsequent startups can use the `.t4j.bin` file directly without the original `.session` file

## Architecture

```
.session file (SQLite)
    |
    v
TelethonSessionReader  -- extracts auth_key (256 bytes) + dc_id
    |
    v
TelethonImportStoreLayout  -- injects auth_key into Telegram4J's store
    |
    v
MTProtoTelegramClient  -- connects with existing auth_key, skips auth flow
    |
    v
REST API  -- exposes login/status/disconnect endpoints
```
