package com.udacity.asteroidradar.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.BuildConfig
import com.udacity.asteroidradar.Constants
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface AsteroidService {
    @GET("/neo/rest/v1/feed")
    fun getAsteroidListAsync(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Deferred<String>


    @GET("/planetary/apod")
    fun getPictureOfTheDayAsync(): Deferred<NetworkPictureOfDay>
}

object AsteroidApi {
    private lateinit var retrofit:  Retrofit
    val retrofitService: AsteroidService by lazy {
        getClient().create(AsteroidService::class.java)
    }

    fun getClient(): Retrofit{
        if (!::retrofit.isInitialized) {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor { apiKeyInterceptor(it) }
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
        }
        return retrofit
    }

    private fun apiKeyInterceptor(it: Interceptor.Chain): Response {
        val originalRequest = it.request()
        val originalHttpUrl = originalRequest.url()

        val newHttpUrl = originalHttpUrl.newBuilder()
            .addQueryParameter("api_key", BuildConfig.NASA_API_KEY)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newHttpUrl)
            .build()

        return it.proceed(newRequest)
    }
}