package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {




    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }


    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminder_GetById() = runBlockingTest {
        val reminder = ReminderDTO(
            "Library Visit",
            "Borrow some books",
            "Central Library",
            6.55555,
            7.56565
        )


        database.reminderDao().saveReminder(reminder)

        val result = database.reminderDao().getReminderById(reminder.id)


        assertThat(result as ReminderDTO, notNullValue())
        assertThat(result.id, `is`(reminder.id))
        assertThat(result.title, `is`(reminder.title))
        assertThat(result.description, `is`(reminder.description))
        assertThat(result.location, `is`(reminder.location))
        assertThat(result.latitude, `is`(reminder.latitude))
        assertThat(result.longitude, `is`(reminder.longitude))

    }

    @Test
    fun getAllRemindersFromDb() = runBlockingTest {
        val reminder1 = ReminderDTO(
                "Library Visit",
        "Borrow some books",
        "Central Library",
        6.55555,
        7.56565
        )

        val reminder2 = ReminderDTO(
            "Grocery Shopping",
            "Buy fruits and vegetables",
            "Market Street",
            6.61234,
            7.43210
        )

        val reminder3 = ReminderDTO(
            "Dinner with Friends",
            "Meet at the restaurant",
            "Downtown Plaza",
            6.74123,
            7.89123
        )

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        val remindersList = database.reminderDao().getReminders()

       assertThat(remindersList, `is`(notNullValue()))
    }

    @Test
    fun insertReminders_deleteAllReminders() = runBlockingTest {
        val reminder1 = ReminderDTO(
            "Library Visit",
            "Borrow some books",
            "Central Library",
            6.55555,
            7.56565
        )

        val reminder2 = ReminderDTO(
            "Grocery Shopping",
            "Buy fruits and vegetables",
            "Market Street",
            6.61234,
            7.43210
        )

        val reminder3 = ReminderDTO(
            "Dinner with Friends",
            "Meet at the restaurant",
            "Downtown Plaza",
            6.74123,
            7.89123
        )


        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)

        database.reminderDao().deleteAllReminders()

        val remindersList = database.reminderDao().getReminders()

        assertThat(remindersList, `is`(emptyList()))
    }

}