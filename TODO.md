# TODO / pistes d'amelioration

Points non traites ou simplifies faute de temps, et ameliorations possibles :

## Backend
- [ ] Authentification / autorisation (explicitement hors perimetre du test,
      mais necessaire avant toute mise en production - le champ
      `requestedBy` est actuellement une simple saisie libre).
- [ ] Remplacer H2 en memoire par une vraie base persistante (PostgreSQL) en
      profil "prod" ; un profil `application-docker.yml` pourrait pointer
      vers le service `db` d'un `docker-compose` etendu.
- [ ] Pagination et tri cote serveur pour `/api/risk-limits` (actuellement
      tout est charge et pagine/trie cote client, ce qui ne passera pas a
      l'echelle avec un tres grand nombre de contreparties).
- [ ] Validation plus fine du CSV (encodage, doublons de `ricosCode` au sein
      d'un meme fichier, limites de taille de fichier configurables).
- [ ] Historisation des demandes de derogation deja traitees (endpoint
      `/api/derogations` existe mais n'est pas exploite par un ecran
      dedie cote frontend).
- [ ] Tests d'integration supplementaires sur les controllers (MockMvc) et
      sur `DerogationService` (limite manquante, depassement de 150 %).
- [ ] Gestion centralisee des erreurs (`@ControllerAdvice` global plutot
      qu'un `@ExceptionHandler` local au `DerogationController`).

## Frontend
- [ ] Tests unitaires Angular (Karma/Jasmine ou vitest)  sur les composants et
      notamment sur la logique de tri multi-colonnes du dashboard.
- [ ] Gestion des etats de chargement/erreur plus homogene (actuellement
      dupliquee dans chaque composant, pourrait etre factorisee).
- [ ] Debounce sur le filtre de nom si le volume de donnees augmente.
- [ ] Ecran de consultation de l'historique complet des demandes de
      derogation (approuvees/rejetees), pas seulement les demandes en
      attente.

## Infra / process
- [ ] Le `.gitlab-ci.yml` n'a pas ete execute sur un runner reel (non
      demande par l'enonce) ; a valider notamment le nom du dossier de
      sortie Angular (`dist/riskboard-frontend/browser`) qui peut varier
      selon la version exacte du builder utilise.
- [ ] Ajouter un stage de qualite de code (Checkstyle/SonarQube cote
      backend, ESLint cote frontend).
- [ ] Ajouter un healthcheck Docker sur le service backend avant de
      demarrer le frontend dans `docker-compose.yml`.
