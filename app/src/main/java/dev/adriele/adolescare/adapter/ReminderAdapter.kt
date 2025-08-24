package dev.adriele.adolescare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.databinding.NotificationItemBinding
import dev.adriele.adolescare.helpers.contracts.IReminder
import dev.adriele.adolescare.helpers.enums.ClickFunction

class ReminderAdapter(
    private val reminders: List<Reminder>,
    private val isArchive: Boolean,
    private val iReminder: IReminder?
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(private val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.tvTitle.text = reminder.title
            binding.tvMessage.text = reminder.message

            binding.btnArchive.visibility = if(isArchive) View.GONE else View.VISIBLE
            binding.btnDelete.visibility = if(isArchive) View.GONE else View.VISIBLE

            binding.btnArchive.setOnClickListener {
                iReminder?.onClickReminder(reminder, ClickFunction.ARCHIVE)
            }

            binding.btnDelete.setOnClickListener {
                iReminder?.onClickReminder(reminder, ClickFunction.DELETE)
            }
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