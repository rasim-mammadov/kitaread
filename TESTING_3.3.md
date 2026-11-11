# Leaf Reader 3.3 — Personal Library preview

## Implemented
- Existing EPUB metadata (creator/subject) is parsed locally from OPF.
- New EPUB imports store author, metadata subjects and a conservative genre mapping.
- Previously imported EPUB books receive one-time non-destructive metadata enrichment.
- A user can change title, author, genre, reading status and a 0–5 personal rating from Book Details.
- Book collections can be created and attached/removed from each book. Library filters collections and genres.
- A book's bookmark is saved at chapter + relative position. The top bookmark icon adds/removes the current point.
- Bookmarks list is accessible both in the reader and from the library toolbar, with open/delete actions.
- Legacy integer chapter/page bookmarks are read and preserved when converting the list.
- Reader Passport unlocks genre-specific titles from unique finished library entries; title tiers at 1, 5 and 15 finished books. A chosen title is saved locally.
- This build retains 3.2 statistics/streak and all ten built-in themes. No Custom Theme editor.

## Safety and limitations
- EPUB OPF subjects are optional and often broad; genre can be incorrect. Users can override it manually.
- PDF and CBZ do not receive invented genre labels; manual editing is supported.
- Existing library files are not modified or removed.
- JSON metadata export includes bookmarks, collections, genre, author and ratings, but **not the actual book/cover files**.
- Bookmarks use relative chapter location, not precise EPUB CFI. Changing fonts may shift the bookmark.
- Collections are stored as tags on each book. Removing the last book from a collection removes that collection label.
- Full-text search, highlight & notes and specialized comics mode remain planned for v3.4.
- Successful CI verifies compilation, pure JVM tests and APK creation, not Samsung visual/touch behavior.

## Device test checklist
1. Install as update over v3.2 without clearing storage. Verify all books, progress, stats and backups remain.
2. Inspect a previously imported EPUB with OPF metadata. Confirm author and genre enrichment.
3. Edit genre manually, close, reopen. Check that the edit persists and filters refresh.
4. Add the same book to two collections; filter each; remove from one, and verify the other remains.
5. Change a book from 'Oxunur' to 'Bitirilib'. Check its completed date and reader passport.
6. Open EPUB, navigate mid-chapter, add a bookmark. Tap bookmark icon again to remove; add several positions.
7. Open 'Əlfəcinlərim' from reader and library. Tap bookmark and verify chapter+approx position.
8. Verify older bookmarks saved as integer chapter/page still display and can be removed.
9. Confirm PDF and CBZ bookmark add/jump/delete works and zoom is unaffected.
10. Change text size and theme; confirm reading position is approximately maintained and no crashes.
