# Mori Launcher

A forest-atmospheric Android home launcher inspired by the supplied reference screens. The forest look is the primary design; a graphite monochrome theme is an alternate.

## Project status

**Early foundation — Android Compose scaffold.** The repository is new. The first milestone establishes the app shell, a home-screen concept, a lock-screen-style preview, and an expandable Dynamic Island-inspired capsule. Device integration (real widgets, app drawer, media sessions, notification access, wallpaper selection, and launcher role) will be added incrementally and tested on real Android hardware.

## Important Android security boundary

Mori can become the default home app. A regular third-party app cannot replace Android's secure Keyguard or bypass device authentication. The in-app lock-screen experience is a visual companion/preview; actual unlock security remains owned by Android.

## Tech

- Kotlin
- Jetpack Compose + Material 3
- Minimum Android 8.0 (API 26), targeting API 35

## Build

Open the project in Android Studio (Hedgehog or newer), let Gradle sync, then run the `app` configuration on an Android device or emulator.

## Roadmap

- [x] Create dedicated repository
- [x] Initial Compose app shell
- [x] Forest-first home and lock-screen visual foundations
- [x] Expandable Dynamic Island-inspired capsule prototype
- [ ] Set up default-launcher role and app drawer
- [ ] Wallpaper picker and persistent layout preferences
- [ ] Real media-session integration
- [ ] Notification listener and notification cards (opt-in)
- [ ] Battery/device widgets and widget-host support
- [ ] Lock-screen companion polish and Android Keyguard-safe integration
- [ ] Accessibility, performance, and device testing

## License

MIT. See [LICENSE](LICENSE).
