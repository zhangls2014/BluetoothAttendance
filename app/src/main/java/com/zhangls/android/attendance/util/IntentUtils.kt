package com.zhangls.android.attendance.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore

/**
 * Intent 工具类，用于生成跳转到外部应用的 Intent
 *
 * @author zhangls
 */
object IntentUtils {

    /**
     * 获取选择照片的 Intent
     */
    //从所有图片中进行选择
    val pickIntentWithGallery: Intent
        get() {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            return intent
        }

    /**
     * 获取从文件中选择照片的 Intent
     *
     *
     * Intent.ACTION_GET_CONTENT 该标签返回的不一定是你希望选择的类型的文件
     */
    val pickIntentWithDocuments: Intent
        get() {
            val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            return intent
        }

    /**
     * 获取拍照的 Intent
     */
    fun getCaptureIntent(outPutUri: Uri): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        //将拍取的照片保存到指定URI
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri)
        return intent
    }

    /**
     * 应用市场 Intent
     */
    fun getMarketIntent(context: Context): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=" + context.packageName)
        return intent
    }

    /**
     * 获取 Intent，打开拨号界面，并输入号码
     *
     * @param phone 手机号
     */
    fun getPhoneIntent(phone: String): Intent {
        return Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
    }

    /**
     * 获取 Intent，拨打电话
     *
     *
     * 需要权限 CALL_PHONE
     *
     * @param phone 手机号
     */
    fun getCallIntent(phone: String): Intent {
        return Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
    }
}
