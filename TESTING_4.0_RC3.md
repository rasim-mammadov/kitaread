# Leaf Reader 4.0 RC3 — Reader stability regression tests

Base: 4.0 RC2 successful build `ce94b82`.

## Reported issues and fixes

1. CBZ: choppy smooth page turns → bounded next-page prefetch, sampled image decode and 210ms crossfade of displayed bitmap.
2. PDF: smooth page turns did not work → same bitmap crossfade and next-page prefetch. Preserve existing controls, zoom and pinch navigation.
3. EPUB: original smooth scroll remains unchanged.
4. EPUB global dark theme: fixed nested book text/background CSS can override reader style → inject high-priority, text-fill override CSS at the end of the body; preserve artwork.
5. Album/grid view: book cover and book title now have explicit tap navigation independent of the `Ətraflı` action; selection mode still selects books.

## Android acceptance tests
- Upgrade over 4.0 RC2 with existing data intact.
- With an EPUB, choose a light book-only theme, switch to global Dark/AMOLED, exit and reopen repeatedly. Body text, hyperlinks and headings must remain legible.
- Test Night/Midnight/Graphite and Light/Ocean/Sage; verify embedded EPUB illustrations remain visible.
- CBZ turn 30 pages rapidly, toggle smooth page transitions on and off, zoom/pan, rotate and test RTL page order.
- PDF turn 30 pages, check no white flash, both smooth off and on, and back/next control operation.
- In album/grid view tap book cover and title, verify book opens; tap `Ətraflı`, verify menu opens without launching book; test multi-select.
- Test large CBZ images, portrait/landscape rotation, low-memory reopening and single-page books.
- CI requires `:app:testDebugUnitTest` and `:app:assembleDebug` success.
- Real device visual smoothness and theme contrast remain unverified until tested on device.
