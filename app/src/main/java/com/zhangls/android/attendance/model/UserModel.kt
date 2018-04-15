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
        var userId: String,
        var userName: String,
        var bleMac: String,
        var groupId: String,
        var groupName: String
)