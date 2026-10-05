# Fylvix — Security & Content Policy Architecture

## 1. Authentication & Session Security
- Passwords hashed on the backend using Argon2id.
- Short-lived JWT access tokens paired with rotating HttpOnly/secure refresh tokens stored in the `sessions` table.
- Role-Based Access Control (`user`, `moderator`, `admin`) enforced on all `/api/admin/*` endpoints.

## 2. Signed Media URLs & Rights Enforcement
- Client applications never receive permanent raw storage bucket URLs or credentials.
- Playback requests call `/api/player/:contentId/token`, which verifies content publication status, regional licensing, classification access, and rights authorization before issuing a time-limited token (`exp = now + 900s`).

## 3. Secret Management
- Secrets are defined as placeholders in `.env.example` and injected via the **Secrets panel in AI Studio** (`BuildConfig`).
- Database credentials, signing keys, and third-party provider master keys must reside exclusively on the backend server.
