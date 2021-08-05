package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.Event
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasksViewModelTest{

    /* Testing live data
       add following rule to allow architecture components related background jobs in the same thread
       ensuring the test results happen "synchronously" and in a "repeatable order"
    */
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun addNewTask_setsNewTaskEvent() {
        // Given a fresh ViewModel
        val tasksViewModel = TasksViewModel(ApplicationProvider.getApplicationContext())

        // Create observer - no need for it to do anything!
        val observer = Observer<Event<Unit>>{}
        try{
            // When adding a new task
            tasksViewModel.addNewTask()

            // Then the new task event is triggered
            val value = tasksViewModel.newTaskEvent.value
            assertThat(value?.getContentIfNotHandled(), (not(nullValue())))
        } finally {
            // whatever happens, don't forgot to remove the observer
            tasksViewModel.newTaskEvent.removeObserver(observer)
        }
    }


}