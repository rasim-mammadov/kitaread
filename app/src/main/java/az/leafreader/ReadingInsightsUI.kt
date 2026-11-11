package az.leafreader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** All values are computed from local data. No internet access or mock data. */
@Composable
fun ReadingInsightsPanel(store:BookStore,overview:ReadingOverview,today:LocalDate) {
    val records=remember(store) {store.records()}
    val prefs=remember(store) {store.setting()}
    var dailyGoal by remember {mutableIntStateOf(prefs.getInt("goalDailyMinutes",20).coerceIn(5,240))}
    var monthlyGoal by remember {mutableIntStateOf(prefs.getInt("goalMonthlyBooks",2).coerceIn(1,30))}
    val daily=overview.daily.associate {it.day to it.minutes}
    val weekStart=today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    val weekMinutes=ReadingInsights.minutesBetween(overview.daily,weekStart,today)
    val monthStart=ReadingInsights.monthStart(today)
    val monthMinutes=ReadingInsights.minutesBetween(overview.daily,monthStart,today)
    val finishedThisMonth=ReadingInsights.completedSince(records,monthStart,today)
    val colors=MaterialTheme.colorScheme

    Card(colors=CardDefaults.cardColors(containerColor=colors.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(ui("Oxuma xəritəsi · 13 həftə"),fontSize=18.sp,fontWeight=FontWeight.Bold)
            val dates=remember(today){ReadingInsights.heatmapDays(today)}
            dates.chunked(7).forEach {week->
                Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                    week.forEach {date->
                        val minutes=daily[date] ?:0L
                        val color=when {
                            minutes>=60->Color(0xFF236B58)
                            minutes>=30->Color(0xFF429A7B)
                            minutes>=5->Color(0xFF92C8B1)
                            minutes>0->Color(0xFFD2E6D9)
                            else->colors.surfaceVariant
                        }
                        Box(Modifier.weight(1f).height(13.dp)
                            .background(color,RoundedCornerShape(3.dp)))
                    }
                }
            }
            Text(ui("Rəngin tündlüyü gündəlik oxuma müddətini göstərir."),
                fontSize=12.sp,color=colors.onSurfaceVariant)
        }
    }
    Card(colors=CardDefaults.cardColors(containerColor=colors.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(ui("Oxu məqsədləri"),fontWeight=FontWeight.Bold,fontSize=18.sp)
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(ui("Gündəlik məqsəd"),modifier=Modifier.weight(1f))
                TextButton(onClick={
                    dailyGoal=if(dailyGoal>=60)5 else dailyGoal+5
                    prefs.edit().putInt("goalDailyMinutes",dailyGoal).apply()
                }) {Text("$dailyGoal "+ui("dəqiqə")+"  ✎")}
            }
            val todayMin=daily[today] ?:0L
            LinearProgressIndicator(progress={(todayMin.toFloat()/dailyGoal).coerceIn(0f,1f)},
                modifier=Modifier.fillMaxWidth())
            Text("$todayMin / $dailyGoal "+ui("dəqiqə"),fontSize=12.sp)
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(ui("Aylıq kitab məqsədi"),modifier=Modifier.weight(1f))
                TextButton(onClick={
                    monthlyGoal=if(monthlyGoal>=12)1 else monthlyGoal+1
                    prefs.edit().putInt("goalMonthlyBooks",monthlyGoal).apply()
                }) {Text("$monthlyGoal  ✎")}
            }
            LinearProgressIndicator(progress={(finishedThisMonth.toFloat()/monthlyGoal).coerceIn(0f,1f)},
                modifier=Modifier.fillMaxWidth())
            Text("$finishedThisMonth / $monthlyGoal "+ui("kitab"),fontSize=12.sp)
            HorizontalDivider()
            Text(ui("Bu həftə")+": $weekMinutes "+ui("dəqiqə"))
            Text(ui("Bu ay")+": $monthMinutes "+ui("dəqiqə"))
            Text(ui("Bu ay bitirilən kitablar")+": $finishedThisMonth")
        }
    }
    Card(colors=CardDefaults.cardColors(containerColor=colors.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
            Text(ui("Nailiyyətlər"),fontWeight=FontWeight.Bold,fontSize=18.sp)
            ReadingInsights.awards(overview).forEach {award->
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text(if(award.earned)"🏆" else "🔒",fontSize=18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(ui(award.key),modifier=Modifier.weight(1f),
                        color=if(award.earned) colors.onSurface else colors.onSurfaceVariant)
                }
            }
        }
    }
    Card(colors=CardDefaults.cardColors(containerColor=colors.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(ui("Janr üzrə irəliləyiş"),fontWeight=FontWeight.Bold,fontSize=18.sp)
            val genres=ReadingInsights.genreProgress(records)
            if(genres.isEmpty())Text(ui("Hələ janr üzrə bitirilmiş kitab yoxdur"))
            genres.take(10).forEach { (genre,count)->
                Row(verticalAlignment=Alignment.CenterVertically) {
                    Text(ui(genre),modifier=Modifier.weight(1f))
                    Text("$count "+ui("kitab"),color=colors.primary)
                }
                LinearProgressIndicator(progress={(count.toFloat()/10f).coerceIn(0f,1f)},
                    modifier=Modifier.fillMaxWidth())
            }
        }
    }
}
