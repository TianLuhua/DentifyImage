package com.boyue.booyuedentifyimage

import android.app.Application
import com.booyue.utils.Utils

/**
 * Created by Tianluhua on 2018\11\16 0016.
 */

class DentifyImageApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}