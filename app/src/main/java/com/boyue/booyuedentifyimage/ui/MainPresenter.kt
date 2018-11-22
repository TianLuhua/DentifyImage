package com.boyue.booyuedentifyimage.ui

import android.annotation.SuppressLint
import android.graphics.*
import android.hardware.Camera
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import com.boyue.booyuedentifyimage.DentifyImageModel
import com.boyue.booyuedentifyimage.api.imagesearch.AipImageSearch
import com.boyue.booyuedentifyimage.base.BasePresenter
import com.boyue.booyuedentifyimage.bean.ResultResponseBean
import com.boyue.booyuedentifyimage.utils.runOnIoThread
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.R.attr.bitmap



/**
 * Created by Tianluhua on 2018\11\21 0021.
 */
class MainPresenter : BasePresenter<MainContract.View>(), MainContract.Presenter {

    companion object {
        val TAG = "MainPresenter"

    }

    init {


    }


    //图片相似请求接口
    val client = AipImageSearch.getInstance()
    //图片相似请求接口参数
    val params = HashMap<String, String>()

    //封面的分类在百度云库中的默认分类
    private val COVER_MODEL = "1,1"
    //默认为封面编号
    private var classifyNumber = COVER_MODEL
    private var dentifyImageModel: DentifyImageModel = DentifyImageModel.COVER

    private var attachData = true

    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {

        }
    }

    override fun reset() {
        attachData = true
    }

    override fun onPreviewData(data: ByteArray, mCamera: Camera) {
//        Log.e(TAG, "onPreviewData")
        if (attachData) {
            runOnIoThread {
                doRequest(data, mCamera) {
                    attachData = false
                }
            }
        }

    }

    /**
     * 向百度云发送请求
     */
    inline private fun doRequest(data: ByteArray, mCamera: Camera, before: () -> Unit) {
        before()
        val previewSize = mCamera.getParameters().getPreviewSize()
        val image = YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null)
        val stream = ByteArrayOutputStream()
        image.compressToJpeg(Rect(0, 0, previewSize.width, previewSize.height), 80, stream)
        val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
        mRootView!!.setBitmap(bmp)
        params.put("tags", classifyNumber)
        params.put("tag_logic", "0")
        Log.e("classifyNumber", classifyNumber)
        var resultJson = client.sameHqSearch(stream.toByteArray(), params)
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
            val ss = brief.split(",")
            //封面brief信息格式：书本描述，分类1编号，分类2编号。举个栗子：火火兔绘本，1，4
            if (ss.size >= 2) {
                val classifyBuilder = StringBuilder()
                classifyBuilder.append(ss[ss.size - 2])
                classifyBuilder.append(",")
                classifyBuilder.append(ss[ss.size - 1])
                classifyNumber = classifyBuilder.toString()
//                        dentifyImageModel = DentifyImageModel.CONTENT
                Log.e("classifyNumber", classifyNumber)
            }
        }
    }

}