package az.leafreader

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Lazy, local-only CBZ webtoon reader. Only visible pages are decoded. */
@Composable
fun ComicWebtoon(
    bookFile:File,
    pageCount:Int,
    initialPage:Int,
    pageGap:Int,
    onPageChanged:(Int)->Unit,
    onToggleControls:()->Unit
) {
    val state=rememberLazyListState(initialFirstVisibleItemIndex=
        initialPage.coerceIn(0,(pageCount-1).coerceAtLeast(0)))
    LaunchedEffect(state,pageCount) {
        snapshotFlow {state.firstVisibleItemIndex}.collect { index->
            if(index in 0 until pageCount)onPageChanged(index)
        }
    }
    LazyColumn(
        state=state,
        modifier=Modifier.fillMaxSize().background(Color(0xFF121417)),
        verticalArrangement=Arrangement.spacedBy(pageGap.coerceIn(0,40).dp),
        contentPadding=PaddingValues(bottom=16.dp)
    ) {
        items(pageCount,key={it}) { index->
            var page by remember(bookFile,index) {mutableStateOf<Bitmap?>(null)}
            var failure by remember(bookFile,index) {mutableStateOf(false)}
            LaunchedEffect(bookFile,index) {
                try {page=withContext(Dispatchers.IO){ReaderMedia.page(bookFile,"cbz",index)}}
                catch(_:Exception){failure=true}
            }
            if(page!=null) {
                Image(page!!.asImageBitmap(),contentDescription=null,
                    modifier=Modifier.fillMaxWidth().clickable(onClick=onToggleControls),
                    contentScale=ContentScale.FillWidth)
            }else {
                Box(Modifier.fillMaxWidth().height(280.dp),contentAlignment=Alignment.Center) {
                    if(failure)Text(ui("Səhifə yüklənmədi")+" · ${index+1}",color=Color.White)
                    else CircularProgressIndicator()
                }
            }
        }
    }
}
