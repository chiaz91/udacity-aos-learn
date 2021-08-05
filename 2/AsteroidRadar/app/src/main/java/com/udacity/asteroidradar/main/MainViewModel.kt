package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    val asteroidList = asteroidRepository.asteroidList
    val pictureOfDay = asteroidRepository.pictureOfTheDayData

    init {
        viewModelScope.launch {
            // retrieve data from network
            asteroidRepository.refreshAsteroid()
            asteroidRepository.refreshPictureOfDay()
        }
    }
}