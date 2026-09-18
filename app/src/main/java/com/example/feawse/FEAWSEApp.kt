package com.example.feawse

import android.app.Application
import com.example.feawse.util.ResourceHelper

class FEAWSEApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ResourceHelper.init(this)
    }
}
