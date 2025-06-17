package dev.adriele.adolescare.adapter

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.adriele.adolescare.PdfViewerActivity
import dev.adriele.adolescare.R
import dev.adriele.adolescare.Utility
import dev.adriele.adolescare.database.entities.LearningModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModuleItemAdapter(
    internal val modules: List<LearningModule>,
    private val scope: CoroutineScope // pass lifecycleScope from Fragment or Activity
) : RecyclerView.Adapter<ModuleItemAdapter.ModuleViewHolder>() {

    inner class ModuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgThumbnail: ImageView = view.findViewById(R.id.imgThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]

        // Optional: set a placeholder while loading
        holder.imgThumbnail.setImageResource(R.drawable.pdf_icon)

        val cachedFile = Utility.copyAssetToCache(holder.itemView.context, module.contentUrl)

        scope.launch {
            val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                try {
                    Utility.generatePdfThumbnail(cachedFile)
                } catch (e: Exception) {
                    Log.e("THUMBNAIL_PDF", e.message, e)
                    null
                }
            }

            bitmap?.let {
                Glide.with(holder.itemView.context)
                    .load(it)
                    .into(holder.imgThumbnail)
            }
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cachedFile
            )

            val intent = Intent(context, PdfViewerActivity::class.java).apply {
                putExtra("pdf_uri", uri.toString())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = modules.size
}