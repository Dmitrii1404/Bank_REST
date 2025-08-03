<h1> Разработка Системы Управления Банковскими Картами</h1>

  <p>Bank_REST это backend-приложение на Java (Spring Boot) для управления банковскими картами:</p>
  <ul>
    <li>Создание и управление картами</li>
    <li>Просмотр карт</li>
    <li>Переводы между своими картами</li>
  </ul>

<h2>Запуск проекта</h2>
1. **Настройка проекта**
    <p>В корне проекта создайте файл <code>.env</code> с содержимым:<p>

    ```text
    # ENCRYPT_KEY - 32 символа. Пример:
    ENCRYPT_KEY=12345678901234567890123456789012
    
    #SECURITY_SECRET_KEY - 95 символов. Пример:
    SECURITY_SECRET_KEY=qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJ
    
    # Имя, пароль, название БД. Пример:
    POSTGRES_USER=postgres
    POSTGRES_PASSWORD=postgresPass
    POSTGRES_DB=bankcards_db
    
    # Оставить как есть:
    SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/${POSTGRES_DB}
    SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
    SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
    ```
2. **Запуск проекта с использованием Docker Compose:**
   <p>Для того чтобы запустить проект в контейнерах, используйте команду:<p>


   ```bash
      docker-compose up --build
   ```

3. **Доступ к приложению:**

   После того как контейнеры будут запущены, приложение будет доступно по адресу:

    - **Основной интерфейс:** [http://localhost:8000](http://localhost:8000)
    - **Документация API:** [http://localhost:8000/docs](http://localhost:8000/swagger-ui.html)


<h2> Структура проекта </h2>

```text
    docs                                    # OpenAPI спецификация и дополнительные описания API
    src/ 
    ├── main/
    |   ├── java/
    |   |   ├── controller                  # REST-контроллеры для управления пользователями, картами и переводами
    |   |   ├── dto                         # Классы передачи данных между слоями приложения
    |   |   ├── entity                      # JPA-сущности: Card, User, Role
    |   |   ├── exception                   # Глобальный обработчик ошибок и пользовательские исключения
    |   |   ├── repository                  # Интерфейсы Spring Data JPA для доступа к базе данных.
    |   |   ├── security                    # Конфигурации и компоненты безопасности: JWT, фильтры, UserDetailsService
    |   |   ├── service                     # Бизнес-логика: управление пользователями, картами и переводами
    |   |   ├── util                        # Вспомогательные классы: шифрование, маскирование
    |   |   └── BankRestApplication.java    # Точка входа
    |   ├── resources/
    |   |   ├── db.migration                # YAML-модули для миграций базы данных
    |   |   └── application.yml             # главный конфигурационный файл Spring Boot
    ├── test/
    |   ├── controller                      # Unit тесты для REST API с использованием MockMvc
    |   └── service                         # Юнит-тесты бизнес-логики с использованием моков
    pom.mxl                                 # файл для управления зависимостями проекта
```
  

<h3>Атрибуты карты</h3>
  <ul>
    <li>Номер карты (зашифрован, отображается маской: <code>**** **** **** 1234</code>)</li>
    <li>Владелец</li>
    <li>Срок действия</li>
    <li>Статус: Активна, Заблокирована, Истек срок</li>
    <li>Баланс</li>
  </ul>

<h3>Аутентификация и авторизация</h3>
  <ul>
    <li>Spring Security + JWT</li>
    <li>Роли: <code>ADMIN</code> и <code>USER</code></li>
  </ul>

<h3>Возможности пользователей</h3>
<strong>Для всех:</strong>
<ul>
    <li>Создают аккаунты + авторизация</li>
  </ul>
<strong>ADMIN:</strong>
  <ul>
    <li>Создаёт, блокирует, активирует, удаляет карты</li>
    <li>Видит запросы пользователей и исполняет их</li>
    <li>Изменяет данные пользователей</li>
    <li>Видит все карты</li>
  </ul>

<strong>USER:</strong>
  <ul>
    <li>Просматривает свои карты (поиск + пагинация)</li>
    <li>Запрашивает блокировку карты + видит свои запросы</li>
    <li>Делает переводы между своими картами</li>
    <li>Смотрит баланс</li>
    <li>Самостоятельно емняет пароль</li>
  </ul>

<h3>API</h3>
  <ul>
    <li>CRUD для карт</li>
    <li>Переводы между своими картами</li>
    <li>Фильтрация и постраничная выдача</li>
    <li>Валидация и сообщения об ошибках</li>
  </ul>

<h3>Безопасность</h3>
  <ul>
    <li>Шифрование данных</li>
    <li>Ролевой доступ</li>
    <li>Маскирование номеров карт</li>
  </ul>

<h3>Работа с БД</h3>
  <ul>
    <li>PostgreSQL</li>
        <p>Используется для хранения информации о пользователях, картах и запросов на блокировку. Схема представления реляционной базы данных включает следующие таблицы:</p>
        <ul>
            <li>users - информация о пользователях</li>
            <li>cards - информация о картах</li>
            <li>request_block - запросы на блокировку</li>
        </ul>
    <li>Миграции через Liquibase</li>
  </ul>

<h3>Документация</h3>
  <ul>
    <li>Swagger UI / OpenAPI</li>
    <li><code>README.md</code> с инструкцией запуска</li>
  </ul>

<h3>Развёртывание и тестирование</h3>
  <ul>
    <li>Docker Compose</li>
    <li>Liquibase миграции</li>
    <li>Юнит-тесты ключевой бизнес-логики</li>
  </ul>

<h2>Технологии</h2>
  <p>
    Java 21, Spring Boot, Spring Security, Spring Data JPA, PostgreSQL, Liquibase, Docker, JWT, Swagger (OpenAPI)
  </p>
