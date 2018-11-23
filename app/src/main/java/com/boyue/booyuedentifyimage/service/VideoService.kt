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
        val COVER = 0X00001//封面
        val CONTENT = 0X00002 //内容
        val COMMON = 0X00003//普通
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
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.cover))
                            mediaPlayer!!.prepare()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    CONTENT -> {
                        try {
                            stopPlayer()
                            mediaPlayer!!.setDataSource(application, Uri.parse("android.resource://" + packageName + "/" + R.raw.content))
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