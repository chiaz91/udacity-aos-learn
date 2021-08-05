package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()

        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    private fun getSampleReminder() = ReminderDataItem(
        "Reminder1", "Desc1", "Googleplex", 37.42206, -122.08409, "r01"
    )


    @Test
    fun validateEnteredData_validateTitleFailed(){
        // given
        val reminder = getSampleReminder()
        reminder.title = ""
        // when
        val validation = viewModel.validateEnteredData(reminder)
        // then
        assertThat(validation, `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_validateLocationFailed(){
        // given
        val reminder = getSampleReminder()
        reminder.location = ""
        // when
        val validation = viewModel.validateEnteredData(reminder)
        // then
        assertThat(validation, `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @Test
    fun validateEnteredData_validatePass(){
        // given
        val reminder = getSampleReminder()
        // when
        val validation = viewModel.validateEnteredData(reminder)
        // then
        assertThat(validation, `is`(true))
    }

    @Test
    fun saveReminder_success(){
        val reminder = getSampleReminder()
        viewModel.saveReminder(reminder)

        val msg = viewModel.app.getString(R.string.reminder_saved)
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`(msg))
//        assertThat(viewModel.navigationCommand.getOrAwaitValue(), `is`(NavigationCommand.Back))
    }


    @Test
    fun saveReminder_loadingState(){
        val reminder = getSampleReminder()
        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder(reminder)
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }


    @Test
    fun onClear_isNull() {
        viewModel.onClear()

        assertThat(viewModel.reminderTitle.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderDescription.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), nullValue())
        assertThat(viewModel.selectedPOI.getOrAwaitValue(), nullValue())
        assertThat(viewModel.latitude.getOrAwaitValue(), nullValue())
        assertThat(viewModel.longitude.getOrAwaitValue(), nullValue())
    }
}