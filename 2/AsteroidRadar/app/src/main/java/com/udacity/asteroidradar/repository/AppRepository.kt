package com.udacity.asteroidradar.repository

import android.util.Log
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


// inject room db later
class AppRepository {
    private val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())

    suspend fun refreshPictureOfDate(){
        withContext(Dispatchers.IO){
            try{
                val pic = Network.nasaService.getPictureOfTheDayAsync().await()
                Log.i("nasa.repo.pic", pic.toString())
            } catch (e: Exception){
                Log.e("nasa.repo","refreshPictureOfDate error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            try {
                val calendar  = Calendar.getInstance()
                val startDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, Constants.DEFAULT_END_DATE_DAYS)
                val endDate = calendar.time

                Log.i("nasa.repo.params", "start ${dateFormat.format(startDate)}")
                Log.i("nasa.repo.params", "end ${dateFormat.format(endDate)}")
                val asteroidList = Network.nasaService.getAsteroidsAsync(
                    dateFormat.format(startDate),
                    dateFormat.format(endDate)
                ).await()

                val asteroidListParsed = parseAsteroidsJsonResult(JSONObject(asteroidList))
                asteroidListParsed.forEach {
                    Log.i("nasa.repo.asteroid", it.toString())
                }

            } catch (e: Exception){
                Log.e("nasa.repo","refreshAsteroids error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

}