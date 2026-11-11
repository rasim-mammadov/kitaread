package az.leafreader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ReadingTheme(val name:String,val paper:Color,val ink:Color)

object ReadingThemes {
    val presets=listOf(
        ReadingTheme("Light",Color.White,Color(0xFF352F29)),
        ReadingTheme("Sepia",Color(0xFFF3E9D8),Color(0xFF352F29)),
        ReadingTheme("Dark",Color(0xFF1C2229),Color(0xFFF0EFE9)),
        ReadingTheme("AMOLED",Color.Black,Color(0xFFF0EFE9)),
        ReadingTheme("Paper",Color(0xFFFCFAF5),Color(0xFF352F29)),
        ReadingTheme("Warm",Color(0xFFF5DFC4),Color(0xFF523C2B)),
        ReadingTheme("Sage",Color(0xFFDCE8D9),Color(0xFF304737)),
        ReadingTheme("Ocean",Color(0xFFDCE9EF),Color(0xFF29465D)),
        ReadingTheme("Midnight",Color(0xFF141F34),Color(0xFFF0EFE9)),
        ReadingTheme("Graphite",Color(0xFF303339),Color(0xFFF0EFE9))
    )
}

@Composable
fun ThemePickerDialog(store:BookStore,onDismiss:()->Unit) {
    var selected by remember {mutableStateOf(store.setting().getString("theme","Sepia") ?: "Sepia")}
    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text(ui("Oxuma temaları"),fontWeight=FontWeight.Bold)},
        text={
            Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(ui("Hazır temalardan seç. Seçim bütün kitablarda saxlanılır."),
                    fontSize=13.sp,color=Color.Gray)
                LazyColumn(Modifier.heightIn(max=425.dp),
                    verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    items(ReadingThemes.presets.size) {i->
                        val t=ReadingThemes.presets[i]
                        Surface(
                            color=if(selected==t.name)Color(0xFFE8F3F0) else Color.Transparent,
                            shape=RoundedCornerShape(12.dp),
                            modifier=Modifier.fillMaxWidth().clickable {
                                selected=t.name
                                store.setting().edit().putString("theme",t.name).apply()
                            }
                        ) {
                            Row(Modifier.padding(8.dp),
                                verticalAlignment=Alignment.CenterVertically) {
                                Box(Modifier.size(width=56.dp,height=45.dp)
                                    .background(t.paper,RoundedCornerShape(8.dp)),
                                    contentAlignment=Alignment.Center) {
                                    Text(ui("Aa"),color=t.ink,fontSize=18.sp)
                                }
                                Spacer(Modifier.width(14.dp))
                                Text(ui(t.name),modifier=Modifier.weight(1f),
                                    fontWeight=if(selected==t.name)FontWeight.Bold else FontWeight.Normal)
                                RadioButton(selected=selected==t.name,
                                    onClick={
                                        selected=t.name
                                        store.setting().edit().putString("theme",t.name).apply()
                                    })
                            }
                        }
                    }
                }
            }
        },
        confirmButton={TextButton(onClick=onDismiss){Text(ui("Bağla"))}}
    )
}
