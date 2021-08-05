package com.udacity.asteroidradar

import java.text.SimpleDateFormat
import java.util.*


fun Date.addDays(days: Int = 0): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    this.time = calendar.timeInMillis
    return this
}

fun Date.format(dateFormat: String = Constants.API_QUERY_DATE_FORMAT): String{
    val dateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
    return dateFormat.format(this)
}

