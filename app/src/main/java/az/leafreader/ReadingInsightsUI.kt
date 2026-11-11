package az.leafreader

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
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
    var editingGoal by remember {mutableStateOf<String?>(null)}
    var draftGoal by remember {mutableStateOf("")}
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
                OutlinedButton(onClick={editingGoal="daily";draftGoal=dailyGoal.toString()}) {
                    Icon(Icons.Default.Edit,null,Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("$dailyGoal "+ui("dəqiqə"))
                }
            }
            val todayMin=daily[today] ?:0L
            LinearProgressIndicator(progress={(todayMin.toFloat()/dailyGoal).coerceIn(0f,1f)},
                modifier=Modifier.fillMaxWidth())
            Text("$todayMin / $dailyGoal "+ui("dəqiqə"),fontSize=12.sp)
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(ui("Aylıq kitab məqsədi"),modifier=Modifier.weight(1f))
                OutlinedButton(onClick={editingGoal="monthly";draftGoal=monthlyGoal.toString()}) {
                    Icon(Icons.Default.Edit,null,Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("$monthlyGoal")
                }
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
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(ui("Nailiyyətlər"),fontWeight=FontWeight.Bold,fontSize=19.sp,
            modifier=Modifier.padding(start=4.dp))
        ReadingInsights.awards(overview).chunked(2).forEach {pair->
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                pair.forEach {award->
                    val gold=Color(0xFFE4AC26)
                    val bg=if(award.earned)Color(0xFFFFE7A1) else Color.White
                    Card(modifier=Modifier.weight(1f).height(150.dp),
                        shape=RoundedCornerShape(20.dp),
                        colors=CardDefaults.cardColors(containerColor=bg),
                        border=BorderStroke(1.dp,if(award.earned)gold else Color(0xFFE1E6EA))) {
                        Column(Modifier.fillMaxSize().padding(12.dp),
                            horizontalAlignment=Alignment.CenterHorizontally,
                            verticalArrangement=Arrangement.Center) {
                            Box(contentAlignment=Alignment.Center) {
                                Icon(Icons.Default.Castle,null,Modifier.size(55.dp),
                                    tint=if(award.earned)Color(0xFFAF7109) else Color(0xFFDADEE2))
                                if(!award.earned)Icon(Icons.Default.Lock,null,
                                    Modifier.align(Alignment.BottomCenter).size(24.dp),
                                    tint=Color(0xFF73808B))
                                else Icon(Icons.Default.Star,null,
                                    Modifier.align(Alignment.TopEnd).size(23.dp),
                                    tint=Color(0xFFFF7A00))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(ui(award.key),fontSize=12.sp,fontWeight=FontWeight.SemiBold,
                                textAlign=TextAlign.Center,
                                color=if(award.earned)Color(0xFF6F480A) else Color(0xFF77828C))
                        }
                    }
                }
                if(pair.size==1)Spacer(Modifier.weight(1f))
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
    if(editingGoal!=null) {
        val dailyMode=editingGoal=="daily"
        val min=if(dailyMode)5 else 1
        val max=if(dailyMode)240 else 30
        val parsed=draftGoal.toIntOrNull()
        val valid=parsed!=null && parsed in min..max
        AlertDialog(
            onDismissRequest={editingGoal=null},
            title={Text(ui(if(dailyMode)"Gündəlik məqsəd" else "Aylıq kitab məqsədi"))},
            text={
                Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text(ui(if(dailyMode)"Dəqiqə sayını daxil et" else "Kitab sayını daxil et"))
                    Row(verticalAlignment=Alignment.CenterVertically,
                        horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        OutlinedIconButton(onClick={
                            draftGoal=((parsed ?: min)-1).coerceIn(min,max).toString()
                        }) {Icon(Icons.Default.Remove,ui("Azalt"))}
                        OutlinedTextField(value=draftGoal,onValueChange={value->
                            draftGoal=value.filter {it.isDigit()}.take(3)
                        },modifier=Modifier.weight(1f),singleLine=true,
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
                            isError=draftGoal.isNotEmpty() && !valid)
                        OutlinedIconButton(onClick={
                            draftGoal=((parsed ?: (min-1))+1).coerceIn(min,max).toString()
                        }) {Icon(Icons.Default.Add,ui("Artır"))}
                    }
                    Text("$min–$max",color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton={TextButton(enabled=valid,onClick={
                val value=parsed ?:return@TextButton
                if(dailyMode) {
                    dailyGoal=value;prefs.edit().putInt("goalDailyMinutes",value).apply()
                }else {
                    monthlyGoal=value;prefs.edit().putInt("goalMonthlyBooks",value).apply()
                }
                editingGoal=null
            }){Text(ui("Saxla"))}},
            dismissButton={TextButton(onClick={editingGoal=null}) {Text(ui("Ləğv et"))}}
        )
    }

}
