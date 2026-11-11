# KitaRead 4.1 — Comics & Manga Pro

Built on Leaf Reader 4.0 RC3 without changing Android applicationId `az.leafreader`. Existing books and metadata remain in the same local storage.

## Name and identity
- Public app name: **KitaRead**
- Launcher emblem: K in the shape of an open book, cropped from the user-supplied original artwork
- Library header: the supplied horizontal K-book mark and KitaRead name

## New CBZ modes
- Single Page: existing pinch/zoom, smoother transition
- Double Page: adjacent images in one spread (RTL reverses left/right placement)
- Webtoon: long vertical scrolling with an adjustable 0–40dp gap; visible pages are loaded on demand
- Reading Direction: LTR/RTL gesture and touch-zone direction
- Tap Zones: 22% on each edge turns pages, centre toggles reading controls
- Per-book settings: `comicMode`, `comicRtl`, `comicPageGap` stored in book metadata
- Archive pages are naturally ordered (2 before 10)

## Acceptance tests
1. Install over 4.0 RC3 and confirm books, progress, bookmarks, annotations, themes, backup and statistics survive renaming.
2. Confirm Android launcher and library header show **KitaRead** and the provided mark.
3. Open CBZ, change from Single Page to Double Page, turn forward/back and exit/reopen; selected mode must persist for that book.
4. Test odd page counts (1, 3, 5) and ensure last spread does not crash.
5. Enable Manga RTL: spread right page is first, swipe and edge taps have mirrored direction; progress still increases in natural page order.
6. Switch to Webtoon, test vertical scrolling, gap slider and switching back to single; verify content and progress.
7. Open a CBZ containing `1.jpg`, `2.jpg`, `10.jpg` and confirm numerical ordering.
8. Confirm PDF and EPUB modes and prior 4.0 RC3 fixes remain.
9. Test large image CBZ, landscape mode, 100-page file and rapid navigation.
10. GitHub Actions must pass `testDebugUnitTest` and `assembleDebug`, and upload `KitaRead-4.1-comics-manga-pro-debug-apk`.

## Constraints
- Automatic comic panel detection is **not implemented** in 4.1. Existing manual pinch/double-tap zoom remains.
- Android device UI tests and actual reading feel remain unverified until the user installs the APK.
