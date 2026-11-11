package az.leafreader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Forest=Color(0xFF267A71)
private val TextInk=Color(0xFF1C3332)
private val SoftBackground=Color(0xFFF8F6F0)

@Composable
fun ReadingDashboard(store:BookStore,onBack:()->Unit) {
    // A fresh snapshot on screen entry; no fabricated sample values.
    val overview=remember {ReadingStats.calculate(store.records(),LocalDate.now())}
    Scaffold(
        contentWindowInsets=WindowInsets.safeDrawing,
        containerColor=MaterialTheme.colorScheme.background,
        topBar={
            Row(Modifier.fillMaxWidth().statusBarsPadding()
                .padding(horizontal=12.dp,vertical=10.dp),
                verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick=onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,ui("Kitabxanaya qayıt"))
                }
                Column(Modifier.padding(start=4.dp)) {
                    Text(ui("Oxu statistikası"),fontSize=23.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface)
                    Text(ui("Yalnız bu cihazdakı oxu məlumatları"),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    ) { insets->
        LazyColumn(Modifier.fillMaxSize().padding(insets).padding(horizontal=18.dp),
            verticalArrangement=Arrangement.spacedBy(14.dp),
            contentPadding=PaddingValues(bottom=36.dp)) {
            item {
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    MetricTile("Bitirilmiş kitab",overview.finishedBooks.toString(),
                        "Ümumi: ${overview.totalBooks}",Modifier.weight(1f))
                    MetricTile("Oxuma vaxtı",
                        if(overview.readingMinutes>=60)
                            "${overview.readingMinutes/60} s ${overview.readingMinutes%60} d"
                        else "${overview.readingMinutes} dəq",
                        "Qeydə alınmış",Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    MetricTile("Cari zəncir","🔥 ${overview.currentStreak} gün",
                        "Gündə ən az 5 dəqiqə",Modifier.weight(1f))
                    MetricTile("Ən uzun zəncir","${overview.longestStreak} gün",
                        "Aktiv gün: ${overview.activeDays}",Modifier.weight(1f))
                }
            }
            item { ReadingInsightsPanel(store,overview,LocalDate.now()) }
            item {
                Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),
                    shape=RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        Text(ui("Oxu təqvimi · son 14 gün"),fontSize=18.sp,
                            fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurface)
                        Text(ui("Bir günün aktiv sayılması üçün ən az 5 dəqiqə oxu tələb olunur."),
                            fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        val byDay=overview.daily.associate {it.day to it.minutes}
                        val days=(13 downTo 0).map {LocalDate.now().minusDays(it.toLong())}
                        Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            days.chunked(7).forEach {week->
                                Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                    week.forEach { day->
                                        val min=byDay[day] ?: 0L
                                        val shade=when {
                                            min>=30->Forest
                                            min>=5->Color(0xFF78BDB0)
                                            min>0->Color(0xFFC2E0DA)
                                            else->Color(0xFFEAEDE6)
                                        }
                                        Column(Modifier.weight(1f),
                                            horizontalAlignment=Alignment.CenterHorizontally) {
                                            Box(Modifier.fillMaxWidth().height(35.dp)
                                                .background(shade,RoundedCornerShape(9.dp)))
                                            Text(ui(day.dayOfMonth.toString()),fontSize=10.sp,
                                                color=MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),
                    shape=RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        Text(ui("Son 6 ay · Bitirilmiş kitablar"),fontSize=18.sp,
                            fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurface)
                        val maxCount=(overview.completedMonthly.maxOfOrNull {it.second} ?: 0).coerceAtLeast(1)
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),
                            verticalAlignment=Alignment.Bottom) {
                            overview.completedMonthly.forEach {entry->
                                Column(Modifier.weight(1f),
                                    horizontalAlignment=Alignment.CenterHorizontally) {
                                    Text(ui(entry.second.toString()),fontSize=11.sp,color=MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(5.dp))
                                    Box(Modifier.fillMaxWidth().height(
                                        (9f+75f*entry.second/maxCount).dp)
                                        .background(if(entry.second>0)Forest else Color(0xFFE7ECE7),
                                            RoundedCornerShape(topStart=7.dp,topEnd=7.dp)))
                                    Text(ui(entry.first),fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),
                    shape=RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        Text(ui("Kitabxana tərkibi"),fontSize=18.sp,
                            fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurface)
                        LibraryMeter("EPUB",overview.epubBooks,overview.totalBooks)
                        LibraryMeter("PDF",overview.pdfBooks,overview.totalBooks)
                        LibraryMeter("CBZ · Komiks",overview.comicBooks,overview.totalBooks)
                        LibraryMeter("Markdown",overview.markdownBooks,overview.totalBooks)
                    }
                }
            }
            item {
                Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),
                    shape=RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(ui("Oxucu pasportu"),fontSize=20.sp,fontWeight=FontWeight.Bold,
                            color=MaterialTheme.colorScheme.onSurface)
                        val earned=remember {GenreCatalog.earned(store.records())}
                        val savedTitle=remember {store.setting().getString("readerTitle","")}
                        var chosen by remember {
                            mutableStateOf(savedTitle.orEmpty())
                        }
                        val unlocked=earned.map {GenreCatalog.title(it.first)}.distinct()
                        val displayedTitle=if(chosen in unlocked)chosen else unlocked.firstOrNull().orEmpty()
                        Text(ui(if(displayedTitle.isNotBlank())"🏆 ${displayedTitle}" else "📚 Kitab Kəşfiyyatçısı"),
                            fontSize=18.sp,fontWeight=FontWeight.SemiBold)
                        Text(ui("${overview.finishedBooks} kitab bitirilib · "+
                            "${overview.activeDays} aktiv oxu günü"),
                            fontSize=14.sp,color=MaterialTheme.colorScheme.primary)
                        if(earned.isEmpty()) {
                            Text(ui("Janra uyğun titul üçün janrı müəyyən edilmiş kitabı bitir."),
                                fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(ui("Qazandığın titullar"),fontWeight=FontWeight.Medium)
                            earned.forEach { (genre,count)->
                                val title=GenreCatalog.title(genre)
                                Row(Modifier.fillMaxWidth(),
                                    verticalAlignment=Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(ui("${title} · ${genre}"),fontSize=13.sp)
                                        Text(ui("${GenreCatalog.level(count)} · ${count} kitab"+
                                            (GenreCatalog.nextMilestone(count)?.let {
                                                " · növbəti: ${it}"
                                            } ?: "")),
                                            color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=11.sp)
                                    }
                                    TextButton(onClick={
                                        chosen=title
                                        store.setting().edit().putString("readerTitle",title).apply()
                                    }) {
                                        Text(ui(if(displayedTitle==title)"Seçilib" else "Seç"),
                                            fontSize=12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun MetricTile(label:String,value:String,sub:String,modifier:Modifier=Modifier) {
    Card(modifier,shape=RoundedCornerShape(20.dp),
        colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(ui(label),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Text(ui(value),fontSize=22.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface)
            Text(ui(sub),fontSize=11.sp,color=MaterialTheme.colorScheme.primary)
        }
    }
}
@Composable
private fun LibraryMeter(label:String,count:Int,total:Int) {
    Column(verticalArrangement=Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(ui(label),fontSize=14.sp)
            Text(ui(count.toString()),fontSize=14.sp,color=MaterialTheme.colorScheme.primary)
        }
        LinearProgressIndicator(progress={if(total>0)count.toFloat()/total else 0f},
            modifier=Modifier.fillMaxWidth().height(6.dp),
            color=MaterialTheme.colorScheme.primary,trackColor=Color(0xFFE7ECE7))
    }
}
