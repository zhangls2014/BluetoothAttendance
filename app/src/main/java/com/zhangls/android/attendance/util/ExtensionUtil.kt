package com.zhangls.android.attendance.util

import android.content.Context
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


/**
 * 显示 Snackbar 消息
 */
fun snack(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) = Snackbar.make(view, message, duration).show()

/**
 * 显示 Snackbar 消息
 */
fun snack(view: View, @StringRes message: Int, duration: Int = Snackbar.LENGTH_SHORT) = Snackbar.make(view, message, duration).show()

/**
 * 关闭软键盘
 *
 * @param context  上下文
 * @param editText 输入框
 */
fun closeKeyboard(context: Context, editText: EditText) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}