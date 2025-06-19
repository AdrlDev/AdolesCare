package dev.adriele.adolescare.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.adriele.adolescare.R
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.helpers.contracts.IModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModuleItemAdapter(
    internal val modules: List<LearningModule>,
    private val scope: CoroutineScope,
    private val categoryPosition: Int,
    private val iModule: IModules.PDF
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
            iModule.onPdfClick(position, categoryPosition, module.contentUrl)
        }
    }

    override fun getItemCount(): Int = modules.size
}