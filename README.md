
# Bank REST API — Описание приложения

## 1. Общая информация

Приложение представляет собой банковскую систему с возможностями управления пользователями, картами и переводами.

**Основные возможности:**

* Регистрация и аутентификация пользователей с использованием JWT.
* Ролевой доступ: `ADMIN` и `USER`.
* Управление пользователями (ADMIN): создание, блокировка, изменение роли.
* Управление картами (ADMIN): создание, активация, блокировка, пометка просроченной, удаление.
* Пользовательские операции: просмотр своих карт, запрос блокировки, просмотр баланса, переводы между картами.
* Пагинация и поиск для списковых эндпоинтов.

**Технологии:**

* Java 17, Spring Boot, Spring Security, Hibernate, PostgreSQL
* Docker и Docker Compose для локального и dev окружения
* JWT для аутентификации
* Swagger для документирования API

---

## 2. Креденциалы для тестирования

**Администратор:**

* Логин: `admin2`
* Пароль: `passport3`
* Роль: `ADMIN`

**Пользователь:**

* Логин: `Ольга`
* Пароль: `qwerty123`
* Роль: `USER`

---

## 3. Swagger

Все эндпоинты документированы через Swagger UI:

* [Swagger UI](http://localhost:8080/swagger-ui.html)
* [API Docs](http://localhost:8080/v3/api-docs)

Swagger содержит полное описание всех эндпоинтов, моделей запросов и ответов.

---

## 4. Запуск проекта

### 4.1 Подготовка

1. Склонируйте репозиторий проекта.
2. В корне проекта создайте файл `.env` со следующими переменными:

```env
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

### 4.2 Запуск через Docker Compose

В терминале, находясь в папке проекта, выполните команду:

```bash
docker-compose up --build
```

**Описание контейнеров:**

* `db` — PostgreSQL, база данных проекта
* `app` — Spring Boot приложение, подключенное к базе внутри Docker сети

Приложение будет доступно на порту, указанном в переменной `API_PORT` (по умолчанию 8080).


