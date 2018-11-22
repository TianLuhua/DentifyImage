package com.boyue.booyuedentifyimage.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.boyue.booyuedentifyimage.base.IBaseModel

/**
 * Created by Tianluhua on 2018\11\21 0021.
 */
class VideoService : Service() {


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return Service.START_STICKY
    }

}