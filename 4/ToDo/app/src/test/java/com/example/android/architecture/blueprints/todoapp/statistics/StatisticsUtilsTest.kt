package com.example.android.architecture.blueprints.todoapp.statistics

import com.example.android.architecture.blueprints.todoapp.data.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsUtilsTest {
    // if there's no completed task, and one active tasks
    // then there are 100% active tasks and 0% completed tasks
    @Test
    fun getActiveAndCompletedStats_noCompleted_returnsHundredZero() {
        // Create an active tasks (the false makes this active)
        val tasks = listOf<Task>(
            Task("title", "desc", isCompleted = false)
        )
        // Call our function
        val result = getActiveAndCompletedStats(tasks)

        // Check the result
        assertEquals(100f, result.activeTasksPercent)
        assertEquals(  0f, result.completedTasksPercent)
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
}