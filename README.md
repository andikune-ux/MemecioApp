# Memecio — Smart Multimedia Player & Vault

Memecio is a modern Android multimedia player and private vault with a built-in scientific calculator disguise, built with **Kotlin** and **Jetpack Compose** (Material 3).

## Key Features

1. **Calculator Disguise & Vault Security**:
   - Operates as a fully functional standard and scientific calculator.
   - Enter PIN (default: `140399`) and tap `=` to unlock the main multimedia suite.
   - Quick Disguise action on every screen to instantly return to calculator mode.
   - Private media vault for sensitive content locked behind PIN authentication.

2. **Full Multimedia Playback (Media3 / ExoPlayer)**:
   - Supports MP4, WebM, MKV, MP3, AAC, FLAC, and HLS (`.m3u8`) live streams.
   - Full playback controls: Play/Pause, ±10s Seek, Seekbar, Aspect ratio modes (Fit, Zoom, Fill), Speed selector (0.5x to 2.0x), and Sleep timer.
   - Audio focus, volume, and mute toggles.

3. **Multiview Dual Streaming**:
   - Dual split-screen player capable of playing two streams or videos simultaneously.
   - Independent playback and audio controls per channel with a quick swap toggle.

4. **Sources & IPTV Streaming**:
   - Support for custom HLS live streams and direct media URLs.
   - Built-in M3U playlist parser for channel groups and logos.
   - Local device media picker integration.

5. **Playlists & Watch History**:
   - Custom playlists with creation, reordering, and batch playback.
   - Watch Later queue and persistent playback history with progress tracking.

6. **Diagnostics & Secret Keypad Codes**:
   - `000`: Secret Code Registry
   - `111`: Crash log & session history
   - `222`: Changelog & feature roadmap
   - `333`: System & hardware specifications
   - `444`: Test video player
   - `555`: Network connectivity info
   - `666`: Application permissions inspector
   - `777`: Storage & cache analyzer
   - `888`: App usage statistics
   - `999`: JSON backup export
   - `123`: Reset thumbnail cache
   - `456`: Database synchronization
   - `789`: Factory reset
   - `101`: Developer mode toggle
   - `103`: Player gesture guide
   - `104`: Display & grid mode switcher
   - `808`: Offline downloads list

## Tech Stack

- **UI**: Jetpack Compose (Material 3 Dark Theme)
- **Media Engine**: AndroidX Media3 ExoPlayer 1.5.0
- **Image Loading**: Coil Compose 2.7.0
- **Language**: Kotlin 2.0+
- **Build System**: Android Gradle Plugin 9.1.1, Gradle 9.3.1
