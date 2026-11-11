package az.leafreader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/** UI language does not mutate book files, metadata or persistent status identifiers. */
val LocalUiLanguage=compositionLocalOf {"az"}

object UiLanguage {
    val choices=listOf("az","ru","en")
    private val dictionary=mapOf(
        "Settings" to Pair("Настройки","Settings"),
        "Appearance" to Pair("Оформление","Appearance"),
        "Reading" to Pair("Чтение","Reading"),
        "Library" to Pair("Библиотека","Library"),
        "Data & Backup" to Pair("Данные и резервные копии","Data & Backup"),
        "About" to Pair("О приложении","About"),
        "Qlobal və oxuma temaları" to Pair("Темы приложения и чтения","App and reader themes"),
        "Oxuma parametrləri" to Pair("Настройки чтения","Reading preferences"),
        "Sıralama və gizli kitablar" to Pair("Сортировка и скрытые книги","Sorting and hidden books"),
        "Export / Import" to Pair("Экспорт / импорт","Export / Import"),
        "Tətbiq məlumatları" to Pair("Информация о приложении","App information"),
        "Kitab axtar" to Pair("Поиск книги","Search books"),
        "Kitab əlavə et" to Pair("Добавить книгу","Add book"),
        "Statistika" to Pair("Статистика","Statistics"),
        "Oxu statistikası" to Pair("Статистика чтения","Reading statistics"),
        "Əlfəcinlər" to Pair("Закладки","Bookmarks"),
        "Əlfəcinlərim" to Pair("Мои закладки","My bookmarks"),
        "Filtrlər" to Pair("Фильтры","Filters"),
        "Filtrləri sıfırla" to Pair("Сбросить фильтры","Reset filters"),
        "Kitabxanam" to Pair("Моя библиотека","My library"),
        "Kitab və müəllif axtar..." to Pair("Поиск по книгам и авторам...","Search books and authors..."),
        "Hamısı" to Pair("Все","All"),
        "Oxunur" to Pair("Читаю","Reading"),
        "Oxunmayıb" to Pair("Не прочитано","Not started"),
        "Bitirilib" to Pair("Прочитано","Finished"),
        "Yarımçıq saxlanılıb" to Pair("Отложено","Paused"),
        "Son oxunanlar" to Pair("Недавно читали","Recently read"),
        "Əlavə edilmə tarixi" to Pair("Дата добавления","Date added"),
        "Progress" to Pair("Прогресс","Progress"),
        "Sıralama" to Pair("Сортировка","Sort"),
        "Burada hələ kitab yoxdur" to Pair("Здесь пока нет книг","No books here yet"),
        "EPUB, PDF və ya CBZ əlavə et" to Pair("Добавьте EPUB, PDF, CBZ или Markdown","Add EPUB, PDF, CBZ or Markdown"),
        "Axtarış və filtrlərə uyğun kitab tapılmadı" to Pair("Книги по запросу не найдены","No books match your filters"),
        "Oxumağa davam et" to Pair("Продолжить чтение","Continue reading"),
        "Kitab ayarları" to Pair("Настройки книги","Book options"),
        "Kitab detalları və status" to Pair("Сведения о книге и статус","Book details and status"),
        "Kitabxanadan gizlət" to Pair("Скрыть из библиотеки","Hide from library"),
        "Kitabxanadan çıxar (fayl qalsın)" to Pair("Убрать из библиотеки (сохранить файл)","Remove from library (keep file)"),
        "Kitab və məlumatlarını tamamilə sil" to Pair("Удалить книгу и все данные","Delete book and all data"),
        "Kitab həmişəlik silinsin?" to Pair("Удалить книгу навсегда?","Delete book permanently?"),
        "Kitabxanadan çıxarılsın?" to Pair("Убрать из библиотеки?","Remove from library?"),
        "Həmişəlik sil" to Pair("Удалить навсегда","Delete permanently"),
        "Çıxar" to Pair("Убрать","Remove"),
        "Bağla" to Pair("Закрыть","Close"),
        "Geri" to Pair("Назад","Back"),
        "Çıx" to Pair("Выйти","Exit"),
        "Ləğv et" to Pair("Отмена","Cancel"),
        "Saxla" to Pair("Сохранить","Save"),
        "Əlavə et" to Pair("Добавить","Add"),
        "Kitabxanaya qayıt" to Pair("Назад в библиотеку","Back to library"),
        "Növbəti" to Pair("Далее","Next"),
        "Əvvəlki" to Pair("Назад","Previous"),
        "Növbəti səhifə" to Pair("Следующая страница","Next page"),
        "Əvvəlki səhifə" to Pair("Предыдущая страница","Previous page"),
        "Səhifə" to Pair("Страница","Page"),
        "Fəsil" to Pair("Глава","Chapter"),
        "Fəsillər" to Pair("Главы","Chapters"),
        "Ekran istiqaməti" to Pair("Ориентация экрана","Screen orientation"),
        "Avtomatik" to Pair("Автоматически","Automatic"),
        "Şaquli" to Pair("Портретная","Portrait"),
        "Üfüqi" to Pair("Альбомная","Landscape"),
        "Qlobal temanı izlə" to Pair("Использовать тему приложения","Follow app theme"),
        "Oxuma teması · 10 hazır seçim" to Pair("Тема чтения · 10 вариантов","Reading theme · 10 presets"),
        "Tema" to Pair("Тема","Theme"),
        "Temalar" to Pair("Темы","Themes"),
        "Oxuma temaları" to Pair("Темы чтения","Reading themes"),
        "Hazır temalardan seç. Seçim bütün kitablarda saxlanılır." to Pair("Выберите тему. Она сохранится для всех книг.","Choose a preset. It applies to all books."),
        "App Theme — bütün ekranlar" to Pair("Тема приложения — все экраны","App theme — all screens"),
        "Reader Theme tətbiq temasını izləsin" to Pair("Тема чтения следует теме приложения","Reader follows app theme"),
        "Kitab üçün ayrıca tema" to Pair("Отдельная тема для чтения","Separate reader theme"),
        "Şrift ölçüsü" to Pair("Размер шрифта","Font size"),
        "Sətirarası məsafə" to Pair("Межстрочный интервал","Line spacing"),
        "Mətnin düzülüşü" to Pair("Выравнивание текста","Text alignment"),
        "Soldan" to Pair("По левому краю","Left"),
        "İki kənara" to Pair("По ширине","Justify"),
        "Ortadan" to Pair("По центру","Center"),
        "Sağdan" to Pair("По правому краю","Right"),
        "Oxuma rejimi" to Pair("Режим чтения","Reading mode"),
        "Səhifələmə" to Pair("Страницы","Pages"),
        "Continuous scroll" to Pair("Непрерывная прокрутка","Continuous scrolling"),
        "Swipe ilə səhifə çevirmə" to Pair("Листать свайпом","Swipe to turn pages"),
        "Sağ-sol düymələri göstər" to Pair("Показывать кнопки перелистывания","Show page buttons"),
        "Fit to Width" to Pair("По ширине экрана","Fit to width"),
        "Qeydlər və sitatlar" to Pair("Заметки и цитаты","Notes and quotes"),
        "Kitabda axtar" to Pair("Поиск по книге","Search in book"),
        "Kitab daxilində axtarış" to Pair("Поиск в книге","Search in book"),
        "Söz və ya ifadə" to Pair("Слово или фраза","Word or phrase"),
        "Fəsilə keç" to Pair("Перейти к главе","Go to chapter"),
        "Qeyd / sitat saxla" to Pair("Сохранить заметку / цитату","Save note / quote"),
        "Şəxsi qeyd" to Pair("Личная заметка","Personal note"),
        "İşarələnəcək mətn (istəyə bağlı)" to Pair("Выделяемый текст (необязательно)","Highlighted text (optional)"),
        "Fəsili aç" to Pair("Открыть главу","Open chapter"),
        "Kitab detalları" to Pair("Сведения о книге","Book details"),
        "Kitabın adı" to Pair("Название книги","Book title"),
        "Müəllif" to Pair("Автор","Author"),
        "Oxu statusu" to Pair("Статус чтения","Reading status"),
        "Janr" to Pair("Жанр","Genre"),
        "Bütün janrlar" to Pair("Все жанры","All genres"),
        "Format" to Pair("Формат","Format"),
        "Kolleksiya" to Pair("Коллекция","Collection"),
        "Şəxsi kolleksiyalar" to Pair("Мои коллекции","My collections"),
        "Hələ kolleksiya yoxdur" to Pair("Коллекций пока нет","No collections yet"),
        "Yeni kolleksiya" to Pair("Новая коллекция","New collection"),
        "Dəyişiklikləri saxla" to Pair("Сохранить изменения","Save changes"),
        "Hələ əlfəcin əlavə etməmisən" to Pair("Закладок пока нет","No bookmarks yet"),
        "Ətraflı filtrlər" to Pair("Расширенные фильтры","Advanced filters"),
        "Kitabları göstər" to Pair("Показать книги","Show books"),
        "Gizlədilmiş kitablar" to Pair("Скрытые книги","Hidden books"),
        "Geri qaytar" to Pair("Восстановить","Restore"),
        "Bitirilmiş kitablar aşağıda saxlanılır." to Pair("Прочитанные книги показаны внизу.","Finished books appear at the bottom."),
        "Yalnız metadata: kitab faylları daxil deyil." to Pair("Только метаданные: файлы книг не включены.","Metadata only: book files not included."),
        "Export JSON" to Pair("Экспорт JSON","Export JSON"),
        "Import JSON" to Pair("Импорт JSON","Import JSON"),
        "Oxu təqvimi · son 14 gün" to Pair("Календарь чтения · 14 дней","Reading calendar · last 14 days"),
        "Yalnız bu cihazdakı oxu məlumatları" to Pair("Данные о чтении только с этого устройства","Reading data from this device only"),
        "Bir günün aktiv sayılması üçün ən az 5 dəqiqə oxu tələb olunur." to Pair("Активным считается день с 5 минутами чтения.","An active day requires at least 5 minutes of reading."),
        "Son 6 ay · Bitirilmiş kitablar" to Pair("Последние 6 месяцев · Прочитанные книги","Last 6 months · Finished books"),
        "Kitabxana tərkibi" to Pair("Состав библиотеки","Library breakdown"),
        "Oxucu pasportu" to Pair("Паспорт читателя","Reader passport"),
        "Qazandığın titullar" to Pair("Полученные звания","Unlocked titles"),
        "Janra uyğun titul üçün janrı müəyyən edilmiş kitabı bitir." to Pair("Завершите книгу с указанным жанром, чтобы получить звание.","Finish a book with a genre to unlock a title."),
        "Dil" to Pair("Язык","Language"),
        "Azərbaycan dili" to Pair("Азербайджанский","Azerbaijani"),
        "Rus dili" to Pair("Русский","Russian"),
        "İngilis dili" to Pair("Английский","English"),
        "Dil seçimi" to Pair("Выбор языка","Language"),
        "Sistem dili" to Pair("Язык системы","System language"),
        "Oxu parametrləri" to Pair("Настройки чтения","Reading settings"),
        "Oxuma ayarları" to Pair("Настройки чтения","Reading settings"),
        "Kitabın öz dili dəyişdirilmir." to Pair("Язык содержимого книги не изменяется.","Book content language is not changed."),
        "Dark" to Pair("Тёмная","Dark"),
        "Light" to Pair("Светлая","Light"),
        "Sepia" to Pair("Сепия","Sepia"),
        "Paper" to Pair("Бумага","Paper"),
        "Warm" to Pair("Тёплая","Warm"),
        "Sage" to Pair("Шалфей","Sage"),
        "Ocean" to Pair("Океан","Ocean"),
        "Midnight" to Pair("Полночь","Midnight"),
        "Graphite" to Pair("Графит","Graphite"),
        "AMOLED" to Pair("AMOLED","AMOLED"),
        "3.3.3 Preview · Offline EPUB / PDF / CBZ" to Pair("Версия 3.5 · EPUB / PDF / CBZ / Markdown офлайн","3.5 Preview · Offline EPUB / PDF / CBZ / Markdown"),
        "Axtarışı bağla" to Pair("Закрыть поиск","Close search"),
        "Parametrlər" to Pair("Настройки","Settings"),
        "Məlumatlar və ehtiyat nüsxə" to Pair("Данные и резервные копии","Data & Backup"),
        "Oxuma və görünüş" to Pair("Чтение и оформление","Reading & Appearance"),
        "Tətbiq haqqında" to Pair("О приложении","About app"),
        "Dil və region" to Pair("Язык и регион","Language and region"),
        "Görünüş" to Pair("Оформление","Appearance"),
        "Oxuma" to Pair("Чтение","Reading"),
        "Kitabxana" to Pair("Библиотека","Library"),
        "Məlumatlar" to Pair("Данные","Data"),
        "Haqqında" to Pair("О приложении","About"),
        "Leaf Reader-dən çıxmaq istəyirsən?" to Pair("Выйти из Leaf Reader?","Exit Leaf Reader?"),
        "Kitabxanan və oxuma mövqeyin saxlanılacaq." to Pair("Библиотека и позиция чтения будут сохранены.","Your library and reading position will be saved."),
        "Qlobal Reader Theme seçimi Appearance bölməsindədir." to Pair("Общая тема чтения находится в разделе «Оформление».","Global reader theme is available in Appearance."),
        "Şrift, sətirarası məsafə, ekran istiqaməti və zoom kitabın daxilindəki Aa menyusundan dəyişdirilir." to Pair("Размер шрифта, интервал, ориентация и масштаб настраиваются через Aa внутри книги.","Font size, spacing, orientation and zoom are adjusted from Aa inside a book."),
        "Kitabxanadakı tema oxuyucuda da tətbiq edilir." to Pair("В читалке используется тема библиотеки.","Reader uses the library theme."),
        "Kitab üçün ayrıca tema seçilib." to Pair("Для чтения выбрана отдельная тема.","Separate reader theme selected."),
        "Seçilib" to Pair("Выбрано","Selected"),
        "Seç" to Pair("Выбрать","Choose"),
        "Axtarılır…" to Pair("Поиск…","Searching…"),
        "Ən azı 2 simvol daxil et" to Pair("Введите хотя бы 2 символа","Enter at least 2 characters"),
        "Yenilə" to Pair("Обновить","Update"),
        "Kitab Kəşfiyyatçısı" to Pair("Исследователь книг","Book Explorer"),
        "Elmi fantastika" to Pair("Научная фантастика","Science fiction"),
        "Fantastika" to Pair("Фэнтези","Fantasy"),
        "Dram" to Pair("Драма","Drama"),
        "Elm" to Pair("Наука","Science"),
        "Tarix" to Pair("История","History"),
        "Detektiv" to Pair("Детектив","Mystery"),
        "Fəlsəfə" to Pair("Философия","Philosophy"),
        "Poeziya" to Pair("Поэзия","Poetry"),
        "Səyahət" to Pair("Путешествия","Travel"),
        "Biznes" to Pair("Бизнес","Business"),
        "Roman" to Pair("Роман","Novel"),
        "Romantika" to Pair("Романтика","Romance"),
        "Qorxu" to Pair("Ужасы","Horror"),
        "Bioqrafiya" to Pair("Биография","Biography"),
        "Komiks" to Pair("Комиксы","Comics"),
        "Digər" to Pair("Другое","Other"),
        "Naməlum" to Pair("Неизвестно","Unknown"),
        "Kosmos Səyyahı" to Pair("Космический странник","Space Wanderer"),
        "Cəngavər" to Pair("Рыцарь","Knight"),
        "Dramaturq" to Pair("Драматург","Playwright"),
        "Elm İnsanı" to Pair("Человек науки","Science Explorer"),
        "Tarix Salnaməçisi" to Pair("Летописец","Chronicler"),
        "Filosof" to Pair("Философ","Philosopher"),
        "Söz Ustası" to Pair("Мастер слова","Wordsmith"),
        "Dünya Səyyahı" to Pair("Путешественник","World Traveler"),
        "Strateq" to Pair("Стратег","Strategist"),
        "Hekayəçi" to Pair("Рассказчик","Storyteller"),
        "Romantik" to Pair("Романтик","Romantic"),
        "Qaranlıq Kəşfiyyatçısı" to Pair("Исследователь тьмы","Dark Explorer"),
        "Həyat Salnaməçisi" to Pair("Летописец жизни","Life Chronicler"),
        "Kadr Səyyahı" to Pair("Путешественник по кадрам","Panel Wanderer"),
        "I · Kəşfiyyatçı" to Pair("I · Исследователь","I · Explorer"),
        "II · Səyyah" to Pair("II · Странник","II · Wanderer"),
        "III · Ustad" to Pair("III · Мастер","III · Master"),
        "Kilidli" to Pair("Заблокировано","Locked"),
        "Davamlı sürüşdürmə" to Pair("Непрерывная прокрутка","Continuous scrolling"),
        "Məlumatların ehtiyat nüsxəsi" to Pair("Резервная копия данных","Data backup"),
        "İrəliləyiş" to Pair("Прогресс","Progress"),
        "Şəxsi qiymətləndirmə: " to Pair("Личная оценка: ","Personal rating: "),
        "Şrift ölçüsü  " to Pair("Размер шрифта  ","Font size  "),
        "Sətirarası məsafə  " to Pair("Межстрочный интервал  ","Line spacing  "),
        "Gizlədilmiş kitablar: " to Pair("Скрытые книги: ","Hidden books: "),
        "Bitirilmə tarixi: " to Pair("Дата завершения: ","Date completed: "),
        "Saxlanılmış qeydlər · " to Pair("Сохранённые заметки · ","Saved notes · "),
        "Kitab bitirildi ✓" to Pair("Книга прочитана ✓","Book finished ✓"),
        "Əlfəcin əlavə edildi" to Pair("Закладка добавлена","Bookmark added"),
        "Əlfəcin silindi" to Pair("Закладка удалена","Bookmark removed"),
        "Yalnız EPUB, PDF, CBZ və Markdown dəstəklənir" to Pair("Поддерживаются только EPUB, PDF, CBZ и Markdown","Only EPUB, PDF, CBZ and Markdown are supported"),
        "3.5 Preview · Offline EPUB / PDF / CBZ / Markdown" to Pair("Версия 3.5 · EPUB / PDF / CBZ / Markdown офлайн","3.5 Preview · Offline EPUB / PDF / CBZ / Markdown"),
        "Bitirilmiş kitab" to Pair("Прочитанные книги","Finished books"),
        "Oxuma vaxtı" to Pair("Время чтения","Reading time"),
        "Cari zəncir" to Pair("Текущая серия","Current streak"),
        "Ən uzun zəncir" to Pair("Самая длинная серия","Longest streak"),
        "Qeydə alınmış" to Pair("Зафиксировано","Recorded"),
        "Gündə ən az 5 dəqiqə" to Pair("Не менее 5 минут в день","At least 5 minutes daily"),
        "Ümumi: " to Pair("Всего: ","Total: "),
        "Aktiv gün: " to Pair("Активных дней: ","Active days: "),
        "gün" to Pair("дн.","days"),
        "dəq" to Pair("мин","min"),
        "aktiv oxu günü" to Pair("дней чтения","active reading days"),
        "kitab" to Pair("книг","books"),
        "kitab bitirilib" to Pair("прочитано книг","books finished"),
        "s" to Pair("ч","h"),
        "d" to Pair("м","m"),
        "növbəti: " to Pair("следующий уровень: ","next: "),
        "EPUB, PDF, CBZ və Markdown əlavə et" to Pair("Добавьте EPUB, PDF, CBZ или Markdown","Add EPUB, PDF, CBZ or Markdown"),
        "Bu əməliyyat geri qaytarıla bilməz." to Pair("Это действие необратимо.","This cannot be undone."),
        "Oxuma mövqeyi saxlanıldı" to Pair("Позиция чтения сохранена","Reading position saved"),
        "Kitabxanadakı tema oxuyucuda da tətbiq edilir." to Pair("Тема библиотеки применяется в читалке.","Reader follows the library theme."),
        "Şrift, sətirarası məsafə, ekran istiqaməti və zoom kitabın daxilindəki Aa menyusundan dəyişdirilir." to Pair("Шрифт, интервал, ориентация и масштаб меняются в меню Aa при чтении.","Font, spacing, orientation and zoom are adjusted in the reader's Aa menu."),
        "Əlfəcin əlavə et və ya sil" to Pair("Добавить или удалить закладку","Add or remove bookmark"),
        "Əlfəcini sil" to Pair("Удалить закладку","Remove bookmark"),
        "Kolleksiyadan çıxar" to Pair("Убрать из коллекции","Remove from collection"),
        "Qeydi sil" to Pair("Удалить заметку","Delete note"),
        "Kitab açıla bilmədi" to Pair("Не удалось открыть книгу","Could not open book"),
        "Fayl açılmadı" to Pair("Не удалось открыть файл","Could not open file"),
        "JSON metadata backup saxlanıldı" to Pair("Резервная копия метаданных JSON сохранена","JSON metadata backup saved"),
        "Backup xətası" to Pair("Ошибка резервного копирования","Backup error"),
        "Import xətası" to Pair("Ошибка импорта","Import error"),
        "Kitab silinmədi" to Pair("Не удалось удалить книгу","Could not delete book"),
        "Kitab faylı, əlfəcinlər, oxuma tarixçəsi və qeydləri silinəcək. Bu əməliyyat geri qaytarıla bilməz." to Pair("Файл книги, закладки, история чтения и заметки будут удалены. Это действие необратимо.","The book file, bookmarks, reading history and notes will be deleted. This cannot be undone."),
        "Kitab qeydini kitabxanadan çıxaracağıq. Orijinal fayl tətbiqin saxlamasında qalacaq, amma siyahıda görünməyəcək." to Pair("Запись исчезнет из библиотеки, но исходный файл останется в памяти приложения.","The library entry will be removed, but the original file remains in app storage."),
        "Ən azı 2 simvol daxil et" to Pair("Введите минимум 2 символа","Enter at least 2 characters"),
        "Görünür" to Pair("Видно","Visible"),
        "3.5 Preview · Offline EPUB / PDF / CBZ / Markdown" to Pair("Версия 3.5 · Без интернета · EPUB / PDF / CBZ / Markdown","3.5 Preview · Offline EPUB / PDF / CBZ / Markdown")
,
        "Oxuma xəritəsi · 13 həftə" to Pair("Карта чтения · 13 недель","Reading heatmap · 13 weeks"),
        "Rəngin tündlüyü gündəlik oxuma müddətini göstərir." to Pair("Насыщенность цвета показывает время чтения за день.","Darker cells mean more reading time."),
        "Oxu məqsədləri" to Pair("Цели чтения","Reading goals"),
        "Gündəlik məqsəd" to Pair("Ежедневная цель","Daily goal"),
        "Aylıq kitab məqsədi" to Pair("Месячная цель по книгам","Monthly book goal"),
        "dəqiqə" to Pair("мин","min"),
        "Bu həftə" to Pair("На этой неделе","This week"),
        "Bu ay" to Pair("В этом месяце","This month"),
        "Bu ay bitirilən kitablar" to Pair("Книг завершено за месяц","Books finished this month"),
        "Nailiyyətlər" to Pair("Достижения","Achievements"),
        "İlk kitab" to Pair("Первая книга","First book"),
        "Kitab qurdu" to Pair("Книжный червь","Bookworm"),
        "7 günlük zəncir" to Pair("Серия 7 дней","7-day streak"),
        "30 günlük zəncir" to Pair("Серия 30 дней","30-day streak"),
        "10 saat oxu" to Pair("10 часов чтения","10 reading hours"),
        "100 saat oxu" to Pair("100 часов чтения","100 reading hours"),
        "Janr üzrə irəliləyiş" to Pair("Прогресс по жанрам","Genre progress"),
        "Hələ janr üzrə bitirilmiş kitab yoxdur" to Pair("Пока нет завершённых книг с жанром","No finished books with a genre yet"),
        "Bütün kitablar" to Pair("Все книги","All books"),
        "Oxu gündəliyi" to Pair("Дневник чтения","Reading journal"),
        "Gündəlik" to Pair("Дневник","Journal"),
        "Sitatlar və qeydlər" to Pair("Цитаты и заметки","Quotes & notes"),
        "Rəylər" to Pair("Отзывы","Reviews"),
        "Kitab seç" to Pair("Выбрать книгу","Select a book"),
        "Markdown ixrac et" to Pair("Экспорт Markdown","Export Markdown"),
        "JSON ixrac et" to Pair("Экспорт JSON","Export JSON"),
        "Bu günün oxu qeydi" to Pair("Сегодняшняя запись","Today's reading entry"),
        "Gündəliyə əlavə et" to Pair("Добавить в дневник","Add to journal"),
        "Kitab haqqında şəxsi rəy" to Pair("Личный отзыв о книге","Personal book review"),
        "Rəyi saxla" to Pair("Сохранить отзыв","Save review"),
        "Gündəlik qeydi silinsin?" to Pair("Удалить запись дневника?","Delete journal entry?"),
        "Boş gündəlik qeydi saxlanılmır" to Pair("Нельзя сохранить пустую запись","Cannot save an empty journal entry"),
        "Kitab tapılmadı" to Pair("Книга не найдена","Book not found"),
        "Sil" to Pair("Удалить","Delete"),
        "Şəxsi qeyd" to Pair("Личная заметка","Personal note"),
        "Fəsil" to Pair("Глава","Chapter"),
        "Focus rejimi" to Pair("Режим фокусировки","Focus mode"),
        "Focus rejimindən çıx" to Pair("Выйти из режима фокусировки","Exit focus mode"),
        "Oxu taymeri" to Pair("Таймер чтения","Reading timer"),
        "Sıfırla" to Pair("Сбросить","Reset"),
        "Parlaqlıq" to Pair("Яркость","Brightness"),
        "Axıcı səhifə keçidi" to Pair("Плавное перелистывание","Smooth page transitions"),
        "Bu kitab üçün ayrıca parametrlər" to Pair("Индивидуальные настройки книги","Per-book settings"),
        "Sistem parlaqlığı" to Pair("Системная яркость","System brightness"),
        "Fərdi parlaqlıq" to Pair("Своя яркость","Custom brightness"),
        "Oxuma sessiyası" to Pair("Сеанс чтения","Reading session"),
        "Tam ekran" to Pair("Полный экран","Fullscreen"),
        "Görünüşü dəyiş" to Pair("Изменить вид","Change layout"),
        "Qruplaşdırma yoxdur" to Pair("Без группировки","No grouping"),
        "Müəllif üzrə" to Pair("По автору","By author"),
        "Seriya üzrə" to Pair("По серии","By series"),
        "Seç" to Pair("Выбрать","Select"),
        "Hamısını seç" to Pair("Выбрать все","Select all"),
        "Seçilmiş kitablar" to Pair("Выбранные книги","Selected books"),
        "Kolleksiyaya əlavə et" to Pair("Добавить в коллекцию","Add to collection"),
        "Gizlət" to Pair("Скрыть","Hide"),
        "Kitabları gizlət?" to Pair("Скрыть книги?","Hide books?"),
        "Fayllar silinmir. Kitabları Parametrlərdən bərpa etmək olar." to Pair("Файлы не удаляются. Книги можно восстановить в настройках.","Files are not deleted. Books can be restored in Settings."),
        "Kolleksiya adı" to Pair("Название коллекции","Collection name"),
        "Mövcud kolleksiyalar" to Pair("Существующие коллекции","Existing collections"),
        "Dublikatlar" to Pair("Дубликаты","Duplicates"),
        "Dublikat kitablar" to Pair("Дубликаты книг","Duplicate books"),
        "Dublikat tapılmadı" to Pair("Дубликатов не найдено","No duplicates found"),
        "Dublikatlar yoxlanılmadı" to Pair("Не удалось проверить дубликаты","Could not scan duplicates"),
        "Yalnız eyni fayl məzmunu yoxlanılır." to Pair("Проверяются только файлы с одинаковым содержимым.","Only files with identical content are matched."),
        "nüsxə" to Pair("копии","copies"),
        "Ətraflı" to Pair("Подробнее","Details"),
        "Seriya" to Pair("Серия","Series"),
        "Ağıllı axtarış" to Pair("Умный поиск","Smart search"),
        "Kitab, müəllif, seriya, janr, kolleksiya axtar..." to Pair("Поиск книги, автора, серии, жанра, коллекции...","Search title, author, series, genre, collection..."),
        "3.9 Preview · Offline EPUB / PDF / CBZ / Markdown" to Pair(
            "Версия 3.9 · Без интернета · EPUB / PDF / CBZ / Markdown",
            "3.9 Preview · Offline EPUB / PDF / CBZ / Markdown")
    )
    fun translate(text:String,language:String):String {
        if(language=="az")return when(text) {
            "Settings"->"Parametrlər"
            "Appearance"->"Görünüş"
            "Reading"->"Oxuma"
            "Library"->"Kitabxana"
            "Data & Backup"->"Məlumatlar və ehtiyat nüsxə"
            "About"->"Haqqında"
            "Continuous scroll"->"Davamlı sürüşdürmə"
            "Swipe ilə səhifə çevirmə"->"Sürüşdürərək səhifə çevirmə"
            "Export JSON"->"JSON ixrac et"
            "Import JSON"->"JSON idxal et"
            "Export / Import"->"İxrac / İdxal"
            "App Theme — bütün ekranlar"->"Tətbiq teması — bütün ekranlar"
            "Reader Theme tətbiq temasını izləsin"->"Oxuyucu tətbiqin temasını izləsin"
            "Qlobal Reader Theme seçimi Appearance bölməsindədir."->
                "Oxuyucunun ümumi temasını Görünüş bölməsindən seç."
            "3.9 Preview · Offline EPUB / PDF / CBZ / Markdown"->
                "3.9 İlkin versiya · İnternetsiz EPUB / PDF / CBZ / Markdown"
            "3.5 Preview · Offline EPUB / PDF / CBZ / Markdown"->
                "3.5 İlkin versiya · İnternetsiz EPUB / PDF / CBZ / Markdown"
            "3.3.3 Preview · Offline EPUB / PDF / CBZ"->
                "3.5 İlkin versiya · İnternetsiz EPUB / PDF / CBZ / Markdown"
            "Progress"->"İrəliləyiş"
            "Fit to Width"->"Enə uyğunlaşdır"
            "Dark"->"Qaranlıq"
            "Light"->"Açıq"
            "Paper"->"Kağız"
            "Warm"->"İsti"
            "Sage"->"Adaçayı"
            "Ocean"->"Okean"
            "Midnight"->"Gecəyarısı"
            "Graphite"->"Qrafit"
            else->text
        }
        dictionary[text]?.let {return if(language=="ru")it.first else it.second}
        // Structured labels preserve numbers and user-owned book names.
        if(text.contains(" • ")) {
            return text.split(" • ").joinToString(" • ") {segment->
                dictionary[segment.trim()]?.let {
                    if(language=="ru")it.first else it.second
                } ?: segment
            }
        }
        val countBookmark=Regex("""^(\d+) yadda saxlanmış mövqe$""").matchEntire(text)
        if(countBookmark!=null)return countBookmark.groupValues[1]+
            if(language=="ru")" сохранённых закладок" else " saved bookmarks"
        val completed=Regex("""^(\d+) kitab bitirilib · (\d+) aktiv oxu günü$""").matchEntire(text)
        if(completed!=null)return if(language=="ru")
            "${completed.groupValues[1]} прочитано · ${completed.groupValues[2]} дней чтения"
        else "${completed.groupValues[1]} finished · ${completed.groupValues[2]} active days"
        val noNotes=Regex("""^Saxlanılmış qeydlər · (\d+)$""").matchEntire(text)
        if(noNotes!=null)return (if(language=="ru")"Сохранённые заметки · " else "Saved notes · ")+noNotes.groupValues[1]
        val chapterOffline=Regex("""^Fəsil (\d+) · offline saxlanılır$""").matchEntire(text)
        if(chapterOffline!=null)return (if(language=="ru")"Глава " else "Chapter ")+
            chapterOffline.groupValues[1]+(if(language=="ru")" · сохранено на устройстве"
                else " · saved offline")
        if(text.startsWith("Format: ") && text.contains(" · Progress: ")) {
            val parts=text.split(" · Progress: ",limit=2)
            return (if(language=="ru")"Формат: " else "Format: ")+
                parts[0].removePrefix("Format: ")+
                (if(language=="ru")" · Прогресс: " else " · Progress: ")+parts[1]
        }
        if(text.startsWith("Şəxsi qiymətləndirmə: ")) {
            return (if(language=="ru")"Личная оценка: " else "Personal rating: ")+
                text.removePrefix("Şəxsi qiymətləndirmə: ")
        }
        if(text.startsWith("Bitirilmə tarixi: ")) {
            return (if(language=="ru")"Дата завершения: " else "Date completed: ")+
                text.removePrefix("Bitirilmə tarixi: ")
        }
        if(text.startsWith("Şrift ölçüsü  ")) {
            return (if(language=="ru")"Размер шрифта  " else "Font size  ")+
                text.removePrefix("Şrift ölçüsü  ")
        }
        if(text.startsWith("Sətirarası məsafə  ")) {
            return (if(language=="ru")"Межстрочный интервал  " else "Line spacing  ")+
                text.removePrefix("Sətirarası məsafə  ")
        }

        val dynamicPrefixes=listOf("Şrift ölçüsü  ","Sətirarası məsafə  ",
            "Gizlədilmiş kitablar: ","Bitirilmə tarixi: ","Saxlanılmış qeydlər · ")
        for(prefix in dynamicPrefixes) {
            if(text.startsWith(prefix)) {
                val translated=dictionary[prefix] ?:continue
                return (if(language=="ru")translated.first else translated.second)+
                    text.removePrefix(prefix)
            }
        }
        if(text.contains(" · ")) {
            return text.split(" · ").joinToString(" · ") {segment->
                dictionary[segment]?.let {if(language=="ru")it.first else it.second} ?: segment
            }
        }
        // Keep numeric values and user-owned names unchanged; translate only UI prefixes.
        val streak=Regex("""^(🔥 )?(\d+) gün$""").matchEntire(text)
        if(streak!=null)return streak.groupValues[1]+streak.groupValues[2]+
            if(language=="ru")" дн." else " days"
        val minutes=Regex("""^(\d+) dəq$""").matchEntire(text)
        if(minutes!=null)return minutes.groupValues[1]+
            if(language=="ru")" мин" else " min"
        val total=Regex("""^(Ümumi: |Aktiv gün: )(\d+)$""").matchEntire(text)
        if(total!=null) {
            val prefix=dictionary[total.groupValues[1]]
            return (if(language=="ru")prefix?.first else prefix?.second).orEmpty()+total.groupValues[2]
        }
        val hours=Regex("""^(\d+) s (\d+) d$""").matchEntire(text)
        if(hours!=null)return if(language=="ru")
            "${hours.groupValues[1]} ч ${hours.groupValues[2]} мин"
        else "${hours.groupValues[1]} h ${hours.groupValues[2]} min"
        val level=Regex("""^(I|II|III) · (Kəşfiyyatçı|Səyyah|Ustad) · (\d+) kitab( · növbəti: (\d+))?$""").matchEntire(text)
        if(level!=null) {
            val title=dictionary[level.groupValues[2]]
            val rendered=(if(language=="ru")title?.first else title?.second)
                ?:level.groupValues[2]
            val label=if(language=="ru")"книг" else "books"
            val next=level.groupValues[5]
            return "${level.groupValues[1]} · $rendered · ${level.groupValues[3]} $label"+
                (if(next.isNotEmpty())(if(language=="ru")" · следующий уровень: " else " · next: ")+next else "")
        }
        if(text.startsWith("🏆 ")) {
            val name=text.removePrefix("🏆 ")
            val result=dictionary[name]
            if(result!=null)return "🏆 "+(if(language=="ru")result.first else result.second)
        }
        if(text.startsWith("📚 ")) {
            val name=text.removePrefix("📚 ")
            val result=dictionary[name]
            if(result!=null)return "📚 "+(if(language=="ru")result.first else result.second)
        }
        val numeric=Regex("""^(Fəsil|Səhifə)\s+(\d+)(.*)$""").matchEntire(text)
        if(numeric!=null) {
            val label=dictionary[numeric.groupValues[1]]
            if(label!=null)return (if(language=="ru")label.first else label.second)+
                " "+numeric.groupValues[2]+numeric.groupValues[3]
        }
        return text
    }
}

@Composable
fun ui(text:String):String=UiLanguage.translate(text,LocalUiLanguage.current)
