package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.DEFAULT_END_DATE_DAYS
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.addDays
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {
    val asteroidForAll: LiveData<List<Asteroid>> = Transformations.map(database.asteroid.getAll()) {
        it?.asDomainModel()
    }
    val asteroidForWeek: LiveData<List<Asteroid>> = Transformations.map(
            database.asteroid.getBetweenDates(Date().format(), Date().addDays(7).format())
        ) {
        it?.asDomainModel()
    }
    val asteroidForToday: LiveData<List<Asteroid>> = Transformations.map(
        database.asteroid.getForDate(Date().format())
    ) {
        it?.asDomainModel()
    }


    val pictureOfTheDayData: LiveData<PictureOfDay> = Transformations.map(database.picture.getPictureOfTheDay()) {
        it?.asDomainModel()
    }

    suspend fun refreshAsteroid() {
        withContext(Dispatchers.IO) {
            try {
                // prepare request parameters
                val startDate = Date().format()
                val endDate = Date().addDays(DEFAULT_END_DATE_DAYS).format()

                // send request and parse data
                val asteroidList = AsteroidApi.retrofitService.getAsteroidListAsync(startDate, endDate).await()
                val asteroidParsed = parseAsteroidsJsonResult(JSONObject(asteroidList))

                // insert into into database
                database.asteroid.insertAllAsteroids(asteroidParsed.asDatabaseModel())
            } catch (e: HttpException) {
                Log.e("refreshAsteroid", e.message())
            }
        }
    }

    suspend fun refreshPictureOfDay() {
        withContext(Dispatchers.IO) {
            try {
                // request for picture of the day
                val pictureOfTheDay = AsteroidApi.retrofitService.getPictureOfTheDayAsync().await()
                // insert into database
                database.picture.insertPictureOfTheDay(pictureOfTheDay.asDatabaseModel())
            } catch (e: HttpException) {
                Log.e("refreshPictureOfDay", e.message())
            }
        }
    }


}