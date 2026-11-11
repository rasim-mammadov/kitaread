# Leaf Reader 4.0 RC2 — UI/UX acceptance checklist

Based on 4.0 RC1. This release is not stable until validated on an Android device.

1. Statistics: Cards and sections have consistent visible vertical spacing.
2. Achievements: Each badge is in its own bordered card; locked awards show a pale white castle with a lock; unlocked awards show a bright gold castle and star.
3. Daily goal: tap edit, enter any integer 5–240, adjust with −/+, save and restart; the same value returns. Invalid or empty entries cannot be saved.
4. Monthly goal: enter 1–30 books via dialog, decrease and increase, save and restart.
5. Reader passport: old guidance text is absent; earned achievement count (including 0) appears; share creates a PNG which opens Android Share Sheet. Verify AZ/RU/EN and test Instagram/WhatsApp sharing.
6. Empty library: headline remains; explanatory subtitle is gone. Reset-filter action remains available when filters hide existing books.
7. Journal: dedicated Library button and screen are removed. **No stored journal, quotes, annotations or reviews are deleted**; legacy data remains in backup for safety.
8. Grouping: author/series grouping selector and headers are removed; sorting and filters still work.
9. Book details menu: each action has a visible border, left-aligned icon/text; permanent deletion still requires a separate confirmation.
10. Settings: Reading entry removed; per-book reader Aa settings unchanged.
11. Regression: EPUB, PDF, CBZ, Markdown, bookmark, notes, stats, goal persistence, ZIP full backup/restore and language switching.
12. Accessibility: large fonts, screen reader, and portrait orientation.

CI: `testDebugUnitTest` and `assembleDebug` must both succeed; artifact expected `LeafReader-4.0-RC2-ui-refinement-debug-apk`.
