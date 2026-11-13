# KitaRead v1.1 — Quality & Regression Plan
Date: 2026-11-13
Base: production v1.0, commit cb76e92a8ae5ec11c6cd1bf84808885a11388770
Development: feature/kitaread-v1.1-quality

## Safety constraints
- Never merge into production v1.0 during development.
- Keep applicationId `az.leafreader` and current storage keys unchanged.
- Never clear application data or uninstall an installed build before exporting a verified full backup.
- Preserve existing library entries, progress, bookmarks, notes, pinning, collections, covers and settings.
- Debug APK is not a signed production release.

## Automated gates (GitHub Actions)
- `gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon`.
- Workflow must upload `KitaRead-v1.1-quality-debug-apk`.
- No release approval until build/test results are confirmed. GitHub Actions is not a device test.

## Manual regression matrix
| ID | Format | Test | Expected | Status |
|---|---|---|---|---|
| R01 | PDF | Open a 1-page and a 200+-page PDF | All pages render with no crash | NOT RUN |
| R02 | PDF | Turn pages rapidly 30+ times | No blank frames, old page remains until next is ready | NOT RUN |
| R03 | PDF | Pinch zoom, pan, double tap, fit width | View remains responsive, expected fit retained | NOT RUN |
| R04 | EPUB | Open EPUB2 (NCX) and EPUB3 (nav.xhtml) | Correct named chapter list / TOC order | NOT RUN |
| R05 | EPUB | Scroll midway, close, force-stop, reopen | Same chapter and approximate reading fraction restored | NOT RUN |
| R06 | EPUB | Switch page/scroll modes, adjust font | Progress still meaningful and readable | NOT RUN |
| R07 | CBZ | Open 200+-page CBZ, jump back/forward | Numeric page order and fast page turns | NOT RUN |
| R08 | CBZ | RTL, double-page, webtoon, zoom | No reversed or missing spreads | NOT RUN |
| R09 | ALL | Rotate portrait/landscape mid-read | No crash; bookmark and position persist | NOT RUN |
| R10 | ALL | Fullscreen/focus mode, swipe, tap zones | Gestures and system bars behave correctly | NOT RUN |
| R11 | DATA | Upgrade installed v1.0 without clearing data | Library, progress, notes, bookmarks, covers remain | NOT RUN |
| R12 | DATA | Full ZIP export and restore to clean installation | Library assets, custom covers and metadata restored | NOT RUN |
| R13 | DATA | JSON export/import with conflicts | Existing metadata preserved under non-destructive policy | NOT RUN |
| R14 | UI | AZ/RU/EN across Library, Settings and Reader | No untranslated labels or mixed-language actions | NOT RUN |
| R15 | UI | Compact/Grid/List, light/dark, large font | No clipped toolbar, buttons, cover images | NOT RUN |
| R16 | DEVICE | Samsung phone Android upgrade install | ApplicationId compatible; no data loss; launch works | NOT RUN |

## Changes started
- v1.0 preserved as archive branch, development isolated.
- CI workflow now includes v1.1 branch and version `1.1.0-dev`.
- CBZ archive-entry name list cached with bounded, metadata-aware invalidation to avoid re-enumerating and re-sorting on each page turn.

## Known investigation areas
- PDF flicker/repaint: rendering currently keeps the previously decoded bitmap until the new page arrives; device regression needed before claiming solved.
- EPUB TOC: labels currently parse EPUB3 anchors and EPUB2 NCX; test malformed/nested entries and duplicate hrefs.
- EPUB restoring: stored chapter number and fractional scroll height; layout/font changes can alter exact restoration.
- Backups: test export/import independently, including custom cover files.
- Do not mark manual regressions PASS until a real install test is performed.

## Acceptance criteria
1. Automated unit tests and APK build are green.
2. R01–R16 PASS on representative real documents and a device.
3. No regression or data loss when upgrading from v1.0.
4. Release only via review/merge after acceptance.
