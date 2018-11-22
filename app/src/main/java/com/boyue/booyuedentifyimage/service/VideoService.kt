package com.boyue.booyuedentifyimage.ui.model

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.boyue.booyuedentifyimage.base.IBaseModel

/**
 * Created by Tianluhua on 2018\11\21 0021.
 */
class VideoService : Service(), IBaseModel {


    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onModelDestroy() {

    }
}