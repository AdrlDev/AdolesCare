package dev.adriele.adolescare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.adriele.adolescare.R
import dev.adriele.adolescare.databinding.ItemDeveloperBinding
import dev.adriele.adolescare.model.Credits

class CreditsAdapter(
    private val credits: List<Credits>
) : RecyclerView.Adapter<CreditsAdapter.DeveloperViewHolder>() {
    inner class DeveloperViewHolder(val binding: ItemDeveloperBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeveloperViewHolder {
        val binding = ItemDeveloperBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DeveloperViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeveloperViewHolder, position: Int) {
        val credits = this@CreditsAdapter.credits[position]
        with(holder.binding) {
            tvName.text = credits.name
            tvPosition.text = credits.position
            Glide.with(holder.itemView.context)
                .load(credits.resId)
                .placeholder(R.drawable.shimmer_placeholder)
                .error(R.drawable.shimmer_placeholder)
                .into(img)
        }
    }

    override fun getItemCount(): Int = credits.size
}