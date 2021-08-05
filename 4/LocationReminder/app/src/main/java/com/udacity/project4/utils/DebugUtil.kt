package com.udacity.project4.utils

import android.content.Intent
import android.os.Bundle
import android.util.Log


fun Intent.log(tag: String = "cy.log.intent"){
    Log.d(tag, this.toUri(0));
    this.extras?.log(tag)
}

fun Bundle.log(tag: String = "cy.log.bundle"){
    Log.d(tag, "{")
    keySet().forEach{ key ->
        Log.d(tag, "key=$key, content=${get(key).toString()}");
    }
    Log.d(tag, "}");
}

