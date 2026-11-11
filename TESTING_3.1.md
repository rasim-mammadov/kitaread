# Leaf Reader 3.1 — Stability checklist

Scope: protect existing library data, improve reading position persistence,
capture reading activity, and validate EPUB/PDF/CBZ behavior. No new branding,
streak UI, achievements or custom themes in this phase.

## Automated checks
GitHub Actions runs:
`gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon`

Unit tests cover scroll fractions, finished-book progress bounds and limiting
idle/foreground session time. These are **not** on-device UI tests.

## On-device smoke test (Samsung Galaxy S25 Ultra)
1. Upgrade directly over 3.0.3 without clearing app storage.
2. Confirm existing EPUB, PDF, CBZ library entries and their reading positions.
3. Open an EPUB with long chapters, mixed font sizes and a TOC. Check layout,
   line spacing, alignment, chapter navigation and remembered position after
   changing font and leaving/reopening.
4. Switch EPUB continuous scroll and page-step modes; verify the screen does not
   jump to the wrong chapter.
5. Open PDF containing photos, pinch to zoom, double tap, pan and navigate.
   Verify safe areas and avoid accidental page changes while zooming.
6. Open CBZ, test portrait and landscape, confirm sequential page order, zoom
   and left/right behavior.
7. Leave a book open, return home for several minutes, then reopen it. Background
   time should not increase readingMs; active foreground intervals are capped to
   five minutes between touch events.
8. From the library export JSON. Read it back to confirm schemaVersion=3,
   includesBookFiles=false, and that readingSessions and notes are present.
9. Import the same JSON. Existing books must not be duplicated or overwritten.
10. Test import on a different installation: missing original EPUB/PDF/CBZ files
    must be reported rather than silently creating broken library entries.

## Data model
- Existing `SharedPreferences("library")` remains the source of truth.
- Per-book fields added: `readingMs`, `readingSessions[]`, `startedAt`,
  `lastReadAt`.
- Time is an approximation based on active foreground intervals; leaving a page
  untouched for over five minutes does **not** count as unlimited reading time.
- Metadata JSON backup does **not** include actual EPUB, PDF, CBZ or cover files.
  A full book-file archive is a separate planned feature.

## Release gate
Do not call this build device-tested without completing the smoke test.
