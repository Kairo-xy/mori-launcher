# Mori Launcher — open-source research and integration notes

Research snapshot: 23 November 2026

This document records the projects reviewed, what Mori can learn from them, and the source/licence boundary for code reuse. A feature listed here is a research finding, not a claim that Mori already implements it.

## Best references

### ARK Launcher — primary implementation reference
- Repository: https://github.com/jrs8205/ARK-launcher
- Licence: Apache-2.0
- Stack: Kotlin, Jetpack Compose, Material 3
- Strong ideas: paged workspace, real drag-and-drop, dock and folders, searchable app drawer, widget catalog and resize, gesture mapping, icon packs, settings search, backup/restore.
- Mori integration: data/MoriWidgetHost.kt adapts the widget-host identity/lifecycle/provider-generation foundation. Attribution and the upstream licence text are in NOTICE-THIRD-PARTY.md and licenses/.
- Next useful modules to study/adapt: widget binding/configuration/rendering, workspace item model, app drawer actions, folder model, and backup format. Port only small platform-independent pieces that fit Mori's architecture; do not copy a module with unresolved dependencies.

### Lawnchair — launcher fundamentals and visual polish
- Repository: https://github.com/LawnchairLauncher/lawnchair
- Strong ideas: Launcher3-derived workspace behaviour, Pixel-style interaction patterns, Material You theming, icon customisation, global search.
- Mori integration: architectural reference only at this stage. Lawnchair is a large Launcher3-based codebase; copying isolated classes without their framework and dependency graph would be brittle.

### Neo Launcher — customization ideas
- Repository: https://github.com/NeoApplications/Neo-Launcher
- Strong ideas: theming, gestures, drawer categories, icon packs/shapes, hidden apps, backup/restore.
- Mori integration: reference for feature discovery and settings grouping. Check the exact branch's licence and dependencies before any code is copied.

### Milki Launcher — search-first interaction
- Repository: https://github.com/milkiLabs/launcher
- Licence: GPL-3.0 (per upstream README).
- Strong ideas: one command/search surface, user-defined search prefixes, pinning results, clipboard suggestions, inline and popup widgets.
- Mori integration: product inspiration only. Do not copy GPL source into Mori's MIT codebase unless the project intentionally changes its licensing and meets the GPL obligations.

### Barely — careful platform/API engineering
- Repository: https://github.com/friendlyglobal/barely
- Strong ideas: LauncherApps discovery and callbacks, system widget bind/configure flow, widget ID persistence, widget view lifecycle, keyboard/accessibility support, local-only preferences.
- Mori integration: engineering reference only until the exact source licence and compatibility are reviewed. Especially valuable is its warning not to repeatedly recreate AppWidgetHostView because hosted widget surfaces can lose input/focus state.

### Slate — distinct home-screen interaction
- Repository: https://github.com/braniik/slate
- Strong ideas: free-positioned icons, guide lines, list mode, a deliberately simple blank-canvas home.
- Mori integration: optional future layout mode; keep the forest home as Mori's default identity.

## Licence policy

- Mori's original code remains MIT.
- ARK-derived code remains additionally subject to Apache-2.0. Preserve its copyright, licence, and NOTICE attribution when adapting more files.
- Do not transplant GPL-3.0 code into the current MIT project casually. A GPL-derived combined work has corresponding distribution obligations.
- For every new upstream file: inspect its exact source path, licence, copyright header, dependencies, and relevant callers before adapting it. Record the upstream URL and modifications in NOTICE-THIRD-PARTY.md.
- Reimplementing a general idea or using Android's public APIs is not the same as copying source code.

## Mori implementation status

### Added in this iteration
- Persistent, device-local preferences for frosted surfaces and dock labels (MoriPreferences.kt).
- Real Android intent actions for Phone/Dialler, Browser, Music, Camera, Wallpaper picker, and Android Settings (MoriLauncherActions.kt). The OS chooses an installed handler; Mori does not pretend to provide its own media player or secure lock screen.
- Functional dock shortcuts and wallpaper entry point.
- App drawer filters for All / Downloaded / System apps.
- App drawer search matches both visible app labels and package names.
- Long-press an app to open its Android App Info page.
- Existing ARK-derived widget-host foundation remains wired to the Activity lifecycle; a full widget picker, bind/configure flow, placement persistence, and rendering are still outstanding.

### Next implementation order
1. Widget system: provider catalog, system bind-consent flow, optional provider configuration, AppWidgetHostView rendering via AndroidView, stable widget IDs and safe removal.
2. Workspace model: persistent pages, app shortcuts, dock order, folders, grid placement, drag/drop and edit mode.
3. App inventory: LauncherApps callbacks so install/update/uninstall changes refresh the drawer without restarting Mori; work-profile support where Android exposes it.
4. Search palette: app/package search first, then Android settings shortcuts and a local calculator; optional providers must remain privacy-respecting.
5. Personalisation: wallpaper-aware palette, icon shapes/packs, grid sizing, gesture settings, and settings search.
6. Quality: accessibility labels/touch targets, process-death restoration, low-memory testing, Android 8–latest API checks, and automated build/device tests.

## Device and performance guardrails

Mori should stay useful on lower-memory Android phones:
- Avoid permanent background services unless a user-enabled feature requires them.
- Load app icons lazily and cache bounded results.
- Keep app discovery off the UI thread as the catalog grows.
- Avoid decoding full-resolution wallpapers into memory.
- Persist only launcher metadata; never attempt to read widget provider contents.
- Treat notification access, contacts, usage access, and accessibility as optional features with clear explanations and explicit opt-in.
- Keep the Android system responsible for secure lock/unlock and sensitive permissions.