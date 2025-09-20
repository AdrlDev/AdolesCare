package dev.adriele.adolescare.helpers

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import dev.adriele.adolescare.model.PdfWord

class PdfTextOverlay(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val words = mutableListOf<PdfWord>()
    private var scale = 1f

    fun setWords(newWords: List<PdfWord>, scaleFactor: Float) {
        words.clear()
        words.addAll(newWords)
        scale = scaleFactor
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Debug highlight: show boxes
        val paint = Paint().apply {
            color = Color.argb(60, 255, 255, 0)
            style = Paint.Style.FILL
        }
        words.forEach {
            val rect = RectF(
                it.rect.left * scale,
                it.rect.top * scale,
                it.rect.right * scale,
                it.rect.bottom * scale
            )
            canvas.drawRect(rect, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x / scale
            val y = event.y / scale
            words.find { it.rect.contains(x, y) }?.let { word ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("PDF Text", word.text))
                Toast.makeText(context, "Copied: ${word.text}", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
