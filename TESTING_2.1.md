# Leaf Reader 2.1 Preview — test checklist

This preview is built by GitHub Actions but **has not been tested on a physical Android device**.

## EPUB
- Import a valid EPUB, open it, and select **Aa ⚙ → Səhifələmə**.
- Tap the visible right and left arrow buttons to advance a page at a time.
- Verify that a page turn stays within its chapter until reaching the chapter boundary.
- Switch to **Continuous scroll**, scroll, and reopen the book.
- Change font size, line spacing and theme; check that the same approximate reading position is restored.
- Use **≡** to open the chapter list and **⇥** to jump within the current chapter.
- Save **☆** bookmarks at different points in the same chapter and revisit them.
- Check that books containing long headings, images, embedded CSS and RTL text still paginate correctly.
- Verify offline operation and no external resource requests.

## PDF and CBZ
- Test 1x / 2x / 5x zoom, pinch zoom, pan and double tap.
- Switch page with edge buttons; confirm zoom navigation does not accidentally turn a page.
- Toggle CBZ right-to-left mode and Fit to Width.
- Inspect image clarity at high zoom: high-resolution re-render is **not yet implemented**.

## Android layout & data
- On Samsung/Android 15+, verify safe areas around status/gesture navigation bars.
- Verify reader settings dialog scrolls and its Apply button is reachable.
- Reinstall over v2.0 (do not clear app data) and check saved library/progress.
- Export and import metadata JSON; note that JSON does not include book files.
- Check progress and monthly statistics; EPUB progress is an approximation weighted by chapter file size.

## Current limitations
- CSS multi-column rendering is an EPUB pagination preview; it may not behave reliably for every EPUB layout.
- Book positions use a chapter + relative offset, **not EPUB CFI**.
- Total EPUB pages change with font size/viewport and are not used in the fixed-page statistics.
- Only compilation and artifact publishing are verified by the CI workflow, not touch interactions.
