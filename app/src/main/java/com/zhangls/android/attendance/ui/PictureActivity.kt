package com.zhangls.android.attendance.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.zhangls.android.attendance.R
import kotlinx.android.synthetic.main.activity_picture.*


class PictureActivity : AppCompatActivity() {

    companion object {

        /**
         * 图片 Path
         */
        private const val IMAGE_PATH = "image_path"

        /**
         * Activity 入口方法
         */
        fun activityStart(context: Context, imagePath: String) {
            val intent = Intent(context, PictureActivity::class.java)
            intent.putExtra(IMAGE_PATH, imagePath)

            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        image.setImageURI(Uri.parse(intent.extras.getString(IMAGE_PATH, "")))
    }
}
