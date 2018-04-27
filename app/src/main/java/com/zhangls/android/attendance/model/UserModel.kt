package com.zhangls.android.attendance.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * 用户个人信息数据结构体
 *
 * @author zhangls
 */
@Entity(tableName = "user")
data class UserModel(
        @PrimaryKey
        var userId: Int,
        var userName: String,
        var bleMac: String,
        var groupId: Int,
        var groupName: String,
        var status: Boolean = false,
        var modifyTime: Long = 0
)