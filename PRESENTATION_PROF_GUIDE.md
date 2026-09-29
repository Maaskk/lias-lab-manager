# Guide de presentation au professeur - LIAS Lab Manager

## Verdict final

La version finale couvre les besoins fonctionnels du cahier des charges : portail public, profils et statuts temporels, gouvernance, demandes d'adhesion, activites, documents, publications, messagerie, materiel, conventions, reunions/PV, recherche, notifications, calendrier, rapport annuel et tracabilite.

La distinction importante a expliquer est la suivante :

- **Statut scientifique** : permanent, associe, doctorant, retraite, ancien membre.
- **Role applicatif** : admin, directeur, vice-directeur, chef d'equipe, membre permanent, associe, doctorant.
- **Etat du compte** : actif, gele ou desactive.

Ainsi, une responsabilite peut changer sans perdre l'historique du membre.

## Preparation avant la seance

1. Ouvrir l'URL de production indiquee dans `PRESENTATION_CREDENTIALS.local.md`.
2. Garder ce fichier ouvert pour les comptes de demonstration.
3. Tester les connexions admin, directeur et doctorant avant d'entrer en salle.
4. Garder le depot GitHub et ce guide dans deux onglets separes.
5. En secours local : `docker compose up --build` depuis la racine du projet.

## Demonstration conseillee - 12 a 15 minutes

### 1. Portail public - 1 minute

Montrer l'accueil, les axes, les equipes, les projets, les partenaires, les publications et les evenements. Ouvrir aussi ICAIS 2025 ou ICISCT 2026. Expliquer qu'un visiteur peut consulter ces informations sans authentification.

### 2. Demande d'adhesion - 1 minute

Ouvrir `/rejoindre`, montrer le statut demande, l'etablissement, le laboratoire d'origine, les centres d'interet, la motivation et le CV. Expliquer que la demande reste `PENDING` jusqu'a la decision du directeur en mandat actif.

### 3. Profil et memoire temporelle - 1 minute

Se connecter comme membre permanent. Ouvrir **Profil**, modifier la biographie ou les centres d'interet, puis montrer l'upload de photo. Montrer les affiliations, les roles et les publications historises. Preciser que la date de naissance n'est jamais exposee dans l'annuaire public.

### 4. Administration des comptes - 1 minute

Se connecter comme admin. Ouvrir **Utilisateurs**. Montrer la creation d'un compte, le changement de role, le changement d'etat `ACTIVE/FROZEN/DISABLED` et la reinitialisation du mot de passe. Expliquer qu'un compte gele ou desactive ne peut plus se connecter.

### 5. Gouvernance - 1 minute

Ouvrir **Gouvernance**, **Mandats**, **Roles** et **Affiliations**. Montrer le directeur, le vice-directeur, les chefs d'equipe et leurs dates. Insister sur le fait qu'une nouvelle nomination ferme une periode mais ne supprime jamais l'ancienne.

### 6. Decision d'adhesion - 1 minute

Se connecter comme directeur, ouvrir **Adhesions**, puis accepter ou refuser une demande. A l'acceptation, choisir le statut membre et le role applicatif. Expliquer que seul le directeur du mandat actif peut prendre cette decision; l'admin technique peut consulter mais pas decider.

### 7. Activites, documents et calendrier - 1 minute

Montrer les conferences, seminaires et editions archivees. Ouvrir un evenement, sa discussion et ses documents. Montrer l'archive documentaire avec types et versions, puis le calendrier mois/semaine/jour qui rassemble evenements, reunions, mandats et conventions.

### 8. Publications du doctorant - 1 minute

Se connecter comme doctorant. Montrer que son menu est volontairement limite a **Profil**, **Publications** et **Notifications**. Ajouter une publication et revenir au profil pour montrer qu'elle apparait dans son historique. Essayer `/membres` pour illustrer le refus d'acces aux modules internes.

### 9. Messagerie - 1 minute

Revenir au membre permanent ou au directeur. Montrer les quatre canaux : global, direct entre deux utilisateurs, equipe et evenement. Envoyer un message direct puis ouvrir l'autre compte pour montrer la conversation persistante.

### 10. Materiel et equite - 1 minute

Montrer l'inventaire, les arrivages, les demandes avec justification, la validation/refus motive, les distributions et la liste des membres n'ayant encore rien recu. Expliquer que le stock et l'auteur de la distribution restent traces.

### 11. Memoire institutionnelle - 1 minute

Montrer les reunions et PV, les conventions et leurs documents, la recherche globale paginee et les notifications personnelles. Generer enfin le rapport annuel PDF depuis **Rapport annuel**.

### 12. Audit et conclusion - 1 minute

Revenir en admin et ouvrir **Audit**. Montrer les connexions, changements, decisions, uploads et generations de rapport. Conclure : l'application ne remplace pas les anciennes donnees; elle conserve les periodes et les actions pour constituer la memoire du laboratoire.

## Matrice complete du cahier des charges

| Exigence | Implementation a montrer | Etat |
|---|---|---|
| Site public du laboratoire | `/`, equipes, projets, partenaires, activites, publications | Fait |
| Categories d'utilisateurs | Roles et navigation differente pour admin, direction, permanent, associe, doctorant, retraite, ancien | Fait |
| Profil membre complet | `/profil`, photo, identite, etablissement, laboratoire, bio, interets, equipe, publications | Fait |
| Confidentialite | Date de naissance filtree; documents internes et uploads proteges | Fait |
| Donnees temporelles | Dates d'affiliation/embauche, periodes de roles, departs, retours et mandats | Fait |
| Gouvernance | Directeur, vice-directeur, chefs d'equipe et historique complet | Fait |
| Demandes d'adhesion | Formulaire public, CV, motivation, acceptation/refus, creation du compte | Fait |
| Retraite et depart | Gel/desactivation, fermeture de l'affiliation, reactivation sans perte d'historique | Fait |
| Evenements | Conferences, seminaires, ateliers, editions, organisateurs, archivage | Fait |
| Documents | Types, evenement/date, upload, telechargement protege, recherche, versions | Fait |
| Rapport annuel | PDF genere depuis membres, activites, publications, reunions, conventions, materiel | Fait |
| Publications | Ajout par membres et doctorants, auteurs, annee, equipe, rattachement au profil | Fait |
| Communication | Messages globaux, directs, par equipe et par evenement | Fait |
| Materiel | Arrivees, inventaire, distributions, auteur/date et vue d'equite | Fait |
| Besoins en materiel | Demande justifiee, decision direction, motif et historique | Fait |
| Conventions | Partenaires, pays, periodes, description et document archive | Fait |
| Reunions et PV | Ordre du jour, date, responsable et upload du PV | Fait |
| Recherche globale | Membres, documents, evenements et publications avec pagination | Fait |
| Notifications | Notifications personnelles et compteur non lu | Fait |
| Calendrier | Vues mois/semaine/jour; evenements, reunions, mandats, conventions | Fait |
| Tracabilite | Journal d'audit et conservation des historiques au lieu de suppression | Fait |

## Questions probables et reponses courtes

**Pourquoi separer statut, role et etat du compte ?**  
Parce qu'un membre permanent peut devenir directeur puis redevenir membre, tout en restant la meme personne. Le role est temporaire, le statut scientifique decrit son lien au laboratoire, et l'etat controle la connexion.

**Comment les droits sont-ils securises ?**  
Le frontend masque les modules interdits pour l'ergonomie, mais la vraie securite est dans Spring Security avec JWT et controles de roles sur chaque API.

**Pourquoi Flyway avec JPA ?**  
Flyway versionne les changements SQL de facon reproductible; JPA mappe ensuite les tables vers les entites Java.

**Ou est conservee la memoire du laboratoire ?**  
Dans les tables d'affiliations, roles, mandats, statuts, documents versions, distributions et audit. Les changements ferment ou ajoutent une periode au lieu d'effacer l'ancien etat.

**Comment le rapport annuel est-il produit ?**  
`ReportService` selectionne les donnees de l'annee, calcule les syntheses et genere un PDF A4 avec PDFBox, puis enregistre le fichier et sa reference en base.

**Que se passe-t-il si un utilisateur force une URL interdite ?**  
Le frontend affiche une page 403 et, meme s'il appelle directement l'API, Spring Security renvoie 403.

**Pourquoi deux hebergeurs ?**  
Vercel heberge le frontend React. Le backend est un service Java Spring Boot persistant avec PostgreSQL et stockage de fichiers; il est donc deploye comme conteneur, puis relie au frontend Vercel.
