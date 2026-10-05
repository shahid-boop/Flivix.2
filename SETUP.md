# Fylvix — Setup & Development Guide

## 1. Environment Configuration

Copy `.env.example` or configure secrets via the **Secrets panel in AI Studio**:

```dotenv
API_BASE_URL=https://api.fylvix.global
METADATA_API_URL=https://metadata.fylvix.global
METADATA_API_KEY=FYLVIX_METADATA_PROVIDER_KEY_PLACEHOLDER
MEDIA_API_URL=https://media-cdn.fylvix.global
ANALYTICS_KEY=FYLVIX_ANALYTICS_KEY_PLACEHOLDER
```

> **Important**: Never commit real credentials or private storage signing keys to source control.

## 2. Project Architecture

The application follows Clean Architecture & MVVM with strict separation between UI, State/ViewModel, Repositories, Network Services, and Local Persistence (Room SQLite + PostgreSQL sync contract):

- `core/` — Constants, Config, Theme (Cinematic Obsidian & Crimson/Gold palette, Outfit + Plus Jakarta Sans typography), Routing, Networking, and Responsive Breakpoints.
- `data/` — Models (PostgreSQL-mirrored entities), Room DAO/Database, Retrofit API Service (`/api/...`), Metadata Provider Adapter, and FylvixRepository.
- `features/` — Modular screens and state holders for `auth`, `home`, `movies`, `tv`, `seasons`, `episodes`, `anime`, `live_tv`, `adult`, `search`, `details`, `player`, `watchlist`, `favorites`, `history`, `profile`, `settings`, `recommendations`, and `admin`.
- `shared/` — Reusable widgets (`FylvixAppBar`, `FylvixSidebar`, `FylvixBottomNavigation`, `HeroBanner`, `MovieCard`, `SeriesCard`, `EpisodeCard`, `ChannelCard`, `FilterPanel`, `VideoPlayer`, etc.).

## 3. Running & Building

### Android / Cross-Device Client
```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

### Web Target Reference (Flutter Web / Static CDN Target)
For hybrid web static hosting targets:
```bash
flutter run -d chrome
flutter build web --release
```
