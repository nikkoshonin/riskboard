# RiskBoard

Application de suivi des limites de risque des contreparties et de gestion
des demandes de derogation, destinee aux equipes Sales d'une banque.

- **Backend** : Java 21, Spring Boot 4, Maven, H2 (base en memoire)
- **Frontend** : Angular (standalone components), TypeScript

## Structure du depot

```
riskboard/
├── backend/            # API Spring Boot
├── frontend/           # Application Angular
├── sample-data/        # Jeu de donnees CSV pour tester l'import
├── docker-compose.yml  # Lance backend + frontend en conteneurs
└── .gitlab-ci.yml       # Pipeline CI (build + tests backend et frontend)
```

## Prerequis

- Java 21
- Maven 3.9+ (ou utiliser le wrapper `mvn` fourni par votre IDE)
- Node.js 24+ et npm
- Angular CLI (`npm install -g @angular/cli`) — optionnel, `npx ng` fonctionne aussi
- Docker et Docker Compose — optionnel, pour lancer l'ensemble en conteneurs

## Lancer en local (sans Docker)

### 1. Backend

```bash
cd backend
mvn spring-boot:run
```

L'API demarre sur `http://localhost:8080`. La base H2 est en memoire : les
donnees sont reinitialisees a chaque redemarrage. La console H2 est
accessible sur `http://localhost:8080/h2-console` (JDBC URL :
`jdbc:h2:mem:riskboard`, user `sa`, pas de mot de passe).

### 2. Frontend

```bash
cd frontend
npm install
npm start
```

L'application demarre sur `http://localhost:4200`. Le serveur de
developpement Angular est configure (`proxy.conf.json`) pour rediriger les
appels `/api/**` vers `http://localhost:8080`, donc aucune configuration CORS
supplementaire n'est necessaire cote client en developpement.

## Lancer avec Docker Compose

```bash
docker-compose up --build
```

- Backend : `http://localhost:8080`
- Frontend : `http://localhost:4200` (servi par Nginx, qui proxy `/api/**`
  vers le conteneur backend)

## Tester l'import CSV

Un jeu de donnees pret a l'emploi se trouve dans `sample-data/risklimits.csv`.
Depuis l'ecran "Import CSV" du frontend, selectionnez ce fichier puis cliquez
sur "Importer". Le resume de l'import (lignes en succes / en erreur) s'affiche
directement dans l'interface.

Vous pouvez aussi appeler l'API directement :

```bash
curl -F "file=@sample-data/risklimits.csv" http://localhost:8080/api/import/csv
```

## Lancer les tests

### Backend

```bash
cd backend
mvn test
```

Couvre notamment :
- Le calcul du niveau d'alerte (GREEN / ORANGE / RED) pour differents taux
  d'usage, y compris les bornes exactes (70 % et 90 %)
- Le calcul de l'exposition agregee par secteur
- L'import CSV (upsert, isolation des lignes en erreur)

### Frontend

```bash
cd frontend
npm test
```

## Principaux endpoints de l'API

| Methode | URL | Description |
|---|---|---|
| GET | `/api/risk-limits` | Liste de toutes les limites de risque (avec usageRate et alertLevel calcules) |
| GET | `/api/risk-limits/exposure-by-sector?limitType=CREDIT` | Exposition agregee par secteur pour un type de limite |
| GET | `/api/risk-limits/counterparties` | Liste des contreparties (pour le formulaire de derogation) |
| POST | `/api/import/csv` | Import (multipart) d'un fichier CSV de contreparties/limites |
| GET | `/api/derogations/check-limit?counterpartyId=1&limitType=CREDIT` | Verifie qu'une limite existe et retourne le montant max autorise pour une derogation |
| POST | `/api/derogations` | Cree une demande de derogation |
| GET | `/api/derogations/pending` | Liste des demandes en attente |
| PUT | `/api/derogations/{id}/approve` | Valide une demande |
| PUT | `/api/derogations/{id}/reject` | Rejette une demande |

## Notes sur les versions

Le projet est ecrit avec les
dernieres versions stables accessibles (Spring Boot 4.1.0 / Angular 22).
J'ai utilisé claude.ai pour générer une première version du frontend que j'ai ensuite modifié pour corriger les bugs ainsi que les migrations de kit.

## Logique du pipeline `.gitlab-ci.yml`

Deux stages (`build`, `test`), chacun avec un job backend et un job
frontend, declenches uniquement si les fichiers du dossier correspondant ont
change (`rules: changes`). Le cache Maven (`.m2/repository`) et
`node_modules` sont mis en cache par branche pour accelerer les executions
successives. Les rapports JUnit sont publies en artifacts pour s'integrer a
l'interface GitLab.
