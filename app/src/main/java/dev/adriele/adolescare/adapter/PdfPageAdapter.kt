package dev.adriele.adolescare.adapter

import android.graphics.pdf.PdfRenderer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.R
import androidx.core.graphics.createBitmap
import com.bumptech.glide.Glide
import io.getstream.photoview.PhotoView

class PdfPageAdapter(private val pdfRenderer: PdfRenderer) : RecyclerView.Adapter<PdfPageAdapter.PageViewHolder>() {

    inner class PageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: PhotoView = view.findViewById(R.id.pdfPageImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pdf_page, parent, false)
        return PageViewHolder(view)
    }

    override fun getItemCount(): Int = pdfRenderer.pageCount

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = pdfRenderer.openPage(position)
        val bitmap = createBitmap(page.width, page.height)

        holder.image.maximumScale = 5.0f
        holder.image.minimumScale = 1.0f

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        Glide.with(holder.itemView.context)
            .load(bitmap)
            .into(holder.image)
        page.close()
    }
}
