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

        fun setBitmap(bitmap: Bitmap?)

        fun currentDentifuModel(dentifyImageModel: DentifyImageModel)

        fun updateUI(msg: String)

    }

    interface Presenter : IPresenter<View> {

        /**
         * 初始化一些成员、变量
         */
        fun initPresenter()

        /**
         * 摄像头数据实时回调
         */
        fun onPreviewData(data: ByteArray, mCamera: Camera)

        /**
         * 回到封面识别模式
         */
        fun reset()
    }

}