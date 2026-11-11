# Leaf Reader 3.9 — Reading Journal & Knowledge

Branch: `feature/leaf-reader-3.9-reading-journal`
Base: 3.8 Reading Analytics & Gamification

## Features
- Central journal shows entries, reviews and existing quotes/notes across all books.
- Book selection filters the knowledge collection and enables writing a dated journal entry or personal review.
- Per-book journal entries and review data use the existing private metadata store; no migration of earlier annotations is necessary.
- Markdown and JSON knowledge exports use Android's document picker and do not contain the original book files.
- Deleting a journal entry requires a confirmation. Existing quotes/notes are not deleted from the global journal.

## Acceptance checklist
1. Upgrade from the latest 3.8 APK; confirm reading progress, annotations, bookmarks, themes, and statistics remain.
2. Open **Gündəlik** from Library. All books should be visible; select one book to write a journal entry and a review.
3. Reopen the app; the journal entry and review should persist.
4. Open the **Sitatlar və qeydlər** tab and verify old annotations are shown without duplication.
5. Export Markdown and JSON with Android's file picker. Open the saved files and check titles, reviews, journal entries and annotations.
6. Check AZ, RU and EN display strings; verify book titles and user-authored text are not translated.
7. Delete one journal entry, confirm removal; verify other entries and book files remain.
8. Test library/reader navigation and 3.8 analytics regression.
9. GitHub Actions: `testDebugUnitTest` and `assembleDebug` must both pass. Test on a real device before calling the version stable.

## Data guarantees
- No automatic cloud sync and no remote server.
- Knowledge export is separate from the existing metadata backup.
- Existing `annotations` arrays are read in place and are not rewritten.
