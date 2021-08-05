package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    private val reminder1 = ReminderDTO("Reminder1", "Desc1", "Googleplex", 37.42206, -122.08409, "r01")
    private val reminder2 = ReminderDTO("Reminder2", "Desc2", "NTU", 1.347362, 103.680785, "r02")


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = database.close()


    @Test
    fun testSaveReminder() = runBlocking {
        // Save one reminder
        repository.saveReminder(reminder1)

        val result = repository.getReminders()
        assertThat(result, not(nullValue()))
        assertThat(result is Result.Success, `is`(true))

        val resultSuccess = result as Result.Success
        val reminderList = resultSuccess.data

        assertThat(reminderList.size, `is`(1))
        assertThat(reminderList.contains(reminder1), `is`(true))
    }

    @Test
    fun testGetReminderById() = runBlocking {
        // Save one reminder
        repository.saveReminder(reminder1)

        // Verify reminder data from db
        val result = repository.getReminder(reminder1.id)
        assertThat(result, not(nullValue()))
        assertThat(result is Result.Success, `is`(true))

        val resultSuccess = result as Result.Success
        val loadedReminder  = resultSuccess.data
        assertThat(loadedReminder.id, `is`(reminder1.id))
        assertThat(loadedReminder.title, `is`(reminder1.title))
        assertThat(loadedReminder.description, `is`(reminder1.description))
        assertThat(loadedReminder.location, `is`(reminder1.location))
        assertThat(loadedReminder.latitude, `is`(reminder1.latitude))
        assertThat(loadedReminder.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun testGetReminderById_invalidId() = runBlocking {
        // Save one reminder
        repository.saveReminder(reminder1)

        // Verify reminder data from db
        val result = repository.getReminder("invalid_id")
        assertThat(result, not(nullValue()))
        assertThat(result is Result.Error, `is`(true))

        val message = (result as Result.Error).message
        assertThat(message, `is`("Reminder not found!"))
    }

    @Test
    fun testGetReminders() = runBlocking {
        // Save multiple reminders
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        val result = repository.getReminders()
        assertThat(result, not(nullValue()))
        assertThat(result is Result.Success, `is`(true))

        val resultSuccess = result as Result.Success
        val reminderList = resultSuccess.data
        assertThat(reminderList.size, `is`(2))
        assertThat(reminderList.contains(reminder1), `is`(true))
        assertThat(reminderList.contains(reminder2), `is`(true))
    }

    @Test
    fun testDeleteReminders() = runBlocking {
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        // Delete all reminders
        repository.deleteAllReminders()

        // Retrieve reminders
        val result = repository.getReminders()
        assertThat(result, not(nullValue()))
        assertThat(result is Result.Success, `is`(true))

        val resultSuccess = result as Result.Success
        val reminderList = resultSuccess.data
        assertThat(reminderList.isNullOrEmpty(), `is`(true))
    }
}