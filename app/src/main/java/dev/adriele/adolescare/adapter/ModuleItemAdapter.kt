package dev.adriele.adolescare.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.R
import dev.adriele.adolescare.database.entities.LearningModule
import dev.adriele.adolescare.helpers.Utility
import dev.adriele.adolescare.helpers.contracts.IModules

class ModuleItemAdapter(
    private val iModule: IModules.PDF
) : RecyclerView.Adapter<ModuleItemAdapter.ModuleViewHolder>() {
    private val modules = mutableListOf<LearningModule>()

    inner class ModuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvPageNumber: TextView = view.findViewById(R.id.tv_page_number)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setModules(modules: List<LearningModule>) {
        this.modules.clear()
        this.modules.addAll(modules)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]

        holder.tvTitle.text = module.title.uppercase()

        val pageNumber = Utility.getPdfPageCount(holder.itemView.context, module.contentUrl)

        holder.tvPageNumber.text = "Pages: $pageNumber"

        holder.itemView.setOnClickListener {
            iModule.onPdfClick(module)
        }
    }

    override fun getItemCount(): Int = modules.size
}