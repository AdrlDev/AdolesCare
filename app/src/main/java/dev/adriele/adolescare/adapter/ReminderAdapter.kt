package dev.adriele.adolescare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.databinding.NotificationItemBinding

class ReminderAdapter(
    private val reminders: List<Reminder>
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(private val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.tvTitle.text = reminder.title
            binding.tvMessage.text = reminder.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NotificationItemBinding.inflate(inflater, parent, false)
        return ReminderViewHolder(binding)
    }

    override fun getItemCount(): Int = reminders.size

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position])
    }
}