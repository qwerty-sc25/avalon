DC=docker compose -f docker-compose.local.yml

up:
	$(DC) up -d --build

down:
	$(DC) down

logs:
	$(DC) logs -f

ps:
	$(DC) ps

restart: down up

rebuild:
	$(DC) build --no-cache