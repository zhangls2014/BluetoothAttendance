package com.zhangls.android.attendance.model

/**
 * 登录个人信息数据结构体
 *
 * @author zhangls
 */
data class LoginModel(
        val token: String,
        val userId: String,
        val userName: String,
        val phoneNum: String
)
