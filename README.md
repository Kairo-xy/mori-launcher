# Mori Launcher

A forest-atmospheric Android home launcher with a quiet glass-and-moss visual system. The goal is a genuinely usable launcher, not a static concept screen.

## Current build

The current Compose build includes:
- Forest-gradient home surface with live date and clock
- Expandable capsule interaction
- Searchable installed-app library with real app icons and launch actions
- Settings and lock-screen concept pages
- Four-slot dock concept and compact home widgets

Some home tiles, dock shortcuts, media controls, and settings are still visual prototypes. They do not yet control Android system services or persist preferences.

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
- [ ] Persistent home layout and preferences
- [ ] Real media-session controls
- [ ] Wallpaper picker
- [x] AppWidgetHost lifecycle/provider-generation foundation (picker, binding and rendering still to come)
- [ ] Notification listener integration (opt-in)
- [ ] Gesture and folder interactions
- [ ] Accessibility, performance and real-device testing

## License

Mori's original code is MIT. The ARK-derived widget-host adaptation is additionally subject to Apache License 2.0; see [NOTICE-THIRD-PARTY.md](NOTICE-THIRD-PARTY.md) and [licenses/Apache-2.0-ARK-Launcher.txt](licenses/Apache-2.0-ARK-Launcher.txt).
