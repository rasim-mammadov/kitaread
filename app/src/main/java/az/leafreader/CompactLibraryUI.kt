package az.leafreader

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

/** Compact list is fully navigable: tapping a cover/title opens, controls remain separate. */
@Composable
fun CompactLibraryBook(book:ReaderBook,store:BookStore,textSize:Int,density:Int,
    pinned:Boolean,selected:Boolean,selecting:Boolean,
    onOpen:()->Unit,onMore:()->Unit,onPin:()->Unit) {
    val context=LocalContext.current
    val cover=remember(book.cover) {
        if(book.cover.isBlank())null
        else runCatching {
            BitmapFactory.decodeFile(File(context.filesDir,book.cover).absolutePath)
        }.getOrNull()
    }
    Card(
        colors=CardDefaults.cardColors(containerColor=if(selected)
            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
        shape=RoundedCornerShape(12.dp),
        modifier=Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal=(6+density*2).dp,
            vertical=(3+density*2).dp),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(width=(40+density*4).dp,height=(54+density*4).dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick=onOpen),contentAlignment=Alignment.Center) {
                if(cover!=null)Image(cover.asImageBitmap(),null,Modifier.fillMaxSize(),
                    contentScale=ContentScale.Crop)
                else Icon(Icons.Default.MenuBook,null,Modifier.size(28.dp),
                    tint=MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f).clickable(onClick=onOpen),
                verticalArrangement=Arrangement.spacedBy(3.dp)) {
                Text(book.title,fontSize=textSize.sp,fontWeight=FontWeight.SemiBold,
                    maxLines=1,overflow=TextOverflow.Ellipsis)
                val author=remember(book.path){store.find(book.path)?.optString("author").orEmpty()}
                Text((if(author.isBlank())book.type.uppercase() else author)+" · "+
                    book.progress+"%",fontSize=(textSize-2).coerceAtLeast(10).sp,
                    maxLines=1,overflow=TextOverflow.Ellipsis,
                    color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if(selecting) Checkbox(checked=selected,onCheckedChange={onOpen()})
            else {
                IconButton(onClick=onPin,modifier=Modifier.size(37.dp)) {
                    Icon(Icons.Default.PushPin,ui(if(pinned)"Sabitləməni ləğv et" else "Kitabı sabitlə"),
                        modifier=Modifier.size(18.dp),
                        tint=if(pinned)MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.45f))
                }
                IconButton(onClick=onMore,modifier=Modifier.size(37.dp)) {
                    Icon(Icons.Default.MoreVert,ui("Ətraflı"),Modifier.size(22.dp))
                }
            }
        }
    }
}
