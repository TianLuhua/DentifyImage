package com.boyue.booyuedentifyimage.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.booyue.utils.ToastUtils
import com.booyue.utils.Utils
import com.boyue.booyuedentifyimage.DentifyImageModel
import com.boyue.booyuedentifyimage.R
import com.boyue.booyuedentifyimage.api.camera.VcCamera
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity(), MainContract.View {

    companion object {
        val TAG = "MainActivity"
    }

    private var mCamera: VcCamera? = null
    private var mPreview: CameraPreview? = null
    private val animation = AnimationUtils.loadAnimation(Utils.getApp(), R.anim.img_anim)
    private val PERMISSIONS_CAMERA = Manifest.permission.CAMERA
    private val PERMISSIONS_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val REQUESTCODE = 0x00001

    private val mainPresenter by lazy {
        MainPresenter(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainPresenter.attachView(this)
        initView()
        if (hasPermission()) {
            initCamera()
        } else {
            requestPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        mainPresenter.initPresenter()
        initCamera()
        startPreView()
    }


    override fun onPause() {
        super.onPause()
        stopPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter.detachView()
    }

    override fun showLoading() {
        Log.e(TAG, "showLoading()")
    }


    override fun dismissLoading() {
        Log.e(TAG, "dismissLoading()")
    }

    /**
     * 绑定视图
     */
    private fun initView() {
        back_cover.setOnClickListener {
            //设置成识别封面模式
            mainPresenter.reset()
        }
    }

    /**
     * 检查Camera和读写SD卡权限
     */
    private fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 23/*Build.VERSION.M*/) {
            return checkSelfPermission(PERMISSIONS_STORAGE) == PackageManager.PERMISSION_GRANTED
                    &&
                    checkSelfPermission(PERMISSIONS_CAMERA) == PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

    /**
     * 请求Camera和SD卡读写权限
     */
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 23/*Build.VERSION.M*/) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(PERMISSIONS_CAMERA, PERMISSIONS_STORAGE), REQUESTCODE)
        }
    }

    /**
     * 判断相机权限、读写内存卡权限
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUESTCODE) {
            val result = grantResults.filter {
                it == PackageManager.PERMISSION_GRANTED
            }.let {
                if (it.size == 2) {
                    initCamera()
                } else {
                    ToastUtils.showLongToast("请到设置界面赋予APP于Camera和读取内存卡的权限！")
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 初始化摄像头
     */
    @Synchronized
    private fun initCamera() {
        //设备支持摄像头才创建实例
        if (application.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (mCamera == null) {
                mCamera = VcCamera(this@MainActivity)//打开硬件摄像头，这里导包得时候一定要注意是android.hardware.Camera
            }
        } else {
            ToastUtils.showToast(R.string.nonsupport_camera)
        }
    }

    /**
     *初始化预览
     */
    private fun startPreView() {
        if (mCamera != null) {
            // 创建Preview view并将其设为activity中的内容
            mPreview = CameraPreview(this, mCamera)
            camera_preview!!.addView(mPreview, 0)
            mCamera!!.setVcPreviewCallback { data, c ->
                //                Log.e(TAG, "setVcPreviewCallback")
                mainPresenter.checkViewAttached()
                mainPresenter.onPreviewData(data, c)
            }
        } else {
            Toast.makeText(this@MainActivity, "打开摄像头失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 关闭相机
     */
    private fun stopPreview() {
        if (mCamera != null) {
            mCamera!!.closeCamera()
            mCamera = null
            if (mPreview != null) {
                camera_preview!!.removeView(mPreview)
            }
        }
    }


    /**
     * 当前处于什么识别模式：封面/内容
     */
    override fun currentDentifuModel(dentifyImageModel: DentifyImageModel) {
        runOnUiThread {
            dentify_model.text = when (dentifyImageModel) {
                DentifyImageModel.COVER -> "封面识别模式"
                DentifyImageModel.CONTENT -> "内容识别模式"
            }
        }
    }

    /**
     * 设置当前的预览图
     */
    override fun setBitmap(bitmap: Bitmap?) {
        runOnUiThread {
            if (bitmap != null) {
                Log.e(TAG, "setBitmap")
                img_photo.visibility = View.VISIBLE
                img_photo.setImageBitmap(bitmap)
                img_photo.startAnimation(animation)
            } else {
                Log.e(TAG, "setBitmap bitmap is null")
            }
        }

    }

    /**
     * 跟新当前的内容：封面内容 or 具体内容
     */
    override fun updateUI(msg: String) {
        runOnUiThread {
            text_content.text = msg
        }
    }

    /**
     * 摄像头预览
     */
    inner class CameraPreview(context: Context, private val mCamera: VcCamera?) : SurfaceView(context), SurfaceHolder.Callback {

        private val TAG = "CameraPreview"
        private val mHolder: SurfaceHolder
        private val mCallback: SurfaceHolder.Callback

        init {
            mHolder = holder
            mCallback = this
            mHolder.addCallback(mCallback)
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
            mHolder.setFormat(PixelFormat.TRANSPARENT)
            setZOrderOnTop(true)
            setZOrderMediaOverlay(true)
        }

        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            try {
                mCamera?.openCamera(surfaceHolder)
            } catch (e: IOException) {
                Log.d(TAG, "Error setting camera preview: " + e.message)
            }

        }

        override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
            // 如果预览无法更改或旋转，注意此处的事件
            // 确保在缩放或重排时停止预览
            if (surfaceHolder.surface == null) {
                // 预览surface不存在
                return
            }
            // 更改时停止预览
            try {
                mCamera!!.closeCamera()
            } catch (e: Exception) {
                // 忽略：试图停止不存在的预览
                Log.d(TAG, "Error stopPreview : " + e.message)
            }
            // 在此进行缩放、旋转和重新组织格式
            // 以新的设置启动预览
            try {
                mCamera!!.openCamera(surfaceHolder)
            } catch (e: Exception) {
                Log.d(TAG, "Error starting camera preview: " + e.message)
            }
        }

        override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
            //停止预览
            if (mCamera != null) {
                mCamera.closeCamera()
            }
            //移除回调
            surfaceHolder.removeCallback(this)
        }
    }
}
