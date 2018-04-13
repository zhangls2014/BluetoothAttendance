package com.zhangls.android.attendance.ui

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle2.LifecycleProvider
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.util.SharedPreferencesKey
import com.zhangls.android.attendance.util.snack
import com.zhangls.android.attendance.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_login.*

class MainActivity : AppCompatActivity() {

    private val provider: LifecycleProvider<Lifecycle.Event> = AndroidLifecycle.createLifecycleProvider(this)
    private lateinit var mainViewModel: MainViewModel

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

        // 设置标题
        val preferences = getSharedPreferences(
                SharedPreferencesKey.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE)
        val username = preferences.getString(SharedPreferencesKey.USERNAME, "")
        if (username.isNotEmpty()) {
            title = String(Base64.decode(username, Base64.DEFAULT))
        }

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mainViewModel.setupProvider(provider)

        // 消息显示监听
        mainViewModel.getToastString().observe(this, Observer {
            snack(appBtnLogin, it!!)
        })

        // 获取分组信息

    }
}