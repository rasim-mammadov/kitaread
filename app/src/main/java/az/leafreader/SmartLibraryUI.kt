package az.leafreader

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

/** Compact cover-first layout, with optional library multi-selection. */
@Composable
fun SmartGridBook(
    book:ReaderBook,
    store:BookStore,
    textSize:Int,
    density:Int,
    pinned:Boolean,
    selected:Boolean,
    selecting:Boolean,
    onOpen:()->Unit,
    onMore:()->Unit,
    onPin:()->Unit,
    onSelect:()->Unit
) {
    val context=LocalContext.current
    val cover=remember(book.cover) {
        if(book.cover.isNotBlank()) {
            try { BitmapFactory.decodeFile(File(context.filesDir,book.cover).absolutePath) }
            catch(_:Exception){null}
        } else null
    }
    Card(
        modifier=Modifier.fillMaxWidth(),
        shape=RoundedCornerShape(16.dp),
        colors=CardDefaults.cardColors(containerColor=if(selected)
            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding((6+density*2).dp),verticalArrangement=Arrangement.spacedBy((3+density*2).dp)) {
            Box(Modifier.fillMaxWidth().height(165.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick=if(selecting)onSelect else onOpen)
                .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment=Alignment.Center) {
                if(cover!=null) {
                    Image(cover.asImageBitmap(),null,Modifier.fillMaxSize(),
                        contentScale=ContentScale.Crop)
                } else Icon(Icons.Default.MenuBook,null,Modifier.size(50.dp),
                    tint=MaterialTheme.colorScheme.primary)
                if(selecting) Checkbox(checked=selected,onCheckedChange={onSelect()},
                    modifier=Modifier.align(Alignment.TopEnd))
            }
            Text(book.title,maxLines=2,overflow=TextOverflow.Ellipsis,
                modifier=Modifier.fillMaxWidth().clickable(onClick=if(selecting)onSelect else onOpen),
                fontSize=textSize.sp,fontWeight=FontWeight.SemiBold,lineHeight=(textSize+4).sp)
            val author=remember(book.path) {store.find(book.path)?.optString("author").orEmpty()}
            if(author.isNotBlank())Text(author,maxLines=1,overflow=TextOverflow.Ellipsis,
                fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(progress={book.progress/100f},
                modifier=Modifier.fillMaxWidth().height(4.dp),
                color=MaterialTheme.colorScheme.primary)
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text("${book.progress}%",fontSize=11.sp,
                    color=MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier=Modifier.weight(1f))
                if(!selecting) IconButton(onClick=onPin,modifier=Modifier.size(32.dp)) {
                    Icon(Icons.Default.PushPin,
                        ui(if(pinned)"Sabitləməni ləğv et" else "Kitabı sabitlə"),
                        modifier=Modifier.size(16.dp),
                        tint=if(pinned)MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.45f))
                }
                if(!selecting) TextButton(onClick=onMore,
                    contentPadding=PaddingValues(horizontal=5.dp,vertical=2.dp)) {
                    Text(ui("Ətraflı"),fontSize=11.sp)
                }
            }
        }
    }
}
