package com.boyue.booyuedentifyimage.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.os.Handler
import android.os.Message
import android.util.Log
import com.boyue.booyuedentifyimage.DentifyImageModel
import com.boyue.booyuedentifyimage.api.imagesearch.AipImageSearch
import com.boyue.booyuedentifyimage.base.BasePresenter
import com.boyue.booyuedentifyimage.bean.ResultResponseBean
import com.boyue.booyuedentifyimage.utils.runOnIoThread
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.util.*
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.booyue.utils.ToastUtils
import com.boyue.booyuedentifyimage.service.VideoService


/**
 * Created by Tianluhua on 2018\11\21 0021.
 */
class MainPresenter() : BasePresenter<MainContract.View>(), MainContract.Presenter, TextToSpeech.OnInitListener {


    companion object {
        val TAG = "MainPresenter"
        //封面的分类在百度云库中的默认分类
        val COVER_MODEL = "1,1"
        //获取当前预览消息的what值
        private val CAMERA_MSG_POSTVIEW_FRAME = 0x040
        //获取当前预览消息的延时
        private val CAMERA_MSG_POSTVIEW_FRAME_DELAY_TIME = 8000.toLong()
    }

    constructor(mContext: Context) : this() {
        this.mContext = mContext
        this.textToSpeech = TextToSpeech(mContext, this)
    }

    private var mContext: Context? = null
    //请求接口标志位
    private var attachData = false
    //图片相似请求接口
    private val client = AipImageSearch.getInstance()
    //图片相似请求接口参数
    private val params = HashMap<String, String>()
    //默认为封面编号
    private var classifyNumber = COVER_MODEL
    //默认为封面模式
    private var dentifyImageModel: DentifyImageModel = DentifyImageModel.COVER

    private var textToSpeech: TextToSpeech? = null // TTS对象

    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.what) {
                CAMERA_MSG_POSTVIEW_FRAME -> {
                    attachData = true
                    removeMessages(CAMERA_MSG_POSTVIEW_FRAME)
                    sendEmptyMessageDelayed(CAMERA_MSG_POSTVIEW_FRAME, CAMERA_MSG_POSTVIEW_FRAME_DELAY_TIME)
                }
            }

        }
    }

    override fun initPresenter() {
        checkViewAttached()
        attachData = true
        mRootView?.currentDentifuModel(dentifyImageModel)
        handler.removeMessages(CAMERA_MSG_POSTVIEW_FRAME)
        handler.sendEmptyMessageDelayed(CAMERA_MSG_POSTVIEW_FRAME, CAMERA_MSG_POSTVIEW_FRAME_DELAY_TIME)
    }

    /**
     * Camera数据实时预览回调
     *
     */
    override fun onPreviewData(data: ByteArray, mCamera: Camera) {
        if (attachData) {
            runOnIoThread {
                //                Log.e(TAG, "onPreviewData")
                doRequest(data, mCamera) {
                    attachData = false
                }
            }
        }
    }

    //tts初始化回调
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.CHINA)
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(mContext, "数据丢失或不支持", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * 重置当前为封面识别模式
     */
    override fun reset() {
        dentifyImageModel = DentifyImageModel.COVER
        mRootView?.currentDentifuModel(dentifyImageModel)
    }


    /**
     * 向百度云发送请求,获取数据和处理逻辑
     */
    inline private fun doRequest(data: ByteArray, mCamera: Camera, before: () -> Unit) {
        before()
        val previewSize = mCamera.getParameters().getPreviewSize()
        val image = YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null)
        val stream = ByteArrayOutputStream()
        image.compressToJpeg(Rect(0, 0, previewSize.width, previewSize.height), 80, stream)
        val data = stream.toByteArray()

        val bmp = BitmapFactory.decodeByteArray(data, 0, stream.size())
        mRootView!!.setBitmap(bmp)

        params.put("tags", classifyNumber)
        params.put("tag_logic", "0")
        var resultJson = client.sameHqSearch(data, params)
        Log.e("result", resultJson.toString())
        val gson = Gson()
        var resultsRespons = gson.fromJson(resultJson.toString(), ResultResponseBean::class.java)
        val results = resultsRespons.result
        val maxResult = results?.maxBy {
            it.score
        }
        //根据封面的brief信息获取该书在图库中的分类信息
        val brief = maxResult?.brief ?: null
        if (brief != null) {
            //识别了：可能是封面，可能是内容
//            dentifyImageModel = DentifyImageModel.COVER
            val ss = brief.split(",")
            //封面brief信息格式：书本描述，分类1编号，分类2编号。举个栗子：火火兔绘本，1，4
            //第一次识别分封面
            if (ss.size > 2) {
                //内容
                val classifyBuilder = StringBuilder()
                classifyBuilder.append(ss[ss.size - 2])
                classifyBuilder.append(",")
                classifyBuilder.append(ss[ss.size - 1])
                classifyNumber = classifyBuilder.toString()
                //当前是内容识别模式
                dentifyImageModel = DentifyImageModel.CONTENT
                mRootView?.currentDentifuModel(dentifyImageModel)
                Log.e(TAG, "classifyNumber:" + classifyNumber)
                startPlayAudio(VideoService.COVER)
                mRootView!!.updateUI(brief)
                return
            } else if (ss.size == 2) {
                //内容识别
                dentifyImageModel = DentifyImageModel.CONTENT
                mRootView?.currentDentifuModel(dentifyImageModel)
                val numberOfcontent = ss[1]
                val brief = with(StringBuilder()) {
                    this.append(ss[0])
                    append("第")
                    append(ss[1])
                    append("页")
                    this.toString()
                }
                if (ss[1] == "list1") {
                    startPlayAudio(VideoService.LIST1)
                } else if (ss[1] == "list2") {
                    startPlayAudio(VideoService.LIST2)
                } else if (ss[1] == "brief") {
                    startPlayAudio(VideoService.BRIEF)
                } else if (ss[1] == "authentication") {
                    startPlayAudio(VideoService.AUTHENTICATION)
                } else {
                    //如果上传至图库的图片 brief 第二个分号后填写的不是数字的话，这里将抛异常
                    startPlayAudio(numberOfcontent.toInt())
                }
                mRootView!!.updateUI(brief)
                Log.e(TAG, "content:" + numberOfcontent)
                return
            } else {
                Log.e(TAG, "注意  注意  注意 ！！" + brief)
            }
//            //封面
//            startPlayAudio(VideoService.COVER)
//            mRootView!!.updateUI(brief)

        } else {
            //没有识别
            //如果处于内容识别模式，没有识别的情况下就不在任何处理。如果是封面模式的话，没有识别就提示用户。
            if (dentifyImageModel == DentifyImageModel.CONTENT)
                return
            startPlayAudio(VideoService.COMMON)
            mRootView?.updateUI("我不认识这本书！")?:ToastUtils.showToast("我不认识这本书！")
        }
    }

    /**
     * 播放对应绘本的音频
     */
    private fun startPlayAudio(position: Int) {
        val intent = Intent(VideoService.STARTACTION)
        intent.putExtra(VideoService.AUDIO_KEY, position)
        mContext?.startService(intent)
    }

    override fun detachView() {
        super.detachView()
        attachData = false
    }
}