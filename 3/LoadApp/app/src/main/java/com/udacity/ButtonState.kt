package com.udacity


sealed class ButtonState {
    object Pressed : ButtonState()
    object Loading : ButtonState()
    object Default : ButtonState()
}