package dev.adriele.adolescare.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

class RecentReadWatchAdapter(
    private val recentList: List<RecentReadAndWatch>,
    private val moduleViewModel: ModuleViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val scope: CoroutineScope,
    private val iRecentReadAndWatch: IRecentReadAndWatch
) : RecyclerView.Adapter<RecentReadWatchAdapter.RecentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val binding = ItemRecentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        holder.bind(recentList[position])
    }

    override fun getItemCount(): Int = recentList.size

    inner class RecentViewHolder(private val binding: ItemRecentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecentReadAndWatch) {
            moduleViewModel.getModuleByIdLive(item.moduleId).observe(lifecycleOwner) { module ->
                if (module != null) {
                    val type = module.contentType

                    when (type) {
                        ModuleContentType.PDF -> {
                            binding.cvVideo.visibility = View.GONE

                            val cachedFile = Utility.copyAssetToCache(binding.root.context, module.contentUrl)

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
                                    Glide.with(binding.root.context)
                                        .load(it)
                                        .into(binding.imgThumbnail)
                                }
                            }
                        }
                        ModuleContentType.VIDEO -> {
                            binding.cvPdf.visibility = View.GONE

                            val cachedFile = Utility.copyAssetToCache(binding.root.context, module.contentUrl)

                            Glide.with(binding.root.context)
                                .asBitmap()
                                .load(cachedFile)
                                .frame(1_000_000) // 1st second
                                .placeholder(R.drawable.video)
                                .into(binding.imgThumbnailVideo)
                        }
                    }

                    binding.root.setOnClickListener {
                        iRecentReadAndWatch.onRecentClick(type, module.contentUrl)
                    }
                }
            }
        }
    }
}
