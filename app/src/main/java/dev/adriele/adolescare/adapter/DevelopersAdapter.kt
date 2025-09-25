package dev.adriele.adolescare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.adriele.adolescare.R
import dev.adriele.adolescare.databinding.ItemDeveloperBinding
import dev.adriele.adolescare.model.Developers

class DevelopersAdapter(
    private val developers: List<Developers>
) : RecyclerView.Adapter<DevelopersAdapter.DeveloperViewHolder>() {
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
        val developer = developers[position]
        with(holder.binding) {
            tvName.text = developer.name
            tvPosition.text = developer.position
            Glide.with(holder.itemView.context)
                .load(developer.resId)
                .placeholder(R.drawable.shimmer_placeholder)
                .error(R.drawable.shimmer_placeholder)
                .into(img)
        }
    }

    override fun getItemCount(): Int = developers.size
}