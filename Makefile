# Infra
infra-up:
	docker compose -f docker-compose.infra.yml up -d

infra-down:
	docker compose -f docker-compose.infra.yml down

# Services
services-up:
	docker compose -f docker-compose.services.yml up -d

services-down:
	docker compose -f docker-compose.services.yml down

# All
up:
	docker compose -f docker-compose.infra.yml -f docker-compose.services.yml up -d

down:
	docker compose -f docker-compose.infra.yml -f docker-compose.services.yml down

# Logs
logs:
	docker compose -f docker-compose.infra.yml -f docker-compose.services.yml logs -f