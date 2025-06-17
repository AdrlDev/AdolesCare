package dev.adriele.adolescare.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.adriele.adolescare.R
import dev.adriele.adolescare.Utility
import dev.adriele.adolescare.VideoPlayerActivity
import dev.adriele.adolescare.database.entities.LearningModule

class VideoThumbnailAdapter(
    private val context: Context,
    private val videoModules: List<LearningModule>
) : RecyclerView.Adapter<VideoThumbnailAdapter.ThumbnailViewHolder>() {

    inner class ThumbnailViewHolder(val view: View) :
        RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_thumbnail, parent, false)

        return ThumbnailViewHolder(view)
    }

    override fun getItemCount(): Int = videoModules.size

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val videoFile = videoModules[position]

        val imageView = holder.view.findViewById<ImageView>(R.id.imgThumbnail)

        // Copy from assets to cache only once
        val cachedFile = Utility.copyAssetToCache(context, videoFile.contentUrl)

        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(cachedFile)
            .frame(1_000_000) // 1st second
            .placeholder(R.drawable.video)
            .into(imageView)

        holder.view.setOnClickListener {
            context.startActivity(Intent(context, VideoPlayerActivity::class.java)
                .putExtra("path", videoFile.contentUrl))
        }
    }
}
