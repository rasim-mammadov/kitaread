# Leaf Reader 3.7 — Smart Library test plan

Branch: `feature/leaf-reader-3.7-smart-library`
Baseline: 3.6 Reading Experience (do not change or delete book contents).

## Automated checks
- GitHub Actions: `testDebugUnitTest` and `assembleDebug` must both pass.
- `SmartLibraryTest`: multi-field token search, author/series grouping, binary duplicate detection.
- `SmartLibrarySafetyTest`: case/width/whitespace normalization; empty query; equal titles with different bytes; identical bytes with different names; path traversal and missing files excluded.

## Manual device smoke tests
1. Upgrade an installed 3.6 APK to 3.7; confirm existing books, read positions, highlights, notes, covers, language and reader options remain.
2. Toggle List → Grid → List; restart app and verify the chosen layout persists. Open a book from both layouts.
3. Group by Author and Series; test missing metadata, alphabetical order, and switching between groups with active filters.
4. Search a title + author token, series name, collection and genre; confirm all active tokens are required, case insensitive; clear filters and search.
5. Select one book, select all visible filtered books, cancel and select again. Add selected books to an existing/new collection without duplicates. Verify books not visible under current filters remain untouched.
6. Hide selected books only after explicit confirmation; restore them in Settings. Confirm reading files still exist.
7. Import two identical book files under different names and one different-content book with the same title; inspect duplicate groups. Confirm no automatic deletion.
8. Switch AZ → RU → EN; inspect the library, grouping, selection, duplicate dialog and confirmation text for untranslated labels.
9. Background and foreground the app during duplicate scanning; confirm scan finishes or reports an error without crashing.
10. Recheck EPUB, PDF, CBZ and Markdown reading, 3.6 Focus Mode, brightness, reading timer and per-book settings.

## Release gate
3.7 is ready for phone testing only after a successful APK build. Mark stable only after manual steps pass on a real device. This plan is not evidence that those tests have been performed.
