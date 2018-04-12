package com.zhangls.android.attendance.util

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View

/**
 * 扩展工具类
 *
 * @author zhangls
 */
class ExtensionUtil {

    /**
     * 显示 snackbar 消息
     */
    fun Context.snack(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration).show()
    }
}