package dev.adriele.adolescare.helpers.contracts

import dev.adriele.adolescare.database.entities.Reminder
import dev.adriele.adolescare.helpers.enums.ClickFunction

interface IReminder {
    fun onClickReminder(reminder: Reminder, function: ClickFunction)
}