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
        "Dark" to Pair("Тёмная","Qaranlıq"),
        "Light" to Pair("Светлая","Açıq"),
        "Sepia" to Pair("Сепия","Sepia"),
        "Paper" to Pair("Бумага","Kağız"),
        "Warm" to Pair("Тёплая","İsti"),
        "Sage" to Pair("Шалфей","Adaçayı"),
        "Ocean" to Pair("Океан","Okean"),
        "Midnight" to Pair("Полночь","Gecəyarısı"),
        "Graphite" to Pair("Графит","Qrafit"),
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
        "Haqqında" to Pair("О приложении","About")
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
        // Keep numeric values and user-owned names unchanged; translate only UI prefixes.
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
