package com.boyue.booyuedentifyimage.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.hardware.Camera
import android.net.Uri
import android.os.*
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
import com.boyue.booyuedentifyimage.api.imagesearch.AipImageSearch
import com.boyue.booyuedentifyimage.api.camera.VcCamera
import com.boyue.booyuedentifyimage.bean.ResultResponseBean
import com.boyue.booyuedentifyimage.utils.runOnIoThread
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), MainContract.View {


    companion object {
        val TAG = "MainActivity"
    }

    private var mCamera: VcCamera? = null
    private var mPreview: CameraPreview? = null
    private var imgUri: Uri? = null              //图片URI
    //默认为封面编号
    private var classifyNumber = "1,1"
    //默认识别模式：封面模式
    private var dentifyImageModel: DentifyImageModel = DentifyImageModel.COVER
    private val animation = AnimationUtils.loadAnimation(Utils.getApp(), R.anim.img_anim)
    private val PERMISSIONS_CAMERA = Manifest.permission.CAMERA
    private val PERMISSIONS_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val REQUESTCODE = 0x00001

    //图片相似请求接口
    val client = AipImageSearch.getInstance()
    //图片相似请求接口参数
    val params = HashMap<String, String>()

    val handler = object : Handler() {
        override fun handleMessage(msg: Message?) {


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        if (hasPermission()) {
            init()
        } else {
            requestPermission()
        }
    }

    override fun showLoading() {

    }


    override fun dismissLoading() {

    }


    override fun onDestroy() {
        super.onDestroy()
        closeCamera()
    }

    /**
     * 绑定视图
     */
    private fun initView() {
        getcurrentDentifuModel()
        back_cover.setOnClickListener {
            //设置成识别封面模式
            classifyNumber = "1,1"
            dentifyImageModel = DentifyImageModel.COVER
            text_content.text = ""
            getcurrentDentifuModel()
        }
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
        } else {
            Toast.makeText(this@MainActivity, "打开摄像头失败", Toast.LENGTH_SHORT).show()
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
            if (shouldShowRequestPermissionRationale(PERMISSIONS_CAMERA) || shouldShowRequestPermissionRationale(PERMISSIONS_STORAGE)) {
                ToastUtils.showLongToast("Camera AND storage permission are required for this demo")
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(PERMISSIONS_CAMERA, PERMISSIONS_STORAGE), REQUESTCODE)
            }
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
                    init()
                } else {
                    ToastUtils.showLongToast("请到设置界面赋予APP于Camera和读取内存卡的权限！")
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    /**
     * 获取当前识别模式：封面/内容
     */
    private fun getcurrentDentifuModel() {
        dentify_model.text = when (dentifyImageModel) {
            DentifyImageModel.COVER -> {
                "封面识别模式"
            }
            DentifyImageModel.CONTENT -> {
                "内容识别模式"
            }
        }
    }

    /**
     * 初始化摄像头参数
     */
    private fun initCamera() {
        //设备支持摄像头才创建实例
        if (application.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mCamera = VcCamera(this@MainActivity)//打开硬件摄像头，这里导包得时候一定要注意是android.hardware.Camera
            mCamera!!.setVcPreviewCallback { data, angle, SPF, isFront ->
                Log.e(TAG, "VcPreviewCallback")
            }
        } else {
            ToastUtils.showToast(R.string.nonsupport_camera)
        }
    }

    /**
     * 关闭相机
     */
    private fun closeCamera() {
        if (mCamera != null) {
            mCamera!!.closeCamera()
            mCamera = null
            if (mPreview != null) {
                mPreview!!.holder.removeCallback(mPreview!!.getmCallback())
                camera_preview!!.removeView(mPreview)
            }
        }
    }

    /**
     * 拍照数据回调
     */
    private inner class TakePictureCallback : Camera.PictureCallback {
        override fun onPictureTaken(data: ByteArray, camera: Camera) {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            img_photo.visibility = View.VISIBLE
            img_photo.setImageBitmap(bitmap)
            img_photo.startAnimation(animation)
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
                    val ss = brief.split(",")
                    //封面brief信息格式：书本描述，分类1编号，分类2编号。举个栗子：火火兔绘本，1，4
                    if (ss.size >= 2) {
                        val classifyBuilder = StringBuilder()
                        classifyBuilder.append(ss[ss.size - 2])
                        classifyBuilder.append(",")
                        classifyBuilder.append(ss[ss.size - 1])
                        classifyNumber = classifyBuilder.toString()
                        dentifyImageModel = DentifyImageModel.CONTENT
                        Log.e("classifyNumber", classifyNumber)
                    }
                }
                runOnUiThread {
                    getcurrentDentifuModel()
                    val text = brief ?: getString(R.string.i_do_not_know_this_book)
                    ToastUtils.showLongToast(text)
                    text_content.text = text
                }

            }
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

        fun getmCallback(): SurfaceHolder.Callback {
            return this.mCallback
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
            if (mHolder.surface == null) {
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
                mCamera!!.openCamera(mHolder)
            } catch (e: Exception) {
                Log.d(TAG, "Error starting camera preview: " + e.message)
            }
        }

        override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
            if (mCamera != null) {
                mCamera.closeCamera()   //停止预览
            }
        }
    }
}
