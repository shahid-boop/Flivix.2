# Fylvix — Backend REST API Specification

Base URL: `${API_BASE_URL}` (default `/api`)

## Authentication (`/api/auth`)
- `POST /api/auth/register` — Create user account and default profile.
- `POST /api/auth/login` — Authenticate user; returns short-lived access JWT + refresh token.
- `POST /api/auth/logout` — Revoke current session token.
- `POST /api/auth/refresh` — Rotate refresh token and issue new access JWT.
- `POST /api/auth/forgot-password` — Dispatch password reset token to verified email.
- `POST /api/auth/reset-password` — Complete password reset with token.

## Movies (`/api/movies`)
- `GET /api/movies` — Query parameters: `page`, `limit`, `genre`, `country`, `language`, `year`, `rating_min`, `runtime_max`, `certification`, `quality`, `sort`.
- `GET /api/movies/:id` — Full movie metadata, cast, crew, production companies, trailers, similar & recommended movies.
- `GET /api/movies/trending` — Trending movies (`window=day|week`).
- `GET /api/movies/popular` — Globally popular movies.
- `GET /api/movies/latest` — Newly released and recently added movies.
- `GET /api/movies/top-rated` — Highest rated movies by weighted score.
- `GET /api/movies/upcoming` — Upcoming theatrical & streaming releases.

## TV Shows & Series (`/api/tv` & `/api/episodes`)
- `GET /api/tv` — Filterable series directory (`status=airing_today|on_air|completed`, `genre`, `country`, `language`, `year`).
- `GET /api/tv/:id` — Full series metadata, seasons summary, cast, crew, trailers, similar & recommended series.
- `GET /api/tv/popular` — Popular TV shows.
- `GET /api/tv/trending` — Trending TV shows.
- `GET /api/tv/latest` — Latest series.
- `GET /api/tv/:id/seasons` — All seasons for a series.
- `GET /api/tv/:id/seasons/:season` — Season details + ordered episode list.
- `GET /api/episodes/:id` — Individual episode metadata, runtime, air date, next/previous episode pointers.

## Anime (`/api/anime`)
- `GET /api/anime` — Query parameters: `category=popular|trending|latest|ongoing|completed|movies|series`, `audio=sub|dub`, `genre`.
- `GET /api/anime/:id` — Anime details, characters, voice cast, seasons, episodes, and similar anime.

## Live TV (`/api/live`)
- `GET /api/live` — Authorized live channels filtered by `category` (`News`, `Sports`, `Entertainment`, `Movies`, `Music`, `Kids`, `Documentary`, `International`) and `country`.
- `GET /api/live/:id` — Channel details, current/next EPG program schedule, and authorized live HLS manifest endpoint.

## 18+ / Adult Content (`/api/adult`)
- `GET /api/adult` — Returns adult movies and series only when backend policy, admin publishing status, and regional rules permit access.
- `GET /api/adult/:id` — Adult content details, classification metadata, and authorized playback manifest.

## Global Search & Taxonomy
- `GET /api/search` — Unified search across movies, series, seasons, episodes, anime, people, actors, directors, genres, countries, languages, and 18+ content (when enabled).
- `GET /api/search/suggestions` — Low-latency prefix autocomplete suggestions.
- `GET /api/genres` — Complete genre taxonomy.
- `GET /api/countries` — Complete country catalog.
- `GET /api/languages` — Complete audio/subtitle language catalog.
- `GET /api/people` — Cast, directors, writers, and voice actors.

## User Profile, Watchlist, History & Recommendations
- `GET /api/users/me` — Authenticated user account and active profiles.
- `PATCH /api/users/me/profile` — Create/update/switch user profile.
- `GET / PUT / DELETE /api/users/me/watchlist` — Manage My List items.
- `GET / POST /api/users/me/history` — Sync playback position, duration, percentage watched, and completed status.
- `GET / PUT / DELETE /api/users/me/favorites` — Manage favorite titles.
- `GET / PATCH /api/users/me/preferences` — Sync theme, language, autoplay, subtitle, audio, and 18+ visibility preferences.
- `GET /api/recommendations` — Personalized recommendations weighted by history, favorites, watchlist, ratings, country, and language.

## Authorized Playback (`/api/player`)
- `POST /api/player/:contentId/token` — Validates entitlement, region, and classification; issues short-lived signed playback token.
- `GET /api/player/:contentId` — Exchanges signed token for temporary HLS/DASH manifest URLs, multi-bitrate variants, subtitle VTT tracks, and audio tracks.
