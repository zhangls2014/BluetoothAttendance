package com.zhangls.android.attendance.model

/**
 * 服务器返回数据基本数据结构体
 *
 * @author zhangls
 */
data class BaseModel<out T>(
        val status: Int,
        val msg: String,
        val data: T
)