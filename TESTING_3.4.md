# Leaf Reader 3.4 Preview — Advanced Reading

## Delivered in this preview
- EPUB offline full-book search across extracted XHTML chapters, with chapter names and excerpts.
- Debounced text queries, case-insensitive matching, and a cap of 80 displayed hits.
- Jump from result to chapter; WebView built-in find highlights matching text in the opened chapter.
- Quotes and personal notes stored locally by book/chapter with stable unique IDs.
- Edit, delete and revisit annotations.
- Support notes for PDF/CBZ by page, though text search is EPUB-only.
- 3.3.4 sort and shared/independent theme controls carried forward.

## Explicit limitations
- Full EPUB CFI text selection and persistent inline highlights are **not implemented**.
  Saved excerpts are quote cards in the notes panel, not in-page colored selection spans.
- Search results open the matching chapter, and WebView finds the first matching term;
  multiple matches in one chapter do not yet jump to the exact selected instance.
- PDF text search/OCR is not provided; image-only PDFs need a different pipeline.
- HTML search is approximate and uses the rendered plain-text extraction;
  it is not an EPUB structural index.
- Saved JSON metadata backups include annotations, but **do not include book files**.
- A long-press selection workflow and proper CFI anchors are candidates for 3.4.1.

## Manual device QA (Samsung Galaxy)
1. Upgrade from 3.3.4 without clearing application data; confirm EPUB/PDF/CBZ still open.
2. In EPUB, open controls and choose Search; search for a word in a later chapter.
3. Tap a hit; ensure the matching chapter opens and the term is highlighted by WebView Find.
4. Save quote from search hit. Check that the note belongs to the hit's chapter.
5. Add a manual note to an EPUB; edit, reopen, delete; verify persistence after app restart.
6. Add a note to a PDF page and reopen that page from its note.
7. Confirm earlier bookmarks, reader position, typography, dark theme and scroll controls work.
8. Export JSON and verify it contains the annotations field; existing book bytes stay unchanged.

## Deferred 3.3 exploration
- Collections / genre filtering UX and sort behavior on larger libraries.
- Reader identity, achievements, book cards and additional community-inspired ideas.
- Branding/name selection for v4.
- Keep 3.3.3 and 3.3.4 branches unchanged; do not discard their backlog.
