# KitaRead 4.3.1 — Compact, Pinned & Typography

Based on 4.1 successful APK; existing package ID and reader data are preserved.

## Implemented
- Library layout cycles List → Grid → Compact → List; persists across restarts
- Compact cover-first rows have separate tap targets for open, pin and details
- Pin/unpin supported from List, Grid and Compact views; pinned items move first but retain the selected secondary sort
- Settings → Library controls text size (11–22sp) and card density (Dense/Normal/Comfortable)
- Controls change only library UI, not EPUB typography or reading mode
- New AZ/RU/EN labels for pin and typography

## Test scenarios
1. On-device: switch all three layouts and open books in each view.
2. Pin books in List, Grid and Compact; restart, filter and change sort; pins persist.
3. Unpin: item returns to natural position under active sort.
4. Increase and decrease font size; check long titles and narrow screens.
5. Set density 0/1/2; ensure controls remain tappable with font scaling.
6. Multi-select Compact books and return to normal mode.
7. Verify prior CBZ/Webtoon, EPUB, PDF, cover images, annotation and backups.
8. CI must pass testDebugUnitTest and assembleDebug.

## Next branch: 4.3.2
- Pick replacement book cover from Android file picker; preserve and restore the original.
- Replace old branding with the new K-book emblem from the user's supplied artwork.
