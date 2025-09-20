package dev.adriele.adolescare.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import dev.adriele.adolescare.model.PdfWord
import androidx.core.graphics.toColorInt

class PdfPageView(context: Context) : FrameLayout(context) {
    private val imageView = ImageView(context)

    init {
        addView(imageView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    fun setPage(bitmap: Bitmap, words: List<PdfWord>) {
        imageView.setImageBitmap(bitmap)

        // Clear old overlays
        removeViews(1, childCount - 1)

        // Add transparent TextViews for each word
        for (word in words) {
            val tv = TextView(context).apply {
                text = word.text
                setTextColor(Color.TRANSPARENT) // invisible text
                setBackgroundColor("#33FFFF00".toColorInt()) // highlight for debug
                setOnClickListener {
                    // Copy to clipboard
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("PDF Text", word.text))
                    Toast.makeText(context, "Copied: ${word.text}", Toast.LENGTH_SHORT).show()
                }
            }

            val params = LayoutParams(
                word.rect.width().toInt(),
                word.rect.height().toInt()
            )
            params.leftMargin = word.rect.left.toInt()
            params.topMargin = word.rect.top.toInt()

            addView(tv, params)
        }
    }
}
