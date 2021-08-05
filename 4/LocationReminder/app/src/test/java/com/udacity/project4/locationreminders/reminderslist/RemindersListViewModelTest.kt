package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun loadReminder_withData()  {
        // only want to test if viewModel can load data with data source,
        // shall we check the correctness of data, compare to data source?
        // or we shall assume that is responsibility of data source to test it's correctness?
        val reminder1 = ReminderDTO("Reminder1", "Desc1", "Googleplex", 37.42206, -122.08409, "r01")
        fakeDataSource.reminderList.add(reminder1)
        viewModel.loadReminders()

        val reminderList = viewModel.remindersList.getOrAwaitValue()
        assertThat(reminderList.isNotEmpty(), `is`(true))
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminder_withNoData() {
        viewModel.loadReminders()
        val reminders = viewModel.remindersList.getOrAwaitValue()

        assertThat(reminders.isNullOrEmpty(), `is`(true))
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminder_withException() {
        fakeDataSource.shouldReturnError = true

        viewModel.loadReminders()

        assertThat(viewModel.showSnackBar.getOrAwaitValue().isNotEmpty(), `is`(true))
    }


    @Test
    fun loadReminder_loadingStates() {
        // pause to check the initial state
        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // resume to check loading state after task completed
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
}