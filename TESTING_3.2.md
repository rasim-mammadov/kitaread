# Leaf Reader 3.2 — Identity & Statistics Preview

## Implemented
- Statistics dashboard accessible from the library header chart icon.
- Total books, finished books, total recorded reading time.
- 14-day reading activity calendar, with recorded minutes grouped by local day.
- Current and longest daily reading streaks (at least 5 minutes per day).
- Last 6 months of completed books, using existing `completedAt` metadata.
- EPUB / PDF / CBZ counts with progress bars.
- A first reader passport card showing actual book and activity statistics.
- Preset themes: existing Light, Sepia, Dark, AMOLED plus Paper, Warm, Sage, Ocean, Midnight and Graphite.

## Data safety
- Same application ID and book data format as 3.1.
- No automatic editing or deletion of prior books.
- Stats use locally recorded sessions; no external service.
- Missing historical reading session data remains zero and is not inferred.
- Streaks may not count time split into sub-minute sessions if rounded away
  in UI, but the calculation itself aggregates millisecond values.

## Pending decisions / later phases
- User has not chosen a final brand name. **Leaf Reader** remains until selected.
- Genre-based titles (e.g. Space Traveler, Knight, Scientist) require reliable
  metadata and manual genre editing, planned for 3.3; no invented genres.
- A complete reader passport with earned badges/levels and selectable title
  follows actual genre implementation.
- No Custom Theme editor. User expressly rejected it.

## Test on device
1. Upgrade over 3.1 without clearing data; confirm saved books/positions.
2. Open the chart icon on the home page. Check all numbers against local books.
3. Read a book for 5+ minutes and background/return. Verify time and streak
   only after a session flush (closing or backgrounding the reader).
4. Test theme selection: Paper, Warm, Sage, Ocean, Midnight, Graphite.
5. Confirm imported library metadata and existing backup actions remain.
6. Verify EPUB typography, PDF zoom and CBZ page order unchanged.

GitHub Actions only verifies Kotlin compilation, local JVM tests and artifact
creation. It does not validate rendered output on the Samsung device.
