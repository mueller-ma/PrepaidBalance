package com.github.muellerma.prepaidbalance

import android.app.Application
import com.google.android.material.color.DynamicColors

class PrepaidBalanceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}