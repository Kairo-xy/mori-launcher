# Mori Launcher

A forest-atmospheric Android home launcher with a quiet glass-and-moss visual system. The goal is a genuinely usable launcher, not a static concept screen.

## Current build

The current Compose build includes:
- Forest-gradient home surface with live date and clock
- Expandable Mori capsule with a real hand-off to the installed music app
- Searchable installed-app library with real app icons and launch actions
- App drawer filters for all, downloaded, and system apps
- Search by app label or package name; long-press opens Android App Info
- Pin up to eight installed apps from the drawer to a persistent home-screen shortcuts row
- Edit pinned shortcuts on the home screen: move each shortcut left/right or remove it; ordering is saved locally
- Functional Phone, Browser, Music, and Camera dock shortcuts
- Wallpaper picker and Android Settings entry points
- Device-local persistent preferences for frosted surfaces and dock labels
- Lock-screen concept preview (not a replacement for Android Keyguard)

The workspace is still early: shortcut ordering is editable and persisted, but freeform drag-and-drop, folders, dock ordering, and full grid placement are not implemented. Hosted widgets cannot yet be resized or freely placed. Pinned app shortcuts are stored locally and can be added or removed from the app drawer or the home-screen edit controls. Mori now has a real Android widget picker, provider configuration hand-off, locally persisted widget IDs, and live AppWidgetHostView rendering.

## Design and engineering references

Mori is an independent implementation. Its feature direction is informed by established open-source launcher projects:
- [Lawnchair](https://github.com/LawnchairLauncher/lawnchair) — launcher fundamentals, Pixel-style interaction patterns and customization.
- [ARK Launcher](https://github.com/jrs8205/ARK-launcher) — Compose-based app search, settings discoverability and widget-host direction.
- [YagniLauncher](https://github.com/T31n/YagniLauncher) — performance-first scope and avoiding unnecessary background work.

Mori now includes an adapted AppWidgetHost/provider-generation foundation derived from ARK Launcher. The adaptation is documented in `NOTICE-THIRD-PARTY.md`, and the upstream Apache License 2.0 text is included under `licenses/`. Other projects currently serve as architectural references; their source is not copied.

## Android boundary

Mori can be selected as the default home app. A normal third-party launcher cannot replace Android's secure Keyguard or bypass device authentication. The lock screen page is a concept preview only; Android remains responsible for device unlock security.

## Tech

- Kotlin
- Jetpack Compose + Material 3
- Minimum Android 8.0 (API 26)
- Java 17 toolchain

## Build

Open in Android Studio and run the `app` configuration, or run:

```bash
gradle :app:assembleDebug
```

GitHub Actions builds a debug APK on pushes to `main` and uploads it as the `MoriLauncher-debug-apk` artifact.

## Roadmap

- [x] Compose launcher shell
- [x] Forest-first visual system
- [x] Installed-app discovery, search and launch
- [x] Settings and lock-preview navigation
- [x] Persistent frosted-surface and dock-label preferences
- [x] Persistent pinned app shortcuts
- [x] Persistent pinned-shortcut ordering and home edit controls
- [ ] Freeform home layout, folders, and dock ordering
- [ ] Real media-session controls
- [x] Wallpaper picker entry point
- [x] AppWidgetHost lifecycle/provider-generation foundation
- [x] Widget provider picker, system configuration hand-off and AndroidView rendering
- [ ] Widget resizing, workspace placement and layout persistence
- [ ] Notification listener integration (opt-in)
- [ ] Gesture and folder interactions
- [ ] Accessibility, performance and real-device testing

## License

Mori's original code is MIT. The ARK-derived widget-host adaptation is additionally subject to Apache License 2.0; see [NOTICE-THIRD-PARTY.md](NOTICE-THIRD-PARTY.md) and [licenses/Apache-2.0-ARK-Launcher.txt](licenses/Apache-2.0-ARK-Launcher.txt).


See [docs/OPEN_SOURCE_RESEARCH.md](docs/OPEN_SOURCE_RESEARCH.md) for the launcher comparison, feature priorities, source-reuse policy, and device-performance guardrails.
