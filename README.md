# LIAS Lab Manager

Application web complète pour gérer le laboratoire LIAS : membres, équipes, mandats, demandes d'adhésion, événements, documents, publications, matériel, réunions/PV, conventions, notifications, audit et rapport annuel PDF.

## Fonctionnalités incluses

- Backend Spring Boot 3 / Java 17 avec REST API, Spring Security, JWT access token + refresh cookie.
- Frontend React 18 + Vite + TailwindCSS en français.
- PostgreSQL + Flyway + JPA/Hibernate.
- Upload local dans `uploads/` avec validation type/taille 10MB.
- Rapport annuel PDF généré avec Apache PDFBox.
- Audit log sur les opérations importantes.
- Espace public, espace membre, espace directeur et panneau admin.
- Pages d'erreur `/403`, `/404`, `/500`.
- Données de démonstration chargées automatiquement au premier démarrage.

## Prérequis

- Docker + Docker Compose, ou :
- Java 17+
- Maven 3.9+
- Node 18+
- PostgreSQL 15+

## Option A — Lancer avec Docker

Depuis le dossier du projet :

```bash
cp .env.example .env
# Modifier au minimum JWT_SECRET et POSTGRES_PASSWORD dans .env
docker compose up --build
```

Accès :

- Frontend : http://localhost
- Backend API : http://localhost/api
- PostgreSQL : localhost:5432

Le conteneur frontend Nginx sert l'application React et proxy toutes les requêtes `/api` et `/uploads` vers le backend. Le backend n'est pas exposé directement sur Internet dans la configuration Docker finale.

## Option B — Lancer manuellement

### 1. Base PostgreSQL

```bash
createdb lias_lab
psql -c "CREATE USER lias WITH PASSWORD 'lias';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE lias_lab TO lias;"
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

Variables utiles :

```bash
export DB_URL=jdbc:postgresql://localhost:5432/lias_lab
export DB_USER=lias
export DB_PASSWORD=lias
export JWT_SECRET=replace_with_a_real_random_secret_of_at_least_64_characters
export CORS_ORIGINS=http://localhost:5173
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Accès dev : http://localhost:5173

## Premier accès

Au premier démarrage sur une base vide, l'application crée les comptes internes avec le mot de passe défini dans `INITIAL_ADMIN_PASSWORD`.

| Rôle | Email |
|---|---|
| Admin | `admin@lias.ma` |
| Directeur | `faouzia.benabbou@lias.local` |
| Vice-directeur | `abdessamad.belangour@lias.local` |
| Membre permanent actif | `permanent.demo@lias.local` |
| Membre associé | `associe.demo@lias.local` |
| Doctorant | `doctorant.demo@lias.local` |
| Responsables d’équipe | adresses `@lias.local` créées depuis les profils publics |

Pour une vraie mise en production, changez les mots de passe depuis l'administration après la première connexion.

Voir aussi `TEST_ACCOUNTS.md` pour les scénarios de test complets : admin, directeur, membre actif, associé, doctorant, messages directs, messages d’équipe, profil/photo, matériel et adhésions.

## Routes principales

### Public

- `/` : page d'accueil
- `/equipes`
- `/equipes/:id`
- `/projets`
- `/partenaires`
- `/activites`
- `/activites/:id`
- `/publications`
- `/publications/:id`
- `/rejoindre`
- `/icais-2025`
- `/icisct-2026`
- `/login`

### Portail authentifié

- `/dashboard`
- `/recherche`
- `/profil`
- `/profil/:id`
- `/membres`
- `/membres/:id`
- `/evenements`
- `/evenements/nouveau`
- `/evenements/:id`
- `/documents`
- `/documents/upload`
- `/calendrier`
- `/messages`
- `/notifications`
- `/materiel`
- `/materiel/demandes`
- `/materiel/demandes/liste`
- `/reunions`
- `/reunions/nouvelle`
- `/conventions`
- `/conventions/nouvelle`
- `/rapport-annuel`
- `/adhesions`

### Admin

- `/admin`
- `/admin/utilisateurs`
- `/admin/membres`
- `/admin/equipes`
- `/admin/gouvernance`
- `/admin/mandats`
- `/admin/roles`
- `/admin/affiliations`
- `/admin/materiel`
- `/admin/audit`
- `/admin/parametres`

## Sécurité et déploiement

- `JWT_SECRET` est obligatoire en production et doit contenir au moins 64 caractères.
- Les routes internes exigent un JWT valide. Les routes de décision, rapport annuel, gouvernance, rôles et affiliations sont limitées aux rôles autorisés.
- Les headers HTTP de sécurité sont activés côté Spring Security et côté Nginx : CSP, HSTS, frame options, referrer policy et XSS/content-type protections.
- Les uploads sont stockés dans le volume Docker `uploads_data` et servis via `/uploads`.
- Le frontend utilise `VITE_API_URL=/api` en Docker pour fonctionner derrière le proxy Nginx.
- Pour une première installation propre, utilisez `docker compose down -v` seulement si vous voulez effacer la base existante.

## Structure

```text
lias-lab/
├── backend/
│   ├── src/main/java/ma/lias/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   ├── src/main/resources/db/migration/
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── docker-compose.yml
└── .env.example
```

## Seed data

Le fichier `DataLoader.java` insère automatiquement au premier démarrage :

- 1 admin
- 1 directeur
- 1 vice-directeur
- 4 chefs d’équipe
- 4 équipes
- 19 membres/comptes de démonstration
- mandats passés et mandat courant
- publications, événements, réunions/PV, conventions, matériel, demandes, notifications et audit

## Génération du rapport annuel

Connectez-vous comme directeur ou admin, puis allez sur :

```text
/rapport-annuel
```

Cliquez sur **Générer PDF**. Le fichier est créé dans :

```text
uploads/reports/
```

et servi via :

```text
/uploads/reports/rapport_annuel_YYYY.pdf
```

## Notes techniques

- Les comptes `FROZEN` et `DISABLED` ne peuvent pas se connecter.
- Les dates de naissance sont masquées aux utilisateurs non-admin dans l'API membres.
- Les opérations de création, modification, décision, upload, changement de statut et génération alimentent `audit_log`.
- Le refresh token est stocké dans un cookie HTTPOnly sur `/api/auth`.
- Le frontend utilise un access token en mémoire locale pour simplifier l'expérience de démo.
- La recherche globale `/recherche` interroge membres, documents, événements et publications.
- Les PV de réunions et documents de conventions peuvent être archivés depuis leurs pages de détail.

## Import des données publiques du site officiel LIAS

Cette version est préchargée avec les données publiques disponibles sur `https://lias.ma/` :

- présentation du laboratoire, contact, chiffres clés ;
- équipes ISDIAC, SIMA, SDTIC, ILIAS ;
- direction et responsables d’équipes ;
- membres listés publiquement ;
- publications récentes affichées publiquement ;
- événements ICAIS 2025, Journée Doctorale, ICISCT 2026 ;
- partenaires publics ;
- lien vers le programme ICAIS 2025 PDF et logo officiel.

Les emails personnels des membres n’étant pas publiés sur le site officiel, l’application crée des emails internes de démonstration `@lias.local`. Gardez-les ou remplacez-les par les vraies adresses si le laboratoire vous les donne officiellement.

Pour télécharger localement les assets publics visibles sur le site officiel :

```bash
./scripts/fetch-lias-assets.sh
```

Cela télécharge :

- `backend/uploads/public/Logo-LIAS-01.png`
- `backend/uploads/public/Program_ICAIS25.pdf`

Si Docker a déjà initialisé PostgreSQL avec l’ancienne seed, supprimez le volume avant de relancer pour charger ces nouvelles données :

```bash
docker compose down -v
docker compose up --build
```


## Final public website upgrade

This package includes the final public LIAS website upgrade: real public LIAS data, official logo, ICAIS'25 PDF program, ICISCT 2026 page, searchable program page, smooth navigation, live clock/date display, dark/light mode and custom error pages. See `FINAL_VERSION_NOTES.md` for details.
