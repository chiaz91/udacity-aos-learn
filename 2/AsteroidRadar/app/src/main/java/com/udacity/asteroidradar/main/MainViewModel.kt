package com.udacity.asteroidradar.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val asteroidRepository = AsteroidRepository()


    init {
        viewModelScope.launch {
            // retrieve data from network
            asteroidRepository.refreshAsteroid()
            asteroidRepository.refreshPictureOfDay()
        }
    }
}