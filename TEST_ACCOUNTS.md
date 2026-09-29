# LIAS Lab Manager Test Accounts

These accounts are seeded on the first backend start when the database is empty.

Default local password comes from `INITIAL_ADMIN_PASSWORD` in `.env`.
The password is intentionally not committed to source control. Use the local presentation credentials file or the deployment secret configured by the project owner.

| Test goal | Role | Email |
|---|---|---|
| Full system administration, users, roles, settings, audit | Admin | `admin@lias.ma` |
| Adhesion decisions, annual reports, events, material validation | Director | `faouzia.benabbou@lias.local` |
| Direction backup and validation workflows | Vice-director | `abdessamad.belangour@lias.local` |
| Internal active member workflows, profile, messages, material requests | Permanent member | `permanent.demo@lias.local` |
| Limited collaboration access | Associate member | `associe.demo@lias.local` |
| Doctorant profile and publications-only access | Doctorant | `doctorant.demo@lias.local` |
| Team-level workflows | Team chief ISDIAC | `nawal.sael@lias.local` |

Recommended smoke tests:

1. Log in as admin, open `/admin/utilisateurs`, create a test user, change a role, reset a password.
2. Log in as director, open `/adhesions`, accept or reject a pending request.
3. Log in as permanent member, open `/profil`, change name/biography/interests, upload a profile photo.
4. Log in as permanent member and director, use `/messages` with the Direct channel between their user IDs.
5. Use Team and Event channels in `/messages` with the seeded ISDIAC team/event IDs.
6. Log in as doctorant and confirm only profile, publications, and notifications are available.
7. Open `/materiel` to see inventory, distributions, and members without material.
