package az.leafreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID

/** Import user-chosen local cover without rewriting the book archive or discarding its original thumbnail. */
object CustomBookCover {
    fun apply(context:Context,store:BookStore,path:String,uri:Uri):String {
        val book=store.find(path) ?:error("Kitab tapılmadı")
        val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true}
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it,null,bounds)
        } ?:error("Şəkil seçilmədi")
        require(bounds.outWidth in 1..16000 && bounds.outHeight in 1..16000) {
            "Şəkil ölçüsü düzgün deyil"
        }
        var sample=1
        while(maxOf(bounds.outWidth,bounds.outHeight)/sample>1600)sample*=2
        val options=BitmapFactory.Options().apply {
            inSampleSize=sample
            inPreferredConfig=Bitmap.Config.RGB_565
        }
        val image=context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it,null,options)
        } ?:error("Şəkil oxunmadı")
        val covers=File(context.filesDir,"covers").apply {mkdirs()}
        val filename="custom_"+UUID.randomUUID()+".jpg"
        val target=File(covers,filename)
        try {
            target.outputStream().use {
                check(image.compress(Bitmap.CompressFormat.JPEG,88,it)){"Üz qabığı saxlanılmadı"}
            }
            store.change(path){record->
                if(!record.has("originalCover"))record.put("originalCover",book.optString("cover"))
                record.put("cover","covers/$filename")
                record.put("customCover",true)
            }
            return "covers/$filename"
        }catch(e:Exception) {
            target.delete()
            throw e
        }finally {image.recycle()}
    }
    fun restore(store:BookStore,path:String) {
        store.change(path){record->
            if(record.has("originalCover")) {
                record.put("cover",record.optString("originalCover"))
                record.remove("originalCover")
            }
            record.remove("customCover")
        }
    }
}
