package az.leafreader

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Image-based reader surface shared by PDF and CBZ.
 * Pinch, double-tap, one-finger pan and edge-safe horizontal page navigation.
 */
class ZoomPageView(
    context: Context,
    private val onNext: () -> Unit,
    private val onPrevious: () -> Unit,
    private val rtlSwipe:Boolean=false,
    private val tapZones:Boolean=false,
    private val onToggleControls: () -> Unit,
    private val onZoomChanged: (Float) -> Unit
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var bitmap: Bitmap? = null
    private var outgoingBitmap: Bitmap? = null
    private var transitionProgress=1f
    private var transition:ValueAnimator?=null
    private var base = 1f
    private var zoom = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var fitWidth = false
    private var requestedZoom = 1f
    private var dragging = false

    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                setZoom((zoom * detector.scaleFactor).coerceIn(1f, 8f), detector.focusX, detector.focusY)
                return true
            }
        })
    private val gestures = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (tapZones && zoom <= 1.05f && width > 0) {
                    // Manga touch zones mirror page progression. Centre toggles toolbar.
                    if (e.x < width * 0.22f) {
                        if (rtlSwipe) onNext() else onPrevious()
                        return true
                    }
                    if (e.x > width * 0.78f) {
                        if (rtlSwipe) onPrevious() else onNext()
                        return true
                    }
                }
                onToggleControls()
                return true
            }
            override fun onDoubleTap(e: MotionEvent): Boolean {
                setZoom(if (zoom > 1.05f) 1f else 2f, e.x, e.y)
                return true
            }
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (zoom > 1.05f || e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                if (abs(dx) > 85f * resources.displayMetrics.density && abs(dx) > abs(dy) * 1.2f) {
                    if ((dx < 0) != rtlSwipe) onNext() else onPrevious()
                    return true
                }
                return false
            }
        })

    fun setPage(image: Bitmap, initialZoom: Float, widthMode: Boolean,
        smooth:Boolean=true) {
        transition?.cancel()
        val previous=bitmap?.takeIf {it!==image && !it.isRecycled}
        outgoingBitmap=if(smooth)previous else null
        bitmap=image
        requestedZoom=initialZoom.coerceIn(1f, 8f)
        fitWidth=widthMode
        zoom=requestedZoom
        transitionProgress=if(outgoingBitmap!=null)0f else 1f
        resetMatrix()
        if(outgoingBitmap!=null) {
            transition=ValueAnimator.ofFloat(0f,1f).apply {
                duration=210L
                interpolator=android.view.animation.DecelerateInterpolator()
                addUpdateListener {anim->
                    transitionProgress=anim.animatedValue as Float
                    if(transitionProgress>=1f) outgoingBitmap=null
                    invalidate()
                }
                start()
            }
        }
        invalidate()
    }

    fun fitWidthEnabled():Boolean=fitWidth

    fun setFitWidth(enabled: Boolean) {
        fitWidth = enabled
        resetMatrix()
    }
    fun resetZoom() = setZoom(1f, width / 2f, height / 2f)
    fun zoomLevel(): Float = zoom

    private fun resetMatrix() {
        val img = bitmap ?: return
        if (width <= 0 || height <= 0) return
        base = if (fitWidth) width.toFloat() / img.width else min(width.toFloat() / img.width, height.toFloat() / img.height)
        offsetX = (width - img.width * base * zoom) / 2f
        offsetY = (height - img.height * base * zoom) / 2f
        clampOffsets()
        invalidate()
    }

    private fun setZoom(value: Float, pivotX: Float, pivotY: Float) {
        val before = zoom
        zoom = value.coerceIn(1f, 8f)
        val ratio = zoom / before
        offsetX = pivotX - (pivotX - offsetX) * ratio
        offsetY = pivotY - (pivotY - offsetY) * ratio
        clampOffsets()
        onZoomChanged(zoom)
        invalidate()
    }

    private fun clampOffsets() {
        val img = bitmap ?: return
        val w = img.width * base * zoom
        val h = img.height * base * zoom
        offsetX = if (w <= width) (width - w) / 2f else offsetX.coerceIn(width - w, 0f)
        offsetY = if (h <= height) (height - h) / 2f else offsetY.coerceIn(height - h, 0f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetMatrix()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(18, 20, 23))
        val img = bitmap ?: return
        val old=outgoingBitmap
        if(old!=null && !old.isRecycled && transitionProgress<1f) {
            // Outgoing image remains visible while the next decoded page fades in.
            val fit=min(width.toFloat()/old.width,height.toFloat()/old.height)
            canvas.save()
            canvas.translate((width-old.width*fit)/2f,(height-old.height*fit)/2f)
            canvas.scale(fit,fit)
            paint.alpha=255
            canvas.drawBitmap(old,0f,0f,paint)
            canvas.restore()
        }
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(base * zoom, base * zoom)
        paint.alpha=(255*transitionProgress).toInt().coerceIn(0,255)
        canvas.drawBitmap(img, 0f, 0f, paint)
        paint.alpha=255
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestures.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x; lastY = event.y; dragging = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                lastX = event.x; lastY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && event.pointerCount == 1) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    val img = bitmap
                    if (img != null && (zoom > 1.05f || img.height * base > height + 1)) {
                        if (abs(dx) + abs(dy) > 2) dragging = true
                        offsetX += dx; offsetY += dy
                        clampOffsets(); invalidate()
                    }
                }
                lastX = event.x; lastY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragging) performClick()
                dragging = false
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        transition?.cancel()
        outgoingBitmap=null
        super.onDetachedFromWindow()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
