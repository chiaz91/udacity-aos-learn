package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)
    private val asteroidFilter = MutableLiveData<AsteroidFilter>(AsteroidFilter.SHOW_ALL)

    private val _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus>
        get() = _status

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
            _status.value = ApiStatus.LOADING
            try{
                asteroidRepository.refreshAsteroid()
                asteroidRepository.refreshPictureOfDay()
                _status.value = ApiStatus.DONE
                Log.i("nasa.main.vm", "loading.succ")
            }catch (e: Exception){
                _status.value = ApiStatus.ERROR
                Log.i("nasa.main.vm", "loading.err ${e.message}")
                // TODO: set up an event to notify and present error to user?
            }
        }
    }
}