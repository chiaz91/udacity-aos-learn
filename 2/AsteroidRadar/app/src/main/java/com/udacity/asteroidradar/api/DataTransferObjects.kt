package com.udacity.asteroidradar.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.DatabaseAsteroid
import com.udacity.asteroidradar.database.DatabasePicture

fun List<Asteroid>.asDatabaseModel(): List<DatabaseAsteroid> {
    return this.map {
        DatabaseAsteroid(
            id = it.id,
            codename = it.codename,
            closeApproachDate = it.closeApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous)
    }.toList()
}

@JsonClass(generateAdapter = true)
data class NetworkPictureOfDay(
    val title: String,
    @Json(name = "media_type")
    val mediaType: String,
    val url: String
)

fun NetworkPictureOfDay.asDomainModel(): PictureOfDay {
    return let {
        PictureOfDay(
            url = it.url,
            mediaType = it.mediaType,
            title = it.title
        )
    }
}

fun NetworkPictureOfDay.asDatabaseModel(): DatabasePicture {
    return let {
        DatabasePicture(
            id = 1,
            url = it.url,
            mediaType = it.mediaType,
            title = it.title
        )
    }
}