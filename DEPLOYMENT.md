# Fylvix — Production Deployment & Media Delivery Architecture

## 1. Media & CDN Pipeline

Fylvix strictly separates catalog metadata from video stream delivery:

```text
Client App (Android / Web)
  ├── Metadata Requests ──> Backend API (/api/movies, /api/tv, /api/search) ──> PostgreSQL + Redis Cache
  └── Playback Request  ──> Playback API (/api/player/:contentId/token)
                              └── Issues Short-Lived Signed Token (HMAC-SHA256 / Cloud CDN Signed URL)
                                    └── Edge CDN ──> HLS (.m3u8) / DASH (.mpd) Adaptive Bitrate Stream
```

- **Authorized Content Only**: Only streams with verified distribution rights (`isAuthorizedStream = true`) and valid signed tokens are served to the player.
- **No Exposed Storage Credentials**: Private S3/GCS bucket keys and encoder secrets remain strictly on the backend media signer service.

## 2. Building for Production

### Android APK / AAB Release
```bash
gradle :app:assembleRelease
gradle :app:bundleRelease
```

### Web Static / CDN Target (Flutter Web Reference)
```bash
flutter build web --release
```
Deploy static bundle with immutable asset caching (`Cache-Control: public, max-age=31536000, immutable`) and SPA/clean-URL rewrite rules.
