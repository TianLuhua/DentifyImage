package com.boyue.booyuedentifyimage.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.booyue.utils.ToastUtils
import com.boyue.booyuedentifyimage.R
import com.boyue.booyuedentifyimage.api.imagesearch.AipImageSearch
import com.boyue.booyuedentifyimage.bean.ResultResponseBean
import com.boyue.booyuedentifyimage.utils.runOnIoThread
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    private var isPreview = false
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var imgUri: Uri? = null              //图片URI
    private val which_camera = 0           //打开哪个摄像头
    //默认为封面编号
    private var classifyNumber = "1,1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        init()
    }

    private fun initanim() {
        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.img_anim)
        img_photo!!.startAnimation(animation)
    }


    /**
     * 初始化摄像头参数
     */
    private fun initCamera() {
        //设备支持摄像头才创建实例
        if (application.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mCamera = getCameraInstance()//打开硬件摄像头，这里导包得时候一定要注意是android.hardware.Camera
            mCamera?.setDisplayOrientation(90)
        } else {
            ToastUtils.showToast(R.string.nonsupport_camera)
        }
    }

    /**
     * 获取Camera实例
     *
     * @return Camera实例
     */
    fun getCameraInstance(): Camera? {
        var c: Camera? = null
        //android 6.0以后必须动态调用权限
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity as Activity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    3)
        } else {
            try {
                c = Camera.open(which_camera) // 试图获取Camera实例
            } catch (e: Exception) {
                Log.e("sda", e.toString())
                // 摄像头不可用（正被占用或不存在）
            }
        }
        return c // 不可用则返回null
    }

    /**
     * 初始化数据
     */
    private fun init() {
        // 创建Camera实例
        initCamera()
        if (mCamera != null) {
            // 创建Preview view并将其设为activity中的内容
            mPreview = CameraPreview(this, mCamera)
            camera_preview!!.addView(mPreview, 0)
            mPreview!!.setOnClickListener {
                mCamera!!.autoFocus(object : Camera.AutoFocusCallback {
                    override fun onAutoFocus(success: Boolean, camera: Camera) {
                        if (success) {
                            doTakePhoto()
                        } else {
                            camera.autoFocus(this)//如果失败，自动聚焦
                        }
                    }
                })
            }
        } else {
            Toast.makeText(this@MainActivity, "打开摄像头失败", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        if (mCamera == null) {
            init()
        }
        super.onResume()
    }

    override fun onPause() {
        closeCamera()
        super.onPause()
    }

    private fun closeCamera() {
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
            if (mPreview != null) {
                mPreview!!.holder.removeCallback(mPreview!!.getmCallback())
                camera_preview!!.removeView(mPreview)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 3) {
            init()                 //获取权限后在去验证一次
        } else if (requestCode == 4) {
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private inner class TakePictureCallback : Camera.PictureCallback {
        override fun onPictureTaken(data: ByteArray, camera: Camera) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            img_photo!!.visibility = View.VISIBLE
            img_photo!!.setImageBitmap(bitmap)
            initanim()
            val filePar = File(Environment.getExternalStorageDirectory().toString() + "/videoappimg")
            //如果不存在这个文件夹就去创建
            if (!filePar.exists()) {
                filePar.mkdirs()
            }
            val file = File(Environment.getExternalStorageDirectory(), "/videoappimg/" + "videoapp_" + System.currentTimeMillis() + ".jpg")
            val outputStream = FileOutputStream(file)
            imgUri = Uri.fromFile(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            camera.stopPreview()
            camera.startPreview()//处理完数据之后可以预览
            runOnIoThread {
                val client = AipImageSearch.getInstance()
                val params = HashMap<String, String>()
                params.put("tags", classifyNumber)
                params.put("tag_logic", "0")
                Log.e("classifyNumber", classifyNumber)
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
                    //封面brief信息格式：书本描述，分类1编号，分类2编号。举个栗子：火火兔绘本，1，4
                    val ss = brief.split(",")
                    if (ss.size >= 2) {
                        val classifyBuilder = StringBuilder()
                        classifyBuilder.append(ss[ss.size - 2])
                        classifyBuilder.append(",")
                        classifyBuilder.append(ss[ss.size - 1])
                        classifyNumber = classifyBuilder.toString()
                        Log.e("classifyNumber", classifyNumber)
                    }
                }
                runOnUiThread {
                    ToastUtils.showLongToast(brief
                            ?: getString(R.string.i_do_not_know_this_book))
                }

            }
        }
    }

    /**
     * 绑定视图
     */
    private fun initView() {
        img_doTakePhoto.setOnClickListener {
            doTakePhoto()
        }
        back_cover.setOnClickListener {
            //设置成识别封面模式
            classifyNumber="1,1"
        }
    }

    //拍照
    fun doTakePhoto() {
        if (mCamera != null) {
            mCamera!!.takePicture(null, null, TakePictureCallback())
        } else {
            ToastUtils.showToast("没有打开相机")
        }
    }


    //打开图片
    fun DoOpenImg(view: View) {
        AlertDialog.Builder(this@MainActivity, AlertDialog.THEME_HOLO_LIGHT).setTitle("提示")
                .setMessage("是否查看图片")
                .setNegativeButton("取消") { dialogInterface, i -> }
                .setPositiveButton("查看") { dialogInterface, i ->
                    val intent = Intent(Intent.ACTION_VIEW)    //打开图片得启动ACTION_VIEW意图
                    intent.setDataAndType(imgUri, "image/*")    //设置intent数据和图片格式
                    startActivity(intent)
                }.create().show()
    }


    internal inner class CameraPreview(context: Context, private val mCamera: Camera?) : SurfaceView(context), SurfaceHolder.Callback {

        private val mHolder: SurfaceHolder
        private val TAG = "CameraPreview"
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

        fun getmCallback(): SurfaceHolder.Callback {
            return this.mCallback
        }

        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            try {
                mCamera!!.setPreviewDisplay(mHolder)
                mCamera.startPreview()
                isPreview = true       //开始预览
            } catch (e: IOException) {
                Log.d(TAG, "Error setting camera preview: " + e.message)
            }

        }

        override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
            // 如果预览无法更改或旋转，注意此处的事件
            // 确保在缩放或重排时停止预览
            if (mHolder.surface == null) {
                // 预览surface不存在
                return
            }
            // 更改时停止预览
            try {
                mCamera!!.stopPreview()
            } catch (e: Exception) {
                // 忽略：试图停止不存在的预览
                Log.d(TAG, "Error stopPreview : " + e.message)
            }
            // 在此进行缩放、旋转和重新组织格式
            // 以新的设置启动预览
            try {
                mCamera!!.setPreviewDisplay(mHolder)
                mCamera.startPreview()
            } catch (e: Exception) {
                Log.d(TAG, "Error starting camera preview: " + e.message)
            }

        }

        override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
            if (mCamera != null) {
                if (isPreview) {            //如果正在预览
                    mCamera.stopPreview()   //停止预览
                    mCamera.release()       //释放资源
                    isPreview = false
                }
            }
        }
    }
}
