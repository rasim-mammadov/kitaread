package az.leafreader

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.FileProvider
import java.io.File

/** Export an independent, shareable PNG without exposing books or private data. */
object ReaderPassportShare {
    fun share(context:Context,title:String,books:Int,activeDays:Int,awards:Int) {
        val image=Bitmap.createBitmap(1080,675,Bitmap.Config.ARGB_8888)
        try {
            val canvas=Canvas(image)
            val paint=Paint(Paint.ANTI_ALIAS_FLAG)
            canvas.drawColor(Color.rgb(16,45,54))
            paint.color=Color.rgb(35,75,83)
            canvas.drawRoundRect(RectF(50f,50f,1030f,625f),42f,42f,paint)
            paint.color=Color.rgb(158,207,193)
            paint.textSize=37f
            paint.isFakeBoldText=true
            canvas.drawText("LEAF READER",95f,129f,paint)
            paint.textSize=27f
            paint.isFakeBoldText=false
            canvas.drawText("READER PASSPORT",95f,173f,paint)
            paint.color=Color.rgb(255,255,255)
            paint.textSize=54f
            paint.isFakeBoldText=true
            val display=title.take(27)
            canvas.drawText(display,95f,297f,paint)
            paint.color=Color.rgb(158,207,193)
            paint.textSize=31f
            paint.isFakeBoldText=false
            canvas.drawText("Books",95f,408f,paint)
            canvas.drawText("Active days",410f,408f,paint)
            canvas.drawText("Achievements",755f,408f,paint)
            paint.color=Color.WHITE
            paint.isFakeBoldText=true
            paint.textSize=68f
            canvas.drawText(books.toString(),95f,495f,paint)
            canvas.drawText(activeDays.toString(),410f,495f,paint)
            canvas.drawText(awards.toString(),755f,495f,paint)
            paint.isFakeBoldText=false
            paint.textSize=25f
            paint.color=Color.rgb(158,207,193)
            canvas.drawText("Offline reading journey",95f,568f,paint)
            val dir=File(context.cacheDir,"shared_passport").apply {mkdirs()}
            val file=File(dir,"leaf-reader-passport.png")
            file.outputStream().use {
                check(image.compress(Bitmap.CompressFormat.PNG,100,it)){"PNG export failed"}
            }
            val uri=FileProvider.getUriForFile(context,
                context.packageName+".fileprovider",file)
            val intent=Intent(Intent.ACTION_SEND).apply {
                type="image/png"
                putExtra(Intent.EXTRA_STREAM,uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData=android.content.ClipData.newUri(context.contentResolver,
                    "Reader passport",uri)
            }
            context.startActivity(Intent.createChooser(intent,"Reader passport"))
        }finally {image.recycle()}
    }
}
