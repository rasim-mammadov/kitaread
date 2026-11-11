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
    language:String,onLanguage:(String)->Unit,
    onExport:()->Unit,onImport:()->Unit,
    onFullExport:()->Unit,onFullImport:()->Unit,
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
                    Icon(Icons.Default.ArrowBack,ui("Geri"))
                }
                Text(ui(if(section=="Language")"Dil" else section),fontSize=25.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
                IconButton(onClick=onClose) {Icon(Icons.Default.Close,ui("Bağla"))}
            }
            HorizontalDivider()
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(vertical=12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                if(section=="Settings") {
                    listOf(
                        Triple("🎨","Appearance","Appearance"),
                        Triple("🌐","Language","Language"),
                        Triple("📚","Library","Library"),
                        Triple("💾","Data & Backup","Data & Backup"),
                        Triple("ℹ️","About","About")
                    ).forEach { (emoji,title,_)->
                        ListItem(
                            leadingContent={Text(emoji,fontSize=23.sp)},
                            headlineContent={Text(ui(if(title=="Language")"Dil" else title))},
                            trailingContent={Icon(Icons.Default.ChevronRight,null)},
                            modifier=Modifier.clickable {section=title})
                        HorizontalDivider()
                    }
                }
                if(section=="Language") {
                    Text(ui("Kitabın öz dili dəyişdirilmir."),fontSize=13.sp,
                        color=MaterialTheme.colorScheme.onSurfaceVariant)
                    listOf(
                        Triple("🇦🇿","az","Azərbaycan dili"),
                        Triple("🇷🇺","ru","Rus dili"),
                        Triple("🇬🇧","en","İngilis dili")
                    ).forEach {(emoji,code,label)->
                        ListItem(
                            leadingContent={Text(emoji,fontSize=23.sp)},
                            headlineContent={Text(ui(label))},
                            trailingContent={RadioButton(selected=language==code,
                                onClick={onLanguage(code)})},
                            modifier=Modifier.clickable {onLanguage(code)}
                        )
                        HorizontalDivider()
                    }
                }
                if(section=="Appearance") {
                    Text(ui("App Theme — bütün ekranlar"),fontWeight=FontWeight.SemiBold)
                    ReadingThemes.presets.chunked(2).forEach {pair->
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            pair.forEach {t->
                                Surface(modifier=Modifier.weight(1f).clickable {onTheme(t.name)},
                                    color=t.paper,shape=RoundedCornerShape(12.dp),
                                    border=BorderStroke(if(appTheme==t.name)2.dp else 1.dp,
                                        if(appTheme==t.name)Color(0xFF267A71) else Color.Gray)) {
                                    Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
                                        Text(ui("Aa"),fontSize=18.sp,color=t.ink)
                                        Spacer(Modifier.width(8.dp))
                                        Text(ui(t.name),color=t.ink,fontSize=13.sp)
                                    }
                                }
                            }
                            if(pair.size==1)Spacer(Modifier.weight(1f))
                        }
                    }
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Text(ui("Reader Theme tətbiq temasını izləsin"),
                            modifier=Modifier.weight(1f))
                        Switch(follow,{
                            follow=it;prefs.edit().putBoolean("readerFollowsApp",it).apply()
                        })
                    }
                    if(!follow) {
                        Text(ui("Kitab üçün ayrıca tema"),fontWeight=FontWeight.SemiBold)
                        ReadingThemes.presets.chunked(3).forEach {row->
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                                row.forEach {t->
                                    FilterChip(selected=readerTheme==t.name,onClick={
                                        readerTheme=t.name
                                        prefs.edit().putString("readerTheme",t.name).apply()
                                    },label={Text(ui(t.name))})
                                    Spacer(Modifier.width(4.dp))
                                }
                            }
                        }
                    }
                }
                if(section=="Library") {
                    Text(ui("Sıralama"),fontWeight=FontWeight.SemiBold)
                    listOf("Son oxunanlar","Əlavə edilmə tarixi","A–Z","Z–A","Progress")
                        .forEach {value->
                            Row(Modifier.fillMaxWidth().clickable {
                                sort=value;prefs.edit().putString("librarySort",value).apply();onSort(value)
                            },verticalAlignment=Alignment.CenterVertically) {
                                RadioButton(selected=sort==value,onClick={
                                    sort=value
                                    prefs.edit().putString("librarySort",value).apply()
                                    onSort(value)
                                })
                                Text(ui(value))
                            }
                        }
                    Text(ui("Bitirilmiş kitablar aşağıda saxlanılır."),fontSize=12.sp)
                    HorizontalDivider()
                    Text(ui("Gizlədilmiş kitablar: ${hidden.size}"),fontWeight=FontWeight.SemiBold)
                    hidden.forEach {b->
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            Text(b.optString("title"),modifier=Modifier.weight(1f))
                            TextButton(onClick={
                                onRestore(b.optString("path"))
                                hiddenRefresh++
                            }) {
                                Text(ui("Geri qaytar"))
                            }
                        }
                    }
                }
                if(section=="Data & Backup") {
                    Text(ui("Tam ehtiyat nüsxə kitab fayllarını və bütün qeydləri saxlayır."),
                        fontSize=13.sp)
                    Button(onClick=onFullExport,modifier=Modifier.fillMaxWidth()) {
                        Text(ui("Tam backup (ZIP)"))
                    }
                    OutlinedButton(onClick=onFullImport,modifier=Modifier.fillMaxWidth()) {
                        Text(ui("Tam bərpa (ZIP)"))
                    }
                    HorizontalDivider()
                    Text(ui("Yalnız metadata: kitab faylları daxil deyil."),fontSize=13.sp)
                    Button(onClick=onExport,modifier=Modifier.fillMaxWidth()) {Text(ui("Export JSON"))}
                    OutlinedButton(onClick=onImport,modifier=Modifier.fillMaxWidth()) {Text(ui("Import JSON"))}
                }
                if(section=="About") {
                    Text(ui("KitaRead"),fontSize=22.sp,fontWeight=FontWeight.Bold)
                    Text(ui("4.1 Preview · Offline EPUB / PDF / CBZ / Markdown"))
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
            Text(ui("Ətraflı filtrlər"),fontSize=24.sp,fontWeight=FontWeight.Bold)
            Text(ui("Janr"))
            (listOf(GenreCatalog.UNKNOWN)+genres.filter {it!=GenreCatalog.UNKNOWN})
                .distinct().chunked(3).forEach {group->
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                        group.forEach {g->
                            FilterChip(selected=genre==g,onClick={onGenre(g)},
                                label={Text(ui(if(g==GenreCatalog.UNKNOWN)"Bütün janrlar" else g))})
                        }
                    }
                }
            Text(ui("Format"))
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                listOf("Hamısı","EPUB","PDF","CBZ","MD").forEach {v->
                    FilterChip(selected=format==v,onClick={onFormat(v)},label={Text(ui(v))})
                    Spacer(Modifier.width(4.dp))
                }
            }
            Text(ui("Kolleksiya"))
            (listOf("")+collections).chunked(3).forEach {group->
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    group.forEach {c->
                        FilterChip(selected=collection==c,onClick={onCollection(c)},
                            label={Text(ui(c.ifBlank {"Hamısı"}))})
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }
            OutlinedButton(onClick={
                onGenre(GenreCatalog.UNKNOWN);onFormat("Hamısı");onCollection("")
            },modifier=Modifier.fillMaxWidth()) {Text(ui("Filtrləri sıfırla"))}
            Button(onClick=onClose,modifier=Modifier.fillMaxWidth()) {Text(ui("Kitabları göstər"))}
        }
    }
}
