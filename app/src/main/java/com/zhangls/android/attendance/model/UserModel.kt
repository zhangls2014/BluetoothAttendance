package com.zhangls.android.attendance.model

/**
 * 用户个人信息数据结构体
 *
 * @author zhangls
 */
data class UserModel(
        val userId: String,
        val userName: String,
        val bleMac: String,
        val groupId: String,
        val groupName: String
)