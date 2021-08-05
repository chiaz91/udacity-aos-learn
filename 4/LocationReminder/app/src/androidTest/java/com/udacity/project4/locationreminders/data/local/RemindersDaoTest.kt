package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase
    private val reminder1 = ReminderDTO("Reminder1", "Desc1", "Googleplex", 37.42206, -122.08409, "r01")
    private val reminder2 = ReminderDTO("Reminder2", "Desc2", "NTU", 1.347362, 103.680785, "r02")


    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()


    @Test
    fun saveReminder() = runBlockingTest{
        // ensure db has no data first
        var reminderList = database.reminderDao().getReminders()
        assertThat(reminderList.isNullOrEmpty(), `is`(true))

        // save new reminder
        database.reminderDao().saveReminder(reminder1)

        // ensure reminder saved
        reminderList = database.reminderDao().getReminders()
        val loaded = reminderList[0]
        assertThat(reminderList.size, `is`(1))
        assertThat(loaded.id, `is`(reminder1.id))
        assertThat(loaded.title, `is`(reminder1.title))
        assertThat(loaded.description, `is`(reminder1.description))
        assertThat(loaded.location, `is`(reminder1.location))
        assertThat(loaded.latitude, `is`(reminder1.latitude))
        assertThat(loaded.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun getReminders() = runBlockingTest{
        // save 2 reminders, and retrieve back 2 reminders back
        database.reminderDao().run{
            saveReminder(reminder1)
            saveReminder(reminder2)
        }

        val loaded = database.reminderDao().getReminders()

        assertThat(loaded.size, `is`(2))
        assertThat(loaded.contains(reminder1), `is`(true))
        assertThat(loaded.contains(reminder2), `is`(true))
    }

    @Test
    fun getReminderById() = runBlockingTest{
        // save 2 reminders, and retrieve 1 reminder with it's id
        val prepare = mutableListOf(reminder1, reminder2)
        prepare.sortBy { it.id }
        prepare.forEach{ database.reminderDao().saveReminder(it) }

        val loaded = database.reminderDao().getReminderById(reminder2.id)

        assertThat(loaded == reminder2, `is`(true))
    }


    @Test
    fun deleteAllReminder() = runBlockingTest{
        // save 2 reminders, and test for delete all
        database.reminderDao().run{
            saveReminder(reminder1)
            saveReminder(reminder2)
        }

        // perform deletion
        database.reminderDao().deleteAllReminders()

        val loaded = database.reminderDao().getReminders()
        assertThat(loaded.isNullOrEmpty(), `is`(true))
    }


}