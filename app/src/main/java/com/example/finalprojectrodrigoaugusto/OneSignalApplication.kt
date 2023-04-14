package com.example.finalprojectrodrigoaugusto

import android.app.Application
import com.onesignal.OneSignal

const val ONESIGNAL_APP_ID = "3c3d6e52-ff9c-478b-ae3b-71297a2bef84"

class OneSignalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }
}