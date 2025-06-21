package dev.adriele.adolescare.adapter

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.adriele.adolescare.R
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.helpers.contracts.IModules
import androidx.core.net.toUri

class VideoThumbnailAdapter(
    private val context: Context,
    private val videoModules: List<LearningModule>,
    private val iModules: IModules.VIDEO
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
        val tvTitle = holder.view.findViewById<TextView>(R.id.tv_title)
        val tvCredits = holder.view.findViewById<TextView>(R.id.tv_credits)

        // Copy from assets to cache only once
        val cachedFile = Utility.copyAssetToCache(context, videoFile.contentUrl)

        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(cachedFile)
            .frame(1_000_000) // 1st second
            .placeholder(R.drawable.video)
            .into(imageView)

        val creditUrl = videoFile.contentCreditsUrl
        if (!creditUrl.isNullOrEmpty()) {
            val creditText = "Credits: $creditUrl"

            val spannable = SpannableString(creditText).apply {
                val linkStart = creditText.indexOf(creditUrl)

                setSpan(UnderlineSpan(), linkStart, creditText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.textColorLink)),
                    linkStart,
                    creditText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            tvCredits.text = spannable
            tvCredits.visibility = View.VISIBLE
        } else {
            tvCredits.visibility = View.GONE
        }

        val formattedTitle = videoFile.title
            .split(' ')
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercaseChar() }
            } + "?"

        tvTitle.text = formattedTitle
        tvCredits.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, creditUrl?.toUri())
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open link: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        tvCredits.isClickable = true
        tvCredits.movementMethod = LinkMovementMethod.getInstance()

        holder.view.setOnClickListener {
            iModules.onVideoClick(position, videoFile.contentUrl)
        }
    }
}
