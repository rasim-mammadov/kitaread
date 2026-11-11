# Leaf Reader 3.6 — Reading Experience

Branch: `feature/leaf-reader-3.6-reading-experience`

## Scope

- **Focus Mode:** immersive fullscreen while reading, toggle from the reader top toolbar or Settings. Tap the reader to reveal the controls. Restores Android bars on exit.
- **Screen brightness:** optional in-reader window brightness adjustment (5–100%); uses system brightness when off, restores the previous window value after closing the reader.
- **Reading session timer:** foreground elapsed time displayed in the reader bottom toolbar and settings. Resettable. Does **not** change the existing activity-based analytics ledger.
- **Per-book settings:** opt-in independent font, line spacing, alignment, mode, theme, orientation, swipe/page buttons, fit-width and smooth transition preferences. Stored in each book's existing metadata record, with global defaults when disabled.
- **Smooth EPUB and Markdown page transitions:** short optional animated within-chapter page scrolling. PDF and CBZ continue showing their previous rendered bitmap until the next page is available; no additional fade/recreate flicker added.
- **AZ / RU / EN:** new reader controls translated using the existing UI language system.
- Application id `az.leafreader` unchanged. Existing book metadata and 3.5 functionality retained.

## CI checks

GitHub Actions executes `:app:testDebugUnitTest` and `:app:assembleDebug`. Includes JVM tests in `ReadingClockTest`. CI build is **not** evidence of device behavior.

## Manual device test checklist

1. Upgrade **over** 3.5 on Samsung S25 Ultra; verify books, progress, bookmarks and notes persist.
2. Open EPUB and Markdown; switch Focus Mode on/off repeatedly, use back, and test status/navigation bars after returning to library.
3. With Focus enabled, tap the page to reveal/hide toolbar; tap fullscreen exit. Verify Android navigation remains accessible.
4. Enable custom brightness, adjust 5% and 100%, close reader; verify device brightness elsewhere is unaffected.
5. Open timer, read for several seconds, background the app, return. Background time must not be added. Reset timer and verify format.
6. Enable custom settings for Book A, change font, reading theme and scroll mode. Open Book B: it should retain global defaults. Reopen A: its choices should persist. Disable the per-book setting and ensure A follows global defaults again.
7. Test fit-width in PDF and CBZ with per-book settings on/off; ensure it updates the view immediately.
8. Toggle smooth page transitions for EPUB/Markdown within a chapter; ensure pagination and progress remain correct; test successive fast taps.
9. Rotate portrait/landscape in Focus Mode and use system back. Verify no stale brightness or hidden system bars.
10. Switch language AZ/RU/EN and confirm the new reader controls translate.
11. Verify previous PDF page-flicker fix persists, especially while rapidly turning pages.

## Known limitations

- The smooth animation applies to **scroll positions within EPUB/Markdown chapters**, not transitions between PDF/CBZ bitmaps.
- The timer counts visible foreground session time; existing reading statistics have independent anti-idle logic.
- EPUB/Markdown chapter transitions can still require a short loading period.
- Current PDF extraction remains raster-based; no PDF full-text extraction is added in 3.6.
