package com.udacity.asteroidradar.repository

import android.util.Log
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.Constants.DEFAULT_END_DATE_DAYS
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository() {
    private val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())

    suspend fun refreshAsteroid() {
        withContext(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                val currentTime = calendar.time
                val startDate = dateFormat.format(currentTime)
                calendar.add(Calendar.DAY_OF_YEAR, DEFAULT_END_DATE_DAYS)
                val endDate = dateFormat.format(calendar.time)

                val asteroidList = AsteroidApi.retrofitService.getAsteroidListAsync(startDate, endDate).await()

                val asteroidParsed = parseAsteroidsJsonResult(JSONObject(asteroidList))

                // debug
                asteroidParsed.forEach{
                    Log.i("nasa.repo.asteroid", it.toString())
                }

            } catch (e: HttpException) {
                Log.e("refreshAsteroid", e.localizedMessage)
            }
        }
    }

    suspend fun refreshPictureOfDay() {
        withContext(Dispatchers.IO) {
            try {
                val pictureOfTheDay = AsteroidApi.retrofitService.getPictureOfTheDayAsync().await()

                // debug
                Log.i("nasa.repo.pic", pictureOfTheDay.toString())
            } catch (e: HttpException) {
                Log.e("refreshPictureOfDay", e.localizedMessage)
            }
        }
    }


}