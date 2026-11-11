package az.leafreader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.util.zip.ZipFile
import kotlin.math.min

object ReaderMedia {
    fun count(file:File,type:String):Int= when(type) {
        "pdf" -> ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY).use {
            PdfRenderer(it).use { pdf->pdf.pageCount }
        }
        "cbz" -> ZipFile(file).use {zip->entries(zip).size}
        else -> 0
    }
    private fun entries(zip:ZipFile):List<String> = zip.entries().asSequence()
        .filter {!it.isDirectory && it.name.substringAfterLast('.').lowercase() in
            listOf("jpg","jpeg","png","webp") }
        .map {it.name}.sortedWith(ComicPageOrder.comparator).toList()

    fun page(file:File,type:String,index:Int):Bitmap = when(type) {
        "pdf" -> ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY).use {desc->
            PdfRenderer(desc).use {pdf->
                pdf.openPage(index).use {page->
                    val scale=min(3f,1900f/page.width)
                    val output=Bitmap.createBitmap(
                        (page.width*scale).toInt().coerceAtLeast(1),
                        (page.height*scale).toInt().coerceAtLeast(1),Bitmap.Config.ARGB_8888
                    )
                    output.eraseColor(Color.WHITE)
                    page.render(output,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    output
                }
            }
        }
        "cbz" -> ZipFile(file).use {zip->
            val name=entries(zip)[index]
            val entry=zip.getEntry(name)
            val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true}
            zip.getInputStream(entry).use {BitmapFactory.decodeStream(it,null,bounds)}
            val maxDim=maxOf(bounds.outWidth,bounds.outHeight)
            var sample=1
            while(maxDim/sample>2500)sample*=2
            val options=BitmapFactory.Options().apply {
                inSampleSize=sample
                inPreferredConfig=Bitmap.Config.RGB_565
            }
            zip.getInputStream(entry).use {
                BitmapFactory.decodeStream(it,null,options)
                    ?:throw IllegalArgumentException("Şəkil açılmadı")
            }
        }
        else -> throw IllegalArgumentException("Unsupported format: $type")
    }
}
