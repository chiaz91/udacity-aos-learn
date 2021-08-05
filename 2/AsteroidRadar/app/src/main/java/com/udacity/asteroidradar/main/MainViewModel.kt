package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)
    private val asteroidFilter = MutableLiveData<AsteroidFilter>(AsteroidFilter.SHOW_ALL)

    fun updateFilter(filter: AsteroidFilter) {
        asteroidFilter.value = filter
    }

    val asteroidList = Transformations.switchMap(asteroidFilter){
        when (it) {
            AsteroidFilter.SHOW_WEEK -> asteroidRepository.asteroidForWeek
            AsteroidFilter.SHOW_TODAY -> asteroidRepository.asteroidForToday
            else -> asteroidRepository.asteroidForAll
        }
    }
    val pictureOfDay = asteroidRepository.pictureOfTheDayData

    init {
        viewModelScope.launch {
            // retrieve data from network
            asteroidRepository.refreshAsteroid()
            asteroidRepository.refreshPictureOfDay()
        }
    }
}