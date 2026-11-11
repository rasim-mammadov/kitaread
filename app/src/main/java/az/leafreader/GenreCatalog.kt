package az.leafreader

import org.json.JSONObject

/** Offline, user-editable labels. Never infer a genre from an unrelated filename. */
object GenreCatalog {
    const val UNKNOWN="Naməlum"
    val genres=listOf(
        UNKNOWN,"Elmi fantastika","Fantastika","Dram","Elm","Tarix",
        "Detektiv","Fəlsəfə","Poeziya","Səyahət","Biznes",
        "Roman","Romantika","Qorxu","Bioqrafiya","Komiks","Digər"
    )
    private val tags=listOf(
        "Elmi fantastika" to listOf("science fiction","sci-fi","scifi","space opera","elmi fantastika","научная фантастика"),
        "Fantastika" to listOf("fantasy","fantastika","фэнтези"),
        "Dram" to listOf("drama","dram","theatre","theater","play","tragedy","dramaturgy"),
        "Elm" to listOf("popular science","nonfiction science","physics","astronomy","biology","science","elm"),
        "Tarix" to listOf("history","historical","tarix","история"),
        "Detektiv" to listOf("mystery","detective","crime","thriller","detektiv"),
        "Fəlsəfə" to listOf("philosophy","fəlsəfə","философия"),
        "Poeziya" to listOf("poetry","poems","poeziya"),
        "Səyahət" to listOf("travel","journey","səyahət"),
        "Biznes" to listOf("business","management","finance","economics","biznes"),
        "Komiks" to listOf("comic","manga","graphic novel","manhwa"),
        "Roman" to listOf("literary fiction","novel","roman"),
        "Romantika" to listOf("romance","love story","romantika"),
        "Qorxu" to listOf("horror","qorxu"),
        "Bioqrafiya" to listOf("biography","memoir","autobiography")
    )
    fun fromSubjects(subjects:List<String>):String {
        for(subject in subjects) {
            val lower=subject.lowercase().trim()
            if(lower.isBlank())continue
            for((genre,keywords) in tags) {
                if(keywords.any {word->lower==word || lower.contains(word)})return genre
            }
        }
        return UNKNOWN
    }
    fun level(finished:Int):String=when {
        finished>=15 -> "III · Ustad"
        finished>=5 -> "II · Səyyah"
        finished>=1 -> "I · Kəşfiyyatçı"
        else -> "Kilidli"
    }
    fun nextMilestone(finished:Int):Int?=when {
        finished<1 -> 1
        finished<5 -> 5
        finished<15 -> 15
        else -> null
    }
    fun title(genre:String):String=when(genre) {
        "Elmi fantastika"->"Kosmos Səyyahı"
        "Fantastika"->"Cəngavər"
        "Dram"->"Dramaturq"
        "Elm"->"Elm İnsanı"
        "Tarix"->"Tarix Salnaməçisi"
        "Detektiv"->"Detektiv"
        "Fəlsəfə"->"Filosof"
        "Poeziya"->"Söz Ustası"
        "Səyahət"->"Dünya Səyyahı"
        "Biznes"->"Strateq"
        "Roman"->"Hekayəçi"
        "Romantika"->"Romantik"
        "Qorxu"->"Qaranlıq Kəşfiyyatçısı"
        "Bioqrafiya"->"Həyat Salnaməçisi"
        "Komiks"->"Kadr Səyyahı"
        else->"Kitab Kəşfiyyatçısı"
    }
    fun earned(records:List<JSONObject>):List<Pair<String,Int>> {
        val finished=records.filter {it.optString("status")=="Bitirilib"}
        return finished.groupingBy {it.optString("genre",UNKNOWN)}
            .eachCount().filterKeys {it!=UNKNOWN}
            .toList().sortedWith(compareByDescending<Pair<String,Int>> {it.second}.thenBy {it.first})
    }
}
