# Leaf Reader 3.3.2 — Rotation stability

Changes:
- Android activity handles orientation/screen-size changes without discarding the active native and Compose reader screens.
- Currently open book path persists through saved instance state as a backup to activity reuse.
- EPUB chapter and fractional reading position use saveable Compose state.
- PDF and CBZ ZoomPageView receive size change callbacks and recalculate fit on rotation; existing loaded bitmap stays visible as the next page is rendered.
- In Reader → Aa → Ekran istiqaməti: Avtomatik, Şaquli, Üfüqi.
- Landscape toolbar hides the lengthy book title to leave room for controls.
- Previous 3.3.1 fixes retained.

On-device checklist for Samsung S25 Ultra:
1. Open PDF on page 5; rotate portrait→landscape→portrait several times. Verify it stays on the same page and remains visible throughout.
2. Pinch zoom a PDF/CBZ and rotate; verify fit, pan and new image boundaries.
3. Open EPUB halfway through a chapter, rotate; verify same chapter and approximate reading location; line spacing and selected theme remain.
4. Open Aa, switch each orientation mode, verify reader controls and bottom sheet remain usable.
5. Return to library from landscape and verify system auto-rotation is restored.
6. Check phone status bar and camera cutout padding.
7. Confirm old library, bookmarks, reading sessions and collection metadata are intact.

CI tests only verify build and JVM unit tests, not actual sensor rotation or device-specific layout.
