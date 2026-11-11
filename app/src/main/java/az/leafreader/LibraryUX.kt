@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package az.leafreader

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object LibraryPalette {
    fun scheme(name:String):ColorScheme {
        val preset=ReadingThemes.presets.firstOrNull {it.name==name} ?:ReadingThemes.presets.first()
        val dark=name in setOf("Dark","AMOLED","Midnight","Graphite")
        val paper=preset.paper
        val ink=preset.ink
        val surface=when {
            dark->Color(0xFF252D35)
            name=="Sepia"->Color(0xFFFFF6E6)
            name=="Sage"->Color(0xFFF0F6EE)
            name=="Ocean"->Color(0xFFF0F8FC)
            else->Color.White
        }
        return if(dark) darkColorScheme(
            primary=Color(0xFF91CCBF),onPrimary=Color(0xFF123B35),
            background=paper,onBackground=ink,surface=surface,onSurface=ink,
            surfaceVariant=surface,onSurfaceVariant=ink,
            secondary=Color(0xFF91CCBF)
        ) else lightColorScheme(
            primary=Color(0xFF267A71),onPrimary=Color.White,
            background=paper,onBackground=ink,surface=surface,onSurface=ink,
            surfaceVariant=surface,onSurfaceVariant=ink,
            secondary=Color(0xFF267A71)
        )
    }
}

@Composable
fun LibrarySettingsSheet(
    store:BookStore,appTheme:String,onTheme:(String)->Unit,
    onExport:()->Unit,onImport:()->Unit,
    onRestore:(String)->Unit,onSort:(String)->Unit,onClose:()->Unit
) {
    var section by remember {mutableStateOf("Settings")}
    val prefs=store.setting()
    var follow by remember {mutableStateOf(prefs.getBoolean("readerFollowsApp",true))}
    var readerTheme by remember {mutableStateOf(prefs.getString("readerTheme","Sepia") ?: "Sepia")}
    var sort by remember {mutableStateOf(prefs.getString("librarySort","Son oxunanlar") ?: "Son oxunanlar")}
    var hiddenRefresh by remember {mutableIntStateOf(0)}
    val hidden=remember(hiddenRefresh) {store.records().filter {it.optBoolean("hidden",false)}}
    ModalBottomSheet(onDismissRequest=onClose) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.88f).padding(horizontal=20.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                if(section!="Settings")IconButton(onClick={section="Settings"}) {
                    Icon(Icons.Default.ArrowBack,"Geri")
                }
                Text(section,fontSize=25.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
                IconButton(onClick=onClose) {Icon(Icons.Default.Close,"Bağla")}
            }
            HorizontalDivider()
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(vertical=12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                if(section=="Settings") {
                    listOf("Appearance" to "Qlobal və oxuma temaları",
                        "Reading" to "Oxuma parametrləri",
                        "Library" to "Sıralama və gizli kitablar",
                        "Data & Backup" to "Export / Import",
                        "About" to "Tətbiq məlumatları").forEach { (title,subtitle)->
                        ListItem(headlineContent={Text(title)},supportingContent={Text(subtitle)},
                            trailingContent={Icon(Icons.Default.ChevronRight,null)},
                            modifier=Modifier.clickable {section=title})
                        HorizontalDivider()
                    }
                }
                if(section=="Appearance") {
                    Text("App Theme — bütün ekranlar",fontWeight=FontWeight.SemiBold)
                    ReadingThemes.presets.chunked(2).forEach {pair->
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            pair.forEach {t->
                                Surface(modifier=Modifier.weight(1f).clickable {onTheme(t.name)},
                                    color=t.paper,shape=RoundedCornerShape(12.dp),
                                    border=BorderStroke(if(appTheme==t.name)2.dp else 1.dp,
                                        if(appTheme==t.name)Color(0xFF267A71) else Color.Gray)) {
                                    Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
                                        Text("Aa",fontSize=18.sp,color=t.ink)
                                        Spacer(Modifier.width(8.dp))
                                        Text(t.name,color=t.ink,fontSize=13.sp)
                                    }
                                }
                            }
                            if(pair.size==1)Spacer(Modifier.weight(1f))
                        }
                    }
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text("Reader Theme tətbiq temasını izləsin",
                            modifier=Modifier.weight(1f))
                        Switch(follow,{
                            follow=it;prefs.edit().putBoolean("readerFollowsApp",it).apply()
                        })
                    }
                    if(!follow) {
                        Text("Kitab üçün ayrıca tema",fontWeight=FontWeight.SemiBold)
                        ReadingThemes.presets.chunked(3).forEach {row->
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                                row.forEach {t->
                                    FilterChip(selected=readerTheme==t.name,onClick={
                                        readerTheme=t.name
                                        prefs.edit().putString("readerTheme",t.name).apply()
                                    },label={Text(t.name)})
                                    Spacer(Modifier.width(4.dp))
                                }
                            }
                        }
                    }
                }
                if(section=="Library") {
                    Text("Sıralama",fontWeight=FontWeight.SemiBold)
                    listOf("Son oxunanlar","Əlavə edilmə tarixi","A–Z","Z–A","Progress")
                        .forEach {value->
                            Row(Modifier.fillMaxWidth().clickable {
                                sort=value;prefs.edit().putString("librarySort",value).apply();onSort(value)
                            },verticalAlignment=Alignment.CenterVertically) {
                                RadioButton(selected=sort==value,onClick={
                                    sort=value;prefs.edit().putString("librarySort",value).apply()
                                })
                                Text(value)
                            }
                        }
                    Text("Bitirilmiş kitablar aşağıda saxlanılır.",fontSize=12.sp)
                    HorizontalDivider()
                    Text("Gizlədilmiş kitablar: ${hidden.size}",fontWeight=FontWeight.SemiBold)
                    hidden.forEach {b->
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            Text(b.optString("title"),modifier=Modifier.weight(1f))
                            TextButton(onClick={
                                onRestore(b.optString("path"))
                                hiddenRefresh++
                            }) {
                                Text("Geri qaytar")
                            }
                        }
                    }
                }
                if(section=="Data & Backup") {
                    Text("Yalnız metadata: kitab faylları daxil deyil.",fontSize=13.sp)
                    Button(onClick=onExport,modifier=Modifier.fillMaxWidth()) {Text("Export JSON")}
                    OutlinedButton(onClick=onImport,modifier=Modifier.fillMaxWidth()) {Text("Import JSON")}
                }
                if(section=="Reading") {
                    Text("Şrift, sətirarası məsafə, ekran istiqaməti və zoom kitabın "+
                        "daxilindəki Aa menyusundan dəyişdirilir.")
                    Text("Qlobal Reader Theme seçimi Appearance bölməsindədir.")
                }
                if(section=="About") {
                    Text("Leaf Reader",fontSize=22.sp,fontWeight=FontWeight.Bold)
                    Text("3.3.3 Preview · Offline EPUB / PDF / CBZ")
                }
            }
        }
    }
}

@Composable
fun AdvancedFiltersSheet(
    genres:List<String>,collections:List<String>,genre:String,format:String,
    collection:String,onGenre:(String)->Unit,onFormat:(String)->Unit,
    onCollection:(String)->Unit,onClose:()->Unit
) {
    ModalBottomSheet(onDismissRequest=onClose) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal=24.dp).padding(bottom=24.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Ətraflı filtrlər",fontSize=24.sp,fontWeight=FontWeight.Bold)
            Text("Janr")
            (listOf(GenreCatalog.UNKNOWN)+genres.filter {it!=GenreCatalog.UNKNOWN})
                .distinct().chunked(3).forEach {group->
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                        group.forEach {g->
                            FilterChip(selected=genre==g,onClick={onGenre(g)},
                                label={Text(if(g==GenreCatalog.UNKNOWN)"Bütün janrlar" else g)})
                        }
                    }
                }
            Text("Format")
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                listOf("Hamısı","EPUB","PDF","CBZ").forEach {v->
                    FilterChip(selected=format==v,onClick={onFormat(v)},label={Text(v)})
                    Spacer(Modifier.width(4.dp))
                }
            }
            Text("Kolleksiya")
            (listOf("")+collections).chunked(3).forEach {group->
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    group.forEach {c->
                        FilterChip(selected=collection==c,onClick={onCollection(c)},
                            label={Text(c.ifBlank {"Hamısı"})})
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }
            OutlinedButton(onClick={
                onGenre(GenreCatalog.UNKNOWN);onFormat("Hamısı");onCollection("")
            },modifier=Modifier.fillMaxWidth()) {Text("Filtrləri sıfırla")}
            Button(onClick=onClose,modifier=Modifier.fillMaxWidth()) {Text("Kitabları göstər")}
        }
    }
}
