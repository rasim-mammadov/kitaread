# Leaf Reader 3.3.3 — Library UX & Global Settings

## UX
- Main header displays only "Leaf Reader" with search and settings icons.
- Three menu buttons are below the title: Statistics, Bookmarks, Filters.
- Four always-visible status filters: Hamısı / Oxunur / Oxunmayıb / Bitirilib.
- Genre, format, collections are in a separately opened advanced filter sheet.
- Search field is hidden until Search icon is pressed.
- Lower-right Floating Action Button shows the LibraryAdd (book + plus) icon only; accessible description remains "Kitab əlavə et".
- Continue Reading shortcut appears for a book with recorded reading activity.
- Sorting options: Son oxunanlar, Əlavə edilmə tarixi, A–Z, Z–A, Progress.
- Finished books remain below other statuses for all sort options.

## Settings
- Appearance: ten built-in presets; app-wide Material colors. Reader defaults to follow the app; can use separate Reader Theme.
- Reading: pointers to in-reader typography, navigation, orientation and zoom controls.
- Library: sort preferences; list of hidden books with restore.
- Data & Backup: JSON export/import; actual book files are NOT included.
- About: app/version and supported formats.
- No Custom Theme editor.

## Deletion
- Hide: sets `hidden=true` on the book; keeps file and metadata.
- Remove from library: removes record but does not delete stored book bytes. This can leave an orphaned app-local file and should be used sparingly; hide is recommended for reversible removal.
- Delete: after a confirmation, attempts to delete app-owned book/cover files, extracted cache and metadata record.
- Existing metadata and other books are preserved.
- Deleted content cannot be restored by metadata-only JSON backup.

## Back navigation
- Book Reader back goes to library as in previous builds.
- Statistics / Settings / Bookmarks / Filters back goes to library.
- Back at root displays a confirmation dialog; cancel keeps the app open.
- Home/system app-switching gestures background the app normally.

## Manual Samsung Galaxy S25 Ultra checks
1. Update over 3.3.2 (do not clear storage). Existing books, EPUB settings, bookmarks and stats must remain.
2. Check header/status bar in portrait and landscape; menus must not overlap.
3. Check four compact status chips and hidden advanced genre/format/collection filters.
4. Search by title and author; close the search; reset filters after an empty result.
5. Test all ten global themes in Library, Statistics, Book Details and Settings.
6. Enable/disable Reader Theme follows app, then open an EPUB to check background/font.
7. Sort by activity; read for at least one second, leave reader, verify most recent and finished-last behavior.
8. Hide a book, restore from Settings. Make sure notes, bookmarks and the file survive.
9. Remove metadata record with retained file; verify removal dialog.
10. Delete a TEST book completely with confirmation; verify it no longer appears.
11. Back from Statistics goes to library. Back at library asks confirmation. Home gesture does not ask.
12. Export/import via Settings → Data & Backup; metadata should be consistent.
13. Confirm landscape PDF/CBZ/EPUB behavior and no regression on 3.3.2 orientation fixes.

CI validates Kotlin compilation, JVM unit tests and APK creation, not phone-specific UI functionality.
