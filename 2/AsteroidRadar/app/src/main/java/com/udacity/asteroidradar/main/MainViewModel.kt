package com.udacity.asteroidradar.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.repository.AppRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = AppRepository()

    init {
        viewModelScope.launch {
            // test network connection
            repository.refreshPictureOfDate()
            repository.refreshAsteroids()
        }
    }
}