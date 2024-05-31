## Запуск проекта

### Шаг 1: Запуск Docker Compose

1. Перейдите в директорию `src/main/docker-compose`.
2. Запустите Docker Compose:
   ```bash
   docker-compose up -d
   ```
3. Убедитесь, что контейнеры PostgreSQL и Kafka успешно запущены:
   ```bash
   docker-compose ps
   ```

### Шаг 2: Запуск инстансов приложения

Запустите два инстанса приложения с различными переменными окружения:

Инстанс 1:
```bash
PLAYER_SIGN=O;PORT=8081;GAMER_ID=gamer_2
```

Инстанс 2:
```bash
PLAYER_SIGN=X;PORT=8080;GAMER_ID=gamer_1
```

Или используйте значения по умолчанию для Инстанс 2.

### Шаг 3: Запуск фронтенда

1. Перейдите в директорию `game-fe` https://github.com/nmalinchik/game-fe.
2. Установите зависимости и запустите фронтенд приложение:
   ```bash
   npm install
   npm run dev
   ```
3. Фронтенд приложение будет доступно по адресу: [http://localhost:5173/](http://localhost:5173/). Оно обращается за статусом игры к инстансу на порту 8080.

## Примечания

- Убедитесь, что порты 8080 и 8081 свободны.
- Убедитесь, что установлены Docker, Docker Compose, Node.js и npm.