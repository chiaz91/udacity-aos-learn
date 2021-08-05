package com.example.android.architecture.blueprints.todoapp.statistics

import com.example.android.architecture.blueprints.todoapp.data.Task
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsUtilsTest {
    /* Tips
       Naming Convention: subjectUnderTest_actionOrInput_resultState
       - subjectUnderTest: what we testing eg method name

       Test case structure: Given/When/Then (similar to Arrange,Act,Assert(AAA))

       using assertion framework like hamcrest to make it more readable like human sentence
    */

    // if there's no completed task, and one active tasks
    // then there are 100% active tasks and 0% completed tasks
    @Test
    fun getActiveAndCompletedStats_noCompleted_returnsHundredZero() {
        // GIVEN a list of task with a single, active task
        val tasks = listOf<Task>(
            Task("title", "desc", isCompleted = false)
        )
        // WHEN you call getActiveAndCompletedStats
        val result = getActiveAndCompletedStats(tasks)

        // THEN there are 100% active tasks and 0% completed tasks
        // convert to hamcrest
        assertThat(result.activeTasksPercent, `is`(100f))
        assertThat(result.completedTasksPercent, `is`(0f))
    }

    // if there's 2 completed tasks, 3 one active tasks
    // then there are 60% active tasks and 40% completed tasks
    @Test
    fun getActiveAndCompletedStats_both_returnsSixtyForty() {
        val tasks = listOf<Task>(
            Task("title", "desc", isCompleted = true),
            Task("title", "desc", isCompleted = true),
            Task("title", "desc", isCompleted = false),
            Task("title", "desc", isCompleted = false),
            Task("title", "desc", isCompleted = false)
        )
        val result = getActiveAndCompletedStats(tasks)
        assertEquals(60f, result.activeTasksPercent)
        assertEquals(40f, result.completedTasksPercent)
    }


    @Test
    fun getActiveAndCompletedStats_empty_returnsZero() {
        val tasks = emptyList<Task>()
        val result = getActiveAndCompletedStats(tasks)
        assertEquals(0f, result.activeTasksPercent )
        assertEquals(0f, result.completedTasksPercent)
    }

    @Test
    fun getActiveAndCompletedStats_error_returnsZero() {
        val tasks = null
        val result = getActiveAndCompletedStats(tasks)
        assertEquals(0f, result.activeTasksPercent )
        assertEquals(0f, result.completedTasksPercent)
    }

}