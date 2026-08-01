# LIAS Lab Manager

LIAS Lab Manager regroupe les opérations courantes d'un laboratoire de recherche dans une application web en français.

Le projet couvre la gestion des membres, des équipes, des mandats, des publications, des événements, du matériel, des réunions, des conventions et des demandes d'adhésion. Il contient également une messagerie interne, un journal d'audit et la génération du rapport annuel en PDF.

## Architecture

```text
Navigateur
    |
    v
React + Nginx
    |
    v
Spring Boot REST API
    |
    v
PostgreSQL
```

| Composant | Technologies |
|---|---|
| Frontend | React 18, Vite, Tailwind CSS, Axios, Zod |
| Backend | Java 17, Spring Boot 3, Spring Security, JPA, Flyway |
| Données | PostgreSQL 16 |
| Authentification | JWT court, refresh token en cookie HTTPOnly |
| Documents | Upload local contrôlé et rapports PDF avec PDFBox |
| Déploiement local | Docker Compose et Nginx |

## Fonctions principales

- annuaire des membres et profils par rôle
- équipes de recherche, affiliations et mandats
- publications, projets, partenaires et événements
- calendrier, notifications et messagerie
- inventaire du matériel et demandes d'attribution
- réunions, procès-verbaux et conventions
- demandes d'adhésion avec circuit de décision
- administration des utilisateurs, rôles et paramètres
- journal d'audit des opérations sensibles
- rapport annuel PDF pour la direction

## Démarrage avec Docker

```bash
cp .env.example .env
```

Modifiez au minimum `POSTGRES_PASSWORD`, `JWT_SECRET` et `INITIAL_ADMIN_PASSWORD`, puis lancez les services:

```bash
docker compose up --build
```

L'application est disponible sur `http://localhost`. Nginx sert le frontend et transmet `/api` et `/uploads` au backend.

Pour arrêter les conteneurs sans supprimer la base:

```bash
docker compose down
```

## Démarrage sans Docker

### Backend

```bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/lias_lab
export DB_USER=lias
export DB_PASSWORD=lias
export JWT_SECRET=replace_with_a_random_secret_of_at_least_64_characters
export INITIAL_ADMIN_PASSWORD=replace_before_first_start
export CORS_ORIGINS=http://localhost:5173
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

Le serveur Vite écoute sur `http://localhost:5173`.

## Tests

```bash
cd backend
mvn test
```

```bash
cd frontend
npm ci
npm test
npm run build
```

## Comptes de démonstration

La base vide reçoit un jeu de données local au premier démarrage. Tous les comptes utilisent la valeur de `INITIAL_ADMIN_PASSWORD`.

| Rôle | Email |
|---|---|
| Administrateur | `admin@lias.ma` |
| Directeur | `faouzia.benabbou@lias.local` |
| Vice-directeur | `abdessamad.belangour@lias.local` |
| Membre permanent | `permanent.demo@lias.local` |
| Membre associé | `associe.demo@lias.local` |
| Doctorant | `doctorant.demo@lias.local` |

Ces comptes servent uniquement aux tests locaux.

## Données publiques LIAS

Le jeu initial reprend des informations publiques de [lias.ma](https://lias.ma/): présentation du laboratoire, équipes, membres publiés, événements, partenaires et publications visibles.

Les adresses personnelles non publiées ne sont pas collectées. Des adresses locales de démonstration sont utilisées à leur place.

Le script suivant télécharge les ressources publiques utilisées par l'application:

```bash
./scripts/fetch-lias-assets.sh
```

## Sécurité

- les secrets de déploiement restent dans `.env`, qui est ignoré par Git
- les routes internes exigent une session valide
- les opérations d'administration vérifient les rôles
- les uploads contrôlent le type et la taille des fichiers
- les réponses Nginx et Spring Security ajoutent les principaux en-têtes HTTP de sécurité
- les actions sensibles alimentent le journal d'audit

Avant un déploiement réel, remplacez tous les mots de passe de démonstration, utilisez un stockage de secrets et placez l'application derrière TLS.
