package com.zhangls.android.attendance

import android.app.Application
import com.squareup.leakcanary.LeakCanary

/**
 * 自定义 Application
 *
 * 初始化 LeakCanary
 *
 * @author zhangls
 */
class AttendanceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // This process is dedicated to LeakCanary for heap analysis.
        // You should not init your app in this process.
        if (LeakCanary.isInAnalyzerProcess(this)) return

        LeakCanary.install(this)
        // Normal app init code...
    }
}