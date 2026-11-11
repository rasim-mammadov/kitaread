package az.leafreader

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object ComicSpread {
    /** Compose the visible two-page spread off the UI thread. */
    fun combine(first:Bitmap,second:Bitmap?,rtl:Boolean):Bitmap {
        if(second==null)return first
        val height=maxOf(first.height,second.height).coerceAtMost(3500)
        val w1=(first.width.toFloat()*height/first.height).toInt().coerceAtLeast(1)
        val w2=(second.width.toFloat()*height/second.height).toInt().coerceAtLeast(1)
        val output=Bitmap.createBitmap(w1+w2,height,Bitmap.Config.RGB_565)
        val canvas=Canvas(output)
        canvas.drawColor(Color.WHITE)
        val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val left=if(rtl)second else first
        val right=if(rtl)first else second
        val leftWidth=if(rtl)w2 else w1
        canvas.drawBitmap(left,null,android.graphics.Rect(0,0,leftWidth,height),p)
        canvas.drawBitmap(right,null,android.graphics.Rect(leftWidth,0,w1+w2,height),p)
        return output
    }
}
