package com.boyue.booyuedentifyimage.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import com.boyue.booyuedentifyimage.R
import com.boyue.booyuedentifyimage.utils.LogUtils
import java.io.IOException

/**
 * Created by Tianluhua on 2018\11\21 0021.
 */
class VideoService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    companion object {
        val TAG = "VideoService"
        val STARTACTION = "com.booyue.service.videoservice"
        val AUDIO_KEY = "audio_key"
        val COMMON = 0//普通
        val COVER = 99//封面
        val BRIEF = 98//简介
        val LIST = 97//目录
        val CONTENT_1 = 1 //内容
        val CONTENT_3 = 3 //内容
        val CONTENT_5 = 5 //内容
        val CONTENT_7 = 7 //内容
        val CONTENT_9 = 9 //内容
        val CONTENT_11 = 11 //内容
        val CONTENT_13 = 13 //内容
        val CONTENT_15 = 15 //内容
        val CONTENT_17 = 17 //内容
        val CONTENT_19 = 19 //内容
        val CONTENT_21 = 21 //内容
        val CONTENT_23 = 23 //内容
        val CONTENT_25 = 25 //内容
        val CONTENT_27 = 27 //内容
        val CONTENT_29 = 29 //内容
        val CONTENT_31 = 31 //内容
        val CONTENT_33 = 33 //内容
        val CONTENT_35 = 35 //内容
        val CONTENT_37 = 37 //内容
        val CONTENT_39 = 39 //内容
        val CONTENT_41 = 41 //内容
        val CONTENT_43 = 43 //内容
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    //播放音乐
    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return Service.START_STICKY
        }
        when (intent.action) {
            STARTACTION -> {
                if (mediaPlayer == null) {
                    LogUtils.e(TAG, "mediaPlayer == null")
                    mediaPlayer = MediaPlayer()
                    mediaPlayer?.setOnPreparedListener(this)
                    mediaPlayer?.setOnCompletionListener(this)
                }
                when (intent.getIntExtra(AUDIO_KEY, -1)) {
                    //如果是封面模式和内容模式的话，如果正在播绘本内容，停止当前播放的内容，播放下一个内容。
                    COVER -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    BRIEF -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_brief))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    LIST -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_list))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_1 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_1))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_3 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_3))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_5 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_5))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_7 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_7))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_9 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_9))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_11 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_11))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_13 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_13))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_15 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_15))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_17 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_17))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_19 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_19))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_21 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_21))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_23 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_23))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_25 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_25))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_27 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_27))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_29 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_29))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_31 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_31))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_33 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_33))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_35 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_35))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_37 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_37))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_39 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_39))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_41 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_41))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT_43 -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.hw3_43))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                    //如果是普通模式（就是没有查找到任何信息的状态下）的话，就让机器把当前提示音播放完。
                    COMMON -> {
                        if (!mediaPlayer!!.isPlaying) {
                            try {
                                stopPlayer()
                                mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.common))
                                mediaPlayer!!.prepare()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        } else {
                            //正在播，不在任何处理
                        }
                    }
                }
            }
        }
        return Service.START_STICKY
    }

    /**
     * 停止当前的播放
     */
    private fun stopPlayer() {
        if (mediaPlayer == null) return
        if (mediaPlayer!!.isPlaying()) {
            LogUtils.e(TAG, "isPlaying")
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        //stopSelf();
        LogUtils.e(TAG, "onCompletion")
        if (mediaPlayer == null) return
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    override fun onPrepared(mp: MediaPlayer) {
        LogUtils.e(TAG, "onPrepared")
        if (mp == null) return
        mediaPlayer?.start()
    }

}