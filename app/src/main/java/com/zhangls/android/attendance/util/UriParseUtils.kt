package com.zhangls.android.attendance.util

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.zhangls.android.attendance.R


/**
 * Uri 解析工具类
 *
 * @author zhangls
 */
object UriParseUtils {

    /**
     * 将 scheme 为 file 的 uri 转成 FileProvider 提供的 content uri
     *
     * @param context 上下文对象
     * @param uri     原始 Uri
     */
    fun convertToFileProvider(context: Context, uri: Uri?): Uri? {
        if (uri == null) {
            return null
        }
        return if (ContentResolver.SCHEME_FILE == uri.scheme) {
            getUriForFile(context, File(uri.path))
        } else uri
    }

    /**
     * 获取一个临时的 Uri, 文件名随机生成
     *
     * @param context 上下文对象
     */
    fun getTempImageUri(context: Context): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            val file = File.createTempFile(imageFileName, ".jpg", storageDir)
            return getUriForFile(context, file)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

    }

    /**
     * 创建一个用于拍照图片输出路径的Uri (FileProvider)
     *
     * @param context 上下文对象
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
                context,
                context.getString(R.string.provider_name),
                file)
    }

    /**
     * 通过 URI 获取文件
     */
    fun getFileWithUri(uri: Uri, activity: Activity): File {
        return File(getPathWithUri(activity, uri))
    }

    /**
     * 通过 URI 获取文件路径
     *
     * @param activity 上下文对象
     * @param uri      Uri
     */
    fun getPathWithUri(activity: Activity, uri: Uri): String {
        var picturePath = ""
        val scheme = uri.scheme
        if (ContentResolver.SCHEME_CONTENT == scheme) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            // 从系统表中查询指定 Uri 对应的资源
            val cursor = activity.contentResolver.query(uri,
                    filePathColumn, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                if (columnIndex >= 0) {
                    picturePath = cursor.getString(columnIndex)
                }
                cursor.close()
            }
            if (TextUtils.equals(uri.authority, activity.getString(R.string.provider_name))) {
                picturePath = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).path + "/" + uri.lastPathSegment
            }
        } else if (ContentResolver.SCHEME_FILE == scheme) {
            picturePath = uri.path
        } else {
            throw IllegalStateException()
        }
        println("======image path======$picturePath")
        return picturePath
    }
}
