# Fylvix — Admin Dashboard & Content Governance Guide

## 1. Admin Modules
The Fylvix Admin Panel provides end-to-end operational control over:
- **Dashboard & Analytics**: Aggregate views, play starts, average completion rate, top titles, popular genres, and retention metrics.
- **Movies & Series Management**: Full CRUD forms for titles, original/alternative titles, synopsis, release year, runtime, country, language, genres, certification, classification, cast, crew, director, writer, media sources, subtitles, audio tracks, and publish/unpublish toggles.
- **Seasons & Episodes**: Hierarchical season and episode management for TV Shows and Anime.
- **18+ / Adult Content Governance**:
  - Dedicated backend/admin toggle to enable or disable the 18+ category globally or per region.
  - Per-title publish/unpublish, homepage visibility, and search visibility controls.
  - No intrusive per-visit age popup; access is governed smoothly by backend classification configuration and profile settings.
- **Live TV Manager**: Add/edit authorized live channels, EPG current/next programs, stream URLs, and live status.
- **API Providers & Metadata Sync**: Replaceable adapter architecture for synchronizing global movie/TV/anime metadata into PostgreSQL without coupling the UI to a single third-party API.
- **Reports & System Logs**: Moderation queue for user-submitted content reports and immutable admin audit logs.
