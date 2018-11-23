package com.boyue.booyuedentifyimage.ui

import android.graphics.Bitmap
import android.hardware.Camera
import com.boyue.booyuedentifyimage.DentifyImageModel
import com.boyue.booyuedentifyimage.base.IBaseView
import com.boyue.booyuedentifyimage.base.IPresenter

/**
 * Created by Tianluhua on 2018\11\21 0021.
 */
class MainContract {
    interface View : IBaseView {

        /**
         * 更新预览图
         * @param bitmap 根据摄像头抓取的数据生成的 bitmap
         */
        fun setBitmap(bitmap: Bitmap?)

        /**
         * 当前识别模式通知到Ui界面
         * @param dentifyImageModel 当前处于的识别模式（识别模式分为：封面识别模式、内容识别模式）
         */
        fun currentDentifuModel(dentifyImageModel: DentifyImageModel)

        /**
         * 百度云返回的数据来更新UI
         * @param msg 主要的是来自百度云的 brief信息
         */
        fun updateUI(msg: String)

    }

    interface Presenter : IPresenter<View> {

        /**
         * 初始化一些成员、变量
         */
        fun initPresenter()

        /**
         * 摄像头数据实时回调
         * @param data　具体的数据（该数据为YUV格式，百度接口不适用需要在调用接口前转换）
         * @param mCamera 获取图片数据的Camera
         */
        fun onPreviewData(data: ByteArray, mCamera: Camera)

        /**
         * 回到封面识别模式
         */
        fun reset()
    }

}