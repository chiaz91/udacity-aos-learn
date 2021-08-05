package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeReminderDataSource(val reminderList: MutableList<ReminderDTO> = mutableListOf()): ReminderDataSource {
    var shouldReturnError = false


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Error occurred in getReminders")
        }
        else {
            return Result.Success(reminderList)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Error occurred in getReminder")
        }
        else {
            // Get reminder from list
            val reminder = reminderList.find {
                it.id == id
            }

            return if (reminder!=null)
                Result.Success(reminder)
            else
                Result.Error("Reminder not found")
        }
    }

    override suspend fun deleteAllReminders() {
        reminderList.clear()
    }


}