package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("SELECT * from table_asteroid ORDER by closeApproachDate ASC")
    fun getListAsteroid(): LiveData<List<DatabaseAsteroid>>

    @Query("SELECT * from table_picture_of_the_day WHERE id=1")
    fun getPictureOfTheDay(): LiveData<DatabasePicture>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllAsteroids(asteroids: List<DatabaseAsteroid>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPictureOfTheDay(picture: DatabasePicture)

    // TODO: allow delete asteroids before today
}

@Database(entities = [DatabaseAsteroid::class, DatabasePicture::class], version = 2)
abstract class AsteroidDatabase: RoomDatabase() {
    abstract val asteroid: AsteroidDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    // ensure accessing of database object is thread-safe
    synchronized(AsteroidDatabase::class.java){
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                AsteroidDatabase::class.java, "caches")
            .fallbackToDestructiveMigration()
            .build()
        }
    }

    return INSTANCE
}

