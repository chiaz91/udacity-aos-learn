package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.Constants.DEFAULT_END_DATE_DAYS
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {
    private val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())

    val asteroidList: LiveData<List<Asteroid>> = Transformations.map(database.asteroid.getListAsteroid()) {
        it?.asDomainModel()
    }

    val pictureOfTheDayData: LiveData<PictureOfDay> = Transformations.map(database.asteroid.getPictureOfTheDay()) {
        it?.let {
            it.asDomainModel()
        }
    }

    suspend fun refreshAsteroid() {
        withContext(Dispatchers.IO) {
            try {
                // prepare request parameters
                val calendar = Calendar.getInstance()
                val currentTime = calendar.time
                val startDate = dateFormat.format(currentTime)
                calendar.add(Calendar.DAY_OF_YEAR, DEFAULT_END_DATE_DAYS)
                val endDate = dateFormat.format(calendar.time)

                // send request and parse data
                val asteroidList = AsteroidApi.retrofitService.getAsteroidListAsync(startDate, endDate).await()
                val asteroidParsed = parseAsteroidsJsonResult(JSONObject(asteroidList))

                // insert into into database
                database.asteroid.insertAllAsteroids(asteroidParsed.asDatabaseModel())
            } catch (e: HttpException) {
                Log.e("refreshAsteroid", e.localizedMessage)
            }
        }
    }

    suspend fun refreshPictureOfDay() {
        withContext(Dispatchers.IO) {
            try {
                // request for picture of the day
                val pictureOfTheDay = AsteroidApi.retrofitService.getPictureOfTheDayAsync().await()
                // insert into database
                database.asteroid.insertPictureOfTheDay(pictureOfTheDay.asDatabaseModel())
            } catch (e: HttpException) {
                Log.e("refreshPictureOfDay", e.localizedMessage)
            }
        }
    }


}