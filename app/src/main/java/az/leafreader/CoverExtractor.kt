package az.leafreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

/** Extract a small offline library thumbnail without server requests. */
object CoverExtractor {
    fun extract(context: Context, bookFile: File, type: String, id: String): String? {
        val dir=File(context.filesDir,"covers")
        dir.mkdirs()
        val output=File(dir,id.substringBeforeLast('.')+".jpg")
        try {
            val image:Bitmap? = when(type) {
                "pdf" -> {
                    ParcelFileDescriptor.open(bookFile,ParcelFileDescriptor.MODE_READ_ONLY).use { desc ->
                        PdfRenderer(desc).use { renderer ->
                            renderer.openPage(0).use { page ->
                                val w=260
                                val h=(page.height*w.toFloat()/page.width).toInt().coerceAtLeast(1)
                                val bitmap=Bitmap.createBitmap(w,h,Bitmap.Config.RGB_565)
                                bitmap.eraseColor(Color.WHITE)
                                page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                bitmap
                            }
                        }
                    }
                }
                "cbz","epub" -> {
                    ZipFile(bookFile).use { archive ->
                        val candidates=archive.entries().asSequence().filter { entry ->
                            !entry.isDirectory && (entry.name.substringAfterLast('.').lowercase() in listOf("jpg","jpeg","png","webp"))
                        }.toList()
                        val selected=if(type=="epub")
                            candidates.firstOrNull {it.name.contains("cover",ignoreCase=true)}
                                ?: candidates.firstOrNull()
                        else candidates.sortedBy {it.name.lowercase()}.firstOrNull()
                        if(selected==null)null else archive.getInputStream(selected).use { stream ->
                            BitmapFactory.decodeStream(stream,null,BitmapFactory.Options().apply {
                                inSampleSize=4
                                inPreferredConfig=Bitmap.Config.RGB_565
                            })
                        }
                    }
                }
                else -> null
            }
            if(image!=null) {
                val thumbWidth=190
                val h=(image.height*thumbWidth.toFloat()/image.width).toInt().coerceIn(1,330)
                val thumbnail=Bitmap.createScaledBitmap(image,thumbWidth,h,true)
                FileOutputStream(output).use { thumbnail.compress(Bitmap.CompressFormat.JPEG,82,it) }
                if(thumbnail!==image)thumbnail.recycle()
                image.recycle()
                return "covers/"+output.name
            }
        }catch(_:Exception){ /* Missing or malformed cover falls back to a placeholder. */ }
        return null
    }
}
