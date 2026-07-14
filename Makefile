MVNW=./master/mvnw
DOCKER_ENV=.env.docker
DOCKER_COMPOSE=compose.yml
RECETTE_ENV=.env.recette
RECETTE_COMPOSE=compose.recette.yml
PROD_ENV=.env.prod
PROD_COMPOSE=compose.prod.yml
RECETTE_URL=http://localhost:8080

.PHONY: dev-test quality docker-up docker-down docker-logs docker-build prod-up prod-down recette-up recette-it recette-down recette-logs

dev-test:
	cd master && ./mvnw -B test

quality:
	cd master && ./mvnw -B -Pquality verify

docker-build:
	docker compose --env-file $(DOCKER_ENV) -f $(DOCKER_COMPOSE) build

docker-up:
	docker compose --env-file $(DOCKER_ENV) -f $(DOCKER_COMPOSE) up --build

docker-down:
	docker compose --env-file $(DOCKER_ENV) -f $(DOCKER_COMPOSE) down

docker-logs:
	docker compose --env-file $(DOCKER_ENV) -f $(DOCKER_COMPOSE) logs -f

prod-up:
	docker compose --env-file $(PROD_ENV) -f $(PROD_COMPOSE) up -d

prod-down:
	docker compose --env-file $(PROD_ENV) -f $(PROD_COMPOSE) down

recette-up:
	docker compose --env-file $(RECETTE_ENV) -f $(RECETTE_COMPOSE) up --build

recette-it:
	cd master && ./mvnw -B -Pintegration -Drecette.base-url=$(RECETTE_URL) verify

recette-down:
	docker compose --env-file $(RECETTE_ENV) -f $(RECETTE_COMPOSE) down --volumes

recette-logs:
	docker compose --env-file $(RECETTE_ENV) -f $(RECETTE_COMPOSE) logs -f
