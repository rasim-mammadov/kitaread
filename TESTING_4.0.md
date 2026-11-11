# Leaf Reader 4.0 — Release Candidate QA

Branch: `feature/leaf-reader-4.0-stable-release`
Baseline: 3.9 success at `582f235`. Version code 20, versionName 4.0-rc1.

## Automated gate
- GitHub Actions must pass both `testDebugUnitTest` and `assembleDebug`.
- Check artifact name `LeafReader-4.0-release-candidate-debug-apk`.
- CI success confirms build/tests only; it does not certify a stable release.

## Device validation (Samsung Galaxy S25 Ultra)
1. Install over existing 3.9 without uninstalling. Verify all books, covers, progress, settings, collections, journal, notes, highlights, bookmarks and statistics.
2. In Settings → Data & Backup choose **Full backup (ZIP)**, save a copy to a user-selected location. Verify ZIP includes `metadata.json`, each original `books/...` item and optional `covers/...` entries.
3. Add a fresh book after the backup. Restore from ZIP and verify existing library remains intact while restored books appear separately with their original progress and notes.
4. Test a ZIP with invalid or missing book paths, duplicate ZIP entry names and oversized file; restore should reject it without removing existing files.
5. Confirm legacy metadata-only JSON export/import still works. Full ZIP backup does not replace this option.
6. Switch AZ/RU/EN, test large system fonts and display sizes, TalkBack descriptions, button tap targets, light and dark themes.
7. Stress-test libraries of 100, 500 and 1000 books for launch, scrolling, switching Grid/List, search, sorting, grouping and duplicate detection.
8. Test PDF, EPUB, CBZ and Markdown including focus mode, brightness, session timer and per-book settings.
9. Verify no network permission or backend dependency is required.
10. Test background/foreground during file import/export, low storage, rotation and process restart.

## Important safety limits
- Backup import never overwrites existing books; duplicate logical books can result from importing the same archive more than once.
- Each archive entry is limited to 200 MiB, total extracted data to 500 MiB, and book count to 20,000.
- ZIP restore reuses metadata but saves restored book bytes under fresh names, preventing path collisions.
- Real-device backup/restore and performance tests are **not yet performed**. Do not label this candidate stable before they pass.
