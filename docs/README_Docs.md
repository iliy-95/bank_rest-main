
# Инструкция по запуску и работе с проектом

## 1. Подготовка

1. Склонируйте репозиторий проекта.
2. В корне проекта создайте файл `.env` со следующими переменными:

```
POSTGRES_DB=bank_dev
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432

API_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/bank_dev
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_PROFILES_ACTIVE=dev
JWT_SECRET=MySuperSecretKeyThatIsAtLeast32BytesLongForJWTTokenSecurity!!
AES_KEY=MyAESEncryptionKey1234567890123456
```

> Эти переменные используются для конфигурации контейнеров Docker и Spring Boot приложения.

---

## 2. Запуск через Docker Compose

1. В терминале, находясь в папке проекта, выполните команду:
docker-compose up --build

2. Docker Compose поднимет два контейнера:

    * **db** — PostgreSQL, база данных проекта.
    * **app** — Spring Boot приложение, подключенное к базе внутри Docker сети.

3. Приложение будет доступно на порту, указанном в переменной `API_PORT` (по умолчанию `8080`).

> Итог: после команды `docker-compose up --build` приложение и база данных работают сразу, ничего дополнительно создавать не нужно.

# API

## Аутентификация

### Регистрация нового пользователя

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "Ольга",
  "password": "qwerty123",
  "fullName": "Ольга Иванова"
}
```

### Вход в систему

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin2",
  "password": "passport3"
}
```

**Пример ответа:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

---

## Администраторские эндпоинты

**Тестовые креденциалы:**

* Логин: `admin2`
* Пароль: `passport3`
* Роль: `ADMIN`

### Управление пользователями

* Получить всех пользователей:

```http
GET /api/v1/admin/users
Authorization: Bearer {jwt_token}
```

* Получить пользователя по ID:

```http
GET /api/v1/admin/users/{id}
Authorization: Bearer {jwt_token}
```

* Изменить роль пользователя:

```http
PUT /api/v1/admin/users/{id}/role
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "role": "ADMIN"
}
```

* Заблокировать пользователя:

```http
PATCH /api/v1/admin/users/{id}/block
Authorization: Bearer {jwt_token}
```

* Разблокировать пользователя:

```http
PATCH /api/v1/admin/users/{id}/unblock
Authorization: Bearer {jwt_token}
```

### Управление картами

* Получить все карты (с поиском и пагинацией):

```http
GET /api/v1/admin/cards/all?search={поисковый_запрос}&page=0&size=10
Authorization: Bearer {jwt_token}
```

* Получить карту по ID:

```http
GET /api/v1/admin/cards/{cardId}/users_cards
Authorization: Bearer {jwt_token}
```

* Создать карту пользователю:

```http
POST /api/v1/admin/cards
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "number": "1234567812345678",
  "holderName": "ОЛЬГА ИВАНОВА",
  "expiryDate": "2027-12-31",
  "userId": "uuid-пользователя"
}
```

* Активировать карту:

```http
PATCH /api/v1/admin/cards/{cardId}/activate
Authorization: Bearer {jwt_token}
```

* Заблокировать карту:

```http
PATCH /api/v1/admin/cards/{cardId}/block
Authorization: Bearer {jwt_token}
```

* Пометить карту как просроченную:

```http
PATCH /api/v1/admin/cards/{cardId}/expired
Authorization: Bearer {jwt_token}
```

* Удалить карту:

```http
DELETE /api/v1/admin/cards/{cardId}/delete
Authorization: Bearer {jwt_token}
```

---

## Пользовательские эндпоинты

**Тестовые креденциалы:**

* Логин: `Ольга`
* Пароль: `qwerty123`
* Роль: `USER`

### Мои карты

* Получить мои карты (с поиском и пагинацией):

```http
GET /api/v1/user/cards/my_cards?search={поисковый_запрос}&page=0&size=10
Authorization: Bearer {jwt_token}
```

* Запросить блокировку карты:

```http
PATCH /api/v1/user/cards/req_block/{cardId}
Authorization: Bearer {jwt_token}
```

* Посмотреть баланс карты:

```http
GET /api/v1/user/cards/{cardId}/balance
Authorization: Bearer {jwt_token}
```

### Переводы между картами

* Перевод между своими картами:

```http
POST /api/v1/user/cards/transactions/transfer
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "fromCardId": "uuid-карты-отправителя",
  "toCardId": "uuid-карты-получателя",
  "amount": 100.50
}
```

---

## Параметры пагинации

* `page` — номер страницы (начинается с 0)
* `size` — количество элементов на странице (по умолчанию 20)
* `sort` — сортировка (например: `createdAt,desc`)

---

## Примеры использования через `curl`

1. Вход администратора:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin2","password":"passport3"}'
```

2. Получение всех карт (админ):

```bash
curl -X GET "http://localhost:8080/api/v1/admin/cards/all?page=0&size=5" \
  -H "Authorization: Bearer {jwt_token}"
```

3. Перевод между картами (пользователь):

```bash
curl -X POST http://localhost:8080/api/v1/user/cards/transactions/transfer \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{"fromCardId":"aaaa1111-aaaa-1111-aaaa-111111111111","toCardId":"bbbb2222-bbbb-2222-bbbb-222222222222","amount":50.00}'
```
* 
* JWT аутентификация — все запросы (кроме `/auth`) требуют Bearer токен.
* Ролевой доступ — разделение на ADMIN и USER endpoints.
* Валидация — автоматическая проверка входных данных.
* Пагинация — для списковых endpoints.
* Поиск — фильтрация по различным полям.

> Документация доступна в Swagger UI:
> `http://localhost:8080/swagger-ui.html`
> `http://localhost:8080/v3/api-docs`



