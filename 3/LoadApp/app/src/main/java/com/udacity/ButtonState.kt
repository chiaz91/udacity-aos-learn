package com.udacity


sealed class ButtonState {
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()

    // TESTING
    fun next() = when (this) {
        Completed -> Loading
        else -> Completed
    }
}