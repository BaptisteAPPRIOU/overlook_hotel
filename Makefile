MVNW=./master/mvnw
RECETTE_ENV=.env.recette
RECETTE_COMPOSE=compose.recette.yml
RECETTE_URL=http://localhost:8080

.PHONY: dev-test quality recette-up recette-it recette-down recette-logs

dev-test:
	cd master && ./mvnw -B test

quality:
	cd master && ./mvnw -B -Pquality verify

recette-up:
	docker compose --env-file $(RECETTE_ENV) -f $(RECETTE_COMPOSE) up --build

recette-it:
	cd master && ./mvnw -B -Pintegration -Drecette.base-url=$(RECETTE_URL) verify

recette-down:
	docker compose --env-file $(RECETTE_ENV) -f $(RECETTE_COMPOSE) down --volumes

recette-logs:
	docker compose --env-file $(RECETTE_ENV) -f $(RECETTE_COMPOSE) logs -f
