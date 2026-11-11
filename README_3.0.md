# Leaf Reader 3.0 — Compose Redesign Preview

Native Android prototype rebuilt with Jetpack Compose / Material 3.

## Primary changes
- Library cards use larger text, cover thumbnails, progress and status filters.
- Reader chrome stays hidden while reading. Tap the page to reveal a top toolbar; use the **Aa** button to open a scrollable, in-book Material 3 bottom sheet.
- The sheet updates EPUB font size, line spacing, page/scroll mode, theme and optional navigation arrows.
- Navigation arrows are **off by default**; swipe is on by default.
- EPUB, PDF and CBZ open offline, reusing the v2 library records and book files.
- Android safe drawing insets protect reading surfaces from status and gesture bars.

## Data compatibility
- Same Android application ID: `az.leafreader`.
- Reads the existing `SharedPreferences("library")` items JSON and files in app-private storage.
- Stores new reader preferences separately in `leaf_v3_options`.
- No destructive migration. Avoid uninstalling or clearing app data when updating.

## Preview limitations
- EPUB rendering uses WebView CSS columns and is **not** yet a full EPUB CFI / Readium implementation. Punctuation, complex embedded CSS, RTL and some illustrations may paginate inconsistently.
- PDFs render via Android PdfRenderer and CBZ via bitmap decode. High-resolution tile re-render on zoom is not implemented.
- Library/statistics extras from v2 (notes, JSON backup, goals, charting) remain in saved data but are **not yet migrated to the new Compose screens**.
- Some EPUB covers are inferred from archive images and may be inaccurate.
- CI verifies compilation/build artifact only; the Samsung Galaxy S25 Ultra layout and swipe interaction require on-device testing.

## Smoke tests
1. Update over the existing 2.1 installation. Verify that books and completion dates remain.
2. Add new EPUB/PDF/CBZ books.
3. Open an EPUB, tap center, open **Aa**, adjust typography and switch Sepia/Dark.
4. Verify arrows are absent by default; enable via **Aa**; test swipe separately.
5. Test pinch zoom, double tap and pan in PDF/CBZ.
6. Confirm app controls are above Android status and gesture bar regions.
