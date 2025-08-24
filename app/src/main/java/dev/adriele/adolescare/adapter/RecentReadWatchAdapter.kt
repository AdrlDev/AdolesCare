package dev.adriele.adolescare.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.adriele.adolescare.R
import dev.adriele.adolescare.database.entities.RecentReadAndWatch
import dev.adriele.adolescare.databinding.ItemRecentBinding
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.contracts.IRecentReadAndWatch
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.viewmodel.ModuleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.lowercase

class RecentReadWatchAdapter(
    private val recentList: List<RecentReadAndWatch>,
    private val moduleViewModel: ModuleViewModel,
    private val iRecentReadAndWatch: IRecentReadAndWatch
) : RecyclerView.Adapter<RecentReadWatchAdapter.RecentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val binding = ItemRecentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<RecentReadAndWatch>) {
        (recentList as MutableList).clear()
        recentList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.bind(recentList[position])
    }

    override fun getItemCount(): Int = recentList.size

    inner class RecentViewHolder(private val binding: ItemRecentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecentReadAndWatch) {
            CoroutineScope(Dispatchers.Main).launch {
                val module = moduleViewModel.getModuleById(item.moduleId)

                module?.let {
                    val type = it.contentType

                    when (type) {
                        ModuleContentType.PDF -> {
                            binding.cvVideo.visibility = View.GONE

                            val safeTitle = it.category
                                .lowercase()
                                .replace(" ", "_")

                            val assetPath = "modules/pdf/cover/$safeTitle.png"
                            val cachedFile = try {
                                Utility.copyAssetToCache(binding.root.context, assetPath)
                            } catch (e: Exception) {
                                Log.e("RecentReadWatchAdapter", "Error copying asset: $e")
                                null
                            }

                            if (cachedFile != null && cachedFile.exists()) {
                                Glide.with(binding.root.context)
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
                                            binding.imgThumbnail.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                            return false
                                        }

                                        override fun onResourceReady(
                                            resource: Drawable,
                                            model: Any,
                                            target: Target<Drawable?>?,
                                            dataSource: DataSource,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            binding.imgThumbnail.scaleType = ImageView.ScaleType.FIT_XY
                                            return false
                                        }
                                    })
                                    .into(binding.imgThumbnail)
                            } else {
                                // No image asset — generate from PDF
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val file = Utility.copyAssetToCache(binding.root.context, it.contentUrl)
                                        val thumbnail = Utility.generatePdfThumbnail(file!!, 0)

                                        withContext(Dispatchers.Main) {
                                            Glide.with(binding.root.context)
                                                .asBitmap()
                                                .load(thumbnail)
                                                .frame(1_000_000)
                                                .placeholder(R.drawable.pdf_icon)
                                                .error(R.drawable.pdf_icon)
                                                .listener(object : RequestListener<Bitmap> {
                                                    override fun onLoadFailed(
                                                        e: GlideException?,
                                                        model: Any?,
                                                        target: Target<Bitmap?>,
                                                        isFirstResource: Boolean
                                                    ): Boolean {
                                                        binding.imgThumbnail.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                                        return false
                                                    }

                                                    override fun onResourceReady(
                                                        resource: Bitmap,
                                                        model: Any,
                                                        target: Target<Bitmap?>?,
                                                        dataSource: DataSource,
                                                        isFirstResource: Boolean
                                                    ): Boolean {
                                                        binding.imgThumbnail.scaleType = ImageView.ScaleType.FIT_XY
                                                        return false
                                                    }
                                                })
                                                .into(binding.imgThumbnail)
                                        }
                                    } catch (_: Exception) {
                                        withContext(Dispatchers.Main) {
                                            binding.imgThumbnail.setImageResource(R.drawable.pdf_icon)
                                            binding.imgThumbnail.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                        }
                                    }
                                }
                            }
                        }

                        ModuleContentType.VIDEO -> {
                            binding.cvPdf.visibility = View.GONE

                            val cachedFile = Utility.copyAssetToCache(binding.root.context, it.contentUrl)

                            Glide.with(binding.root.context)
                                .asBitmap()
                                .load(cachedFile)
                                .frame(1_000_000)
                                .placeholder(R.drawable.video)
                                .into(binding.imgThumbnailVideo)
                        }
                    }

                    binding.root.setOnClickListener {
                        iRecentReadAndWatch.onRecentClick(type, item, module.contentUrl)
                    }
                }
            }
        }
    }
}
