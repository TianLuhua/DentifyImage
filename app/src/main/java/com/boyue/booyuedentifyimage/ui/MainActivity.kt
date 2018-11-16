package com.boyue.booyuedentifyimage.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.booyue.utils.ToastUtils
import com.boyue.booyuedentifyimage.R
import com.boyue.booyuedentifyimage.api.imagesearch.AipImageSearch
import com.boyue.booyuedentifyimage.utils.runOnIoThread
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    private var isPreview = false
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private var preview: RelativeLayout? = null
    private var img_photo: ImageView? = null
    private var imgUri: Uri? = null              //图片URI
    private var number_camera = 0          //摄像头个数
    private val which_camera = 0           //打开哪个摄像头

//    private var accessToken: String? = null//来自百度云的认证信息


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initdata()
        setContentView(R.layout.activity_main)
        bindViews()
        init()
//        getAccessToken()
        number_camera = Camera.getNumberOfCameras()
    }

//    private fun getAccessToken() {
//        runOnIoThread {
//            AuthService.getAuth {
//                Log.e("result", it)
//                accessToken = it
//            }
//        }
//    }

    private fun initanim() {
        val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.img_anim)
        img_photo!!.startAnimation(animation)
    }


    /**
     * 初始化摄像头参数
     */
    private fun initCamera() {
        //设备支持摄像头才创建实例
        if (checkCameraHardware(this@MainActivity)) {
            mCamera = getCameraInstance()//打开硬件摄像头，这里导包得时候一定要注意是android.hardware.Camera
            mCamera!!.setDisplayOrientation(180 / 2)
        } else {
            Toast.makeText(this@MainActivity, "当前设备不支持摄像头", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 判断摄像头是否存在
     *
     * @param context
     * @return
     */
    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)

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
                    arrayOf(Manifest.permission.CAMERA),
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
            preview!!.addView(mPreview, 0)
            mPreview!!.setOnClickListener {
                mCamera!!.autoFocus(object : Camera.AutoFocusCallback {
                    override fun onAutoFocus(success: Boolean, camera: Camera) {
                        if (success) {
                            println(">>>>>>>>success")
                        } else {
                            camera.autoFocus(this)//如果失败，自动聚焦
                        }
                    }
                })
            }
            try {
                Toast.makeText(this@MainActivity, preview!!.childCount, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {

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
                preview!!.removeView(mPreview)
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
            try {
//                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
//                img_photo!!.visibility = View.VISIBLE
//                img_photo!!.setImageBitmap(bitmap)
//                initanim()
//                val filePar = File(Environment.getExternalStorageDirectory().toString() + "/videoappimg")
//                //如果不存在这个文件夹就去创建
//                if (!filePar.exists()) {
//                    filePar.mkdirs()
//                }
//                val file = File(Environment.getExternalStorageDirectory(), "/videoappimg/" + "videoapp_" + System.currentTimeMillis() + ".jpg")
//                val outputStream = FileOutputStream(file)
//                imgUri = Uri.fromFile(file)
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//                outputStream.close()
//                camera.stopPreview()
//                camera.startPreview()//处理完数据之后可以预览
                runOnIoThread {
                    val client = AipImageSearch("14795579", "MdOoOFdeptRjcAyvTP5L094i", "Ad4cnllYQGS3IRgZ2dLGIW5naeLtGGmc")
                    val params = HashMap<String, String>()
                    params.put("tags", "1")
                    var result = client.sameHqSearch(data, params)
                    Log.e("result", result.toString())
                    runOnUiThread { ToastUtils.showLongToast(result.toString()) }
                }


//                runOnIoThread {
//                    try {
//                        val image = Base64Util.encode(data)
//                        val params = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(image, "UTF-8")
//                        Log.e("params", params)
//                        val result = HttpUtil.post(ImageSearchConsts.SAME_HQ_SEARCH, accessToken, params)
//                        Log.e("result", result)
//                        val gson = Gson()
//                        val resultBean = gson.fromJson(result, ResultBean::class.java)
//                        val resultResponseBeans = resultBean.getResult()
//                        if (resultResponseBeans.size > 0) {
//                            val resultResponseBean = resultResponseBeans.get(0)
//                            //                                Log.e("result Brief", resultResponseBean.toString());
//                            runOnUiThread { ToastUtils.showLongToast(resultResponseBean.getBrief()) }
//
//                        } else {
//                            runOnUiThread { ToastUtils.showLongToast("没有找到结果！！") }
//                        }
//
//                    } catch (e: UnsupportedEncodingException) {
//                        e.printStackTrace()
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
            } catch (e: Exception) {
                Log.e("Exception", "Exception : " + e.message)
            }

        }
    }

    /**
     * 未绑定页面时的数据初始化操作
     */
    private fun initdata() {
        val window = window                    //得到窗口
        requestWindowFeature(Window.FEATURE_NO_TITLE)              //请求没有标题
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, //设置全屏
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)    //设置高亮
    }

    /**
     * 绑定视图
     */
    private fun bindViews() {
        preview = findViewById(R.id.camera_preview)
        img_photo = findViewById(R.id.img_photo)
    }

    //拍照
    fun DoTakePhoto(view: View) {
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
