package com.adentech.recovery.fbevent

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class FBEventManager {

    fun logEvent(eventName: String) {
        val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
        val bundle = Bundle()

        firebaseAnalytics.logEvent(eventName, bundle)
    }

    fun logEvent(eventName: String, vararg attributes: String) {
        val firebaseAnalytics = Firebase.analytics
        val bundle = Bundle()
        val len = attributes.size
        var i = 0
        while (i < len) {
            bundle.putString(attributes[i], attributes[i + 1])
            i += 2
        }

        Log.d("salimmm", "bundle ${bundle}")
        firebaseAnalytics.logEvent(eventName, bundle)
    }

}