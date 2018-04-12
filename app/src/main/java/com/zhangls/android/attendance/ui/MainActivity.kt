package com.zhangls.android.attendance.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.zhangls.android.attendance.R

class MainActivity : AppCompatActivity() {

    companion object {

        /**
         * Activity 入口方法
         */
        fun activityStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
