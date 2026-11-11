# KitaRead 4.3.2 — Custom Covers & Branding

Base: KitaRead 4.3.1, including Compact layout, pinning and Library typography.

## Implemented
- Per-book **Change cover** action in the bordered Details menu, using Android image picker.
- Local sampled image import as compressed JPEG inside the app's existing covers directory.
- Original cover path is preserved on first customization; subsequent replacements do not overwrite that original path.
- **Restore original cover** appears for customized books.
- The previous KitaRead emblem is removed; launcher and Library toolbar now use the newly supplied open-book/K artwork, traced into an Android vector.
- Existing Android applicationId and book metadata are preserved.
- Full ZIP backup includes customized cover images using existing backup logic.

## Acceptance checklist
1. Upgrade over 4.3.1 and confirm the library, Compact/Grid/List layouts, pinning and notes are present.
2. In each layout, open book Details, choose Change cover, and select a valid image; cover updates immediately in the library.
3. Exit/reopen: changed image persists. Replace again with a second image, then Restore original: original extracted thumbnail returns.
4. For an imported book with no cover, restore returns to the placeholder.
5. Reject unsupported/invalid pictures and confirm original metadata is not modified.
6. Check new K-logo in both Android launcher and library toolbar on light/dark themes.
7. Validate AZ/RU/EN labels and large accessibility text.
8. Export full ZIP backup and restore on another installation; custom cover bytes must survive.
9. GitHub Actions must pass testDebugUnitTest and assembleDebug and upload KitaRead-4.3.2-covers-branding-debug-apk.
