# Deploiement de production

## Architecture retenue

- **Frontend React/Vite** : Vercel.
- **Backend Spring Boot 3 / Java 17** : conteneur Railway.
- **Base** : PostgreSQL geree sur Railway.
- **Fichiers** : volume persistant monte sur `/app/uploads`.

Vercel sert l'interface mondiale en HTTPS. Les routes `/api` et `/uploads` sont proxifiees vers le backend afin de conserver une URL unique pour le navigateur.

## Variables du backend

`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `INITIAL_ADMIN_PASSWORD`, `CORS_ORIGINS`, `PUBLIC_URL`, `COOKIE_SECURE`, `COOKIE_SAME_SITE`, `UPLOAD_DIR` et les variables SMTP optionnelles.

Les vraies valeurs ne sont jamais versionnees. `.env.example` contient seulement le modele local.

## Variables du frontend

Le frontend utilise `VITE_API_URL=/api` grace au proxy Vercel. Aucun secret n'est place dans le bundle React.

## Verification apres deploiement

1. `GET /api/public/health` retourne `UP`.
2. Les pages publiques et les routes profondes React s'ouvrent directement.
3. Les cinq comptes de demonstration se connectent.
4. Photo, document et rapport PDF restent disponibles apres un redeploiement.
5. Les messages directs et les notifications restent isoles par utilisateur.
6. Un doctorant recoit 403 sur les modules internes.
7. L'admin ne peut pas prendre la decision reservee au directeur actif.

Les URL finales et le mot de passe de demonstration sont dans `PRESENTATION_CREDENTIALS.local.md`, fichier local ignore par Git.
