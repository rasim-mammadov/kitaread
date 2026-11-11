package az.leafreader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Editable book metadata, genre and multiple user-named collections. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsSheet(book:ReaderBook,store:BookStore,onClose:()->Unit,onChanged:()->Unit) {
    val source=remember(book.path){store.find(book.path)}
    var title by remember(book.path){mutableStateOf(source?.optString("title").orEmpty())}
    var author by remember(book.path){mutableStateOf(source?.optString("author").orEmpty())}
    var genre by remember(book.path){mutableStateOf(source?.optString("genre",GenreCatalog.UNKNOWN) ?: GenreCatalog.UNKNOWN)}
    var newCollection by remember(book.path){mutableStateOf("")}
    var collections by remember(book.path){
        mutableStateOf((source?.optJSONArray("collections") ?: JSONArray()).let {arr->
            (0 until arr.length()).map {arr.optString(it)}
        })
    }
    var rating by remember(book.path){mutableFloatStateOf(source?.optDouble("rating",0.0)?.toFloat() ?: 0f)}
    var status by remember(book.path){mutableStateOf(source?.optString("status","Oxunmayıb") ?: "Oxunmayıb")}
    ModalBottomSheet(onDismissRequest=onClose,containerColor=Color(0xFFF8F6F0)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal=24.dp).padding(bottom=24.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Kitab detalları",fontSize=24.sp,fontWeight=FontWeight.Bold)
            OutlinedTextField(title,{title=it},label={Text("Kitabın adı")},
                modifier=Modifier.fillMaxWidth(),singleLine=true)
            OutlinedTextField(author,{author=it},label={Text("Müəllif")},
                modifier=Modifier.fillMaxWidth(),singleLine=true)
            Text("Oxu statusu",fontWeight=FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("Oxunmayıb","Oxunur","Bitirilib","Yarımçıq saxlanılıb").forEach {item->
                    FilterChip(selected=status==item,onClick={status=item},label={Text(item)})
                }
            }
            Text("Janr",fontWeight=FontWeight.SemiBold)
            Column(Modifier.fillMaxWidth()) {
                GenreCatalog.genres.chunked(3).forEach { group->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                        group.forEach { g->
                            FilterChip(selected=genre==g,onClick={genre=g},
                                label={Text(g,fontSize=11.sp)})
                        }
                    }
                }
            }
            Text("Şəxsi kolleksiyalar",fontWeight=FontWeight.SemiBold)
            if(collections.isEmpty())Text("Hələ kolleksiya yoxdur",color=Color.Gray,fontSize=13.sp)
            collections.forEach {label->
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text(label,modifier=Modifier.weight(1f))
                    IconButton(onClick={
                        store.removeFromCollection(book.path,label)
                        collections=collections.filterNot {it==label}
                        onChanged()
                    }) {Icon(Icons.Default.DeleteOutline,"Kolleksiyadan çıxar")}
                }
            }
            Row(verticalAlignment=Alignment.CenterVertically) {
                OutlinedTextField(newCollection,{newCollection=it},
                    label={Text("Yeni kolleksiya")},modifier=Modifier.weight(1f),
                    singleLine=true)
                Spacer(Modifier.width(8.dp))
                Button(onClick={
                    val value=newCollection.trim().take(50)
                    if(value.isNotBlank()) {
                        store.addToCollection(book.path,value)
                        collections=(collections+value).distinct()
                        newCollection=""
                        onChanged()
                    }
                },enabled=newCollection.isNotBlank()) {Text("Əlavə et")}
            }
            Text("Şəxsi qiymətləndirmə: "+
                (if(rating<0.5f)"Hələ yoxdur" else "${rating.toInt()} / 5"),
                fontWeight=FontWeight.SemiBold)
            Slider(value=rating,onValueChange={rating=it},
                valueRange=0f..5f,steps=4)
            Text("Format: ${book.type.uppercase()} · Progress: ${book.progress}%",
                fontSize=13.sp,color=Color.Gray)
            if(book.completedAt.isNotBlank())
                Text("Bitirilmə tarixi: ${book.completedAt}",color=Color.Gray,fontSize=13.sp)
            Button(onClick={
                store.change(book.path){row->
                    row.put("title",title.trim().ifBlank{book.title})
                    row.put("author",author.trim())
                    row.put("genre",genre)
                    row.put("rating",rating.toInt())
                    row.put("status",status)
                    if(status=="Bitirilib") {
                        row.put("progress",100)
                        if(row.optString("completedAt").isEmpty()) {
                            row.put("completedAt",
                                SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date()))
                        }
                    } else if(row.optString("status")!="Bitirilib" && row.optInt("progress")==100) {
                        row.put("progress",99)
                    }
                }
                onChanged()
                onClose()
            },modifier=Modifier.fillMaxWidth().height(52.dp)) {Text("Dəyişiklikləri saxla")}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksSheet(
    store:BookStore,
    bookPath:String?=null,
    onOpen:(String,BookStore.BookMark)->Unit,
    onClose:()->Unit,
    onChanged:()->Unit
) {
    var refresh by remember {mutableIntStateOf(0)}
    val books=remember(refresh,bookPath) {store.books()
        .filter {bookPath==null || it.path==bookPath}}
    val marks=remember(refresh,bookPath) {
        books.flatMap {book->store.bookmarks(book.path).map {book to it}}
            .sortedByDescending {it.second.createdAt}
    }
    ModalBottomSheet(onDismissRequest=onClose,containerColor=Color(0xFFF8F6F0)) {
        Column(Modifier.fillMaxWidth().padding(horizontal=22.dp).padding(bottom=18.dp)) {
            Text("Əlfəcinlərim",fontSize=24.sp,fontWeight=FontWeight.Bold)
            Text("${marks.size} yadda saxlanmış mövqe",fontSize=13.sp,color=Color.Gray)
            Spacer(Modifier.height(10.dp))
            if(marks.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(150.dp),contentAlignment=Alignment.Center) {
                    Text("Hələ əlfəcin əlavə etməmisən",color=Color.Gray)
                }
            }else LazyColumn(contentPadding=PaddingValues(bottom=25.dp)) {
                items(marks,key={it.first.path+":"+
                    it.second.chapter+":"+it.second.fraction+":"+it.second.createdAt}) { (book,mark)->
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        ListItem(
                            modifier=Modifier.weight(1f).clickable {onOpen(book.path,mark)},
                            headlineContent={Text(book.title,maxLines=1)},
                            supportingContent={
                                Text((if(book.type=="epub")"Fəsil " else "Səhifə ")+
                                    (mark.chapter+1)+
                                    (if(book.type=="epub")" · ${(mark.fraction*100).toInt()}%" else ""))
                            },
                            leadingContent={Icon(Icons.Default.Bookmark,null)}
                        )
                        IconButton(onClick={
                            store.removeBookmark(book.path,mark)
                            refresh++
                            onChanged()
                        }) {
                            Icon(Icons.Default.DeleteOutline,"Əlfəcini sil")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
