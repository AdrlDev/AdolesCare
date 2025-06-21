package dev.adriele.adolescare.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.adriele.adolescare.R
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.contracts.IModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PdfModulesAdapter(
    private val learningModules: List<LearningModule>,
    private val iModules: IModules.PDF
) : RecyclerView.Adapter<PdfModulesAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tv_category)
        val pdfCover: ImageView = view.findViewById(R.id.pdf_cover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pdf_modules_layout, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val pdf = learningModules[position]

        val context = holder.itemView.context

        val formattedCategory = pdf.category
            .split(' ')
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercaseChar() }
            }

        holder.tvCategory.text = formattedCategory

        // Set the adapter only if it's not already set or if data changed
        val safeTitle = pdf.category
            .lowercase()
            .replace(" ", "_")

        val assetPath = "modules/pdf/cover/$safeTitle.png"

        try {
            val cachedFile = try {
                Utility.copyAssetToCache(context, assetPath)
            } catch (e: Exception) {
                Log.e("PDFModulesAdapter", "Error copying asset: $e")
                null
            }

            if (cachedFile != null && cachedFile.exists()) {
                Glide.with(context)
                    .load(cachedFile)
                    .placeholder(R.drawable.pdf_icon)
                    .error(R.drawable.pdf_icon)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            holder.pdfCover.scaleType = ImageView.ScaleType.CENTER_INSIDE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable?>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            // Load succeeded: set scaleType to FIT_XY
                            holder.pdfCover.scaleType = ImageView.ScaleType.FIT_XY
                            return false // allow Glide to set the image
                        }
                    })
                    .into(holder.pdfCover)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val file = Utility.copyAssetToCache(context, pdf.contentUrl)
                        val thumbnail = Utility.generatePdfThumbnail(file!!, 0)

                        withContext(Dispatchers.Main) {
                            Glide.with(context)
                                .asBitmap()
                                .load(thumbnail)
                                .placeholder(R.drawable.pdf_icon)
                                .error(R.drawable.pdf_icon)
                                .listener(object : RequestListener<Bitmap> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Bitmap?>,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        holder.pdfCover.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        model: Any,
                                        target: Target<Bitmap?>?,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        holder.pdfCover.scaleType = ImageView.ScaleType.FIT_XY
                                        return false
                                    }
                                })
                                .into(holder.pdfCover)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            holder.pdfCover.setImageResource(R.drawable.pdf_icon)
                            holder.pdfCover.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            holder.pdfCover.setImageResource(R.drawable.pdf_icon)
        }

        holder.itemView.setOnClickListener {
            iModules.onPdfClick(pdf)
        }
    }

    override fun getItemCount(): Int = learningModules.size
}
