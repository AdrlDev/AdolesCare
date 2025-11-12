package dev.adriele.adolescare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.database.entities.ArchiveReminder
import dev.adriele.adolescare.databinding.HistoryItemBinding

class HistoryAdapter(
    private val reminders: List<ArchiveReminder>
) : RecyclerView.Adapter<HistoryAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(private val binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: ArchiveReminder) {
            binding.tvTitle.text = reminder.title
            binding.tvMessage.text = reminder.message
            binding.tvCreatedDate.text = reminder.dateTime
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HistoryItemBinding.inflate(inflater, parent, false)
        return ReminderViewHolder(binding)
    }

    override fun getItemCount(): Int = reminders.size

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position])
    }
}