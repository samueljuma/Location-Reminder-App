package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import net.bytebuddy.implementation.bytecode.Throw

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersList: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {


    private var shouldReturnError = false


    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error(
                "Error getting reminders"
            )
        }
        remindersList?.let {
            return Result.Success(it)
        }
        return Result.Success(emptyList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if(shouldReturnError){
            Result.Error("FakeDataSource Error")
        }

        val reminder = remindersList?.find { reminderDTO ->
            reminderDTO.id == id
        }

        return if(reminder !=null){
            Result.Success(reminder)
        }else{
            Result.Error("Reminder not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()
    }

}