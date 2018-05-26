package com.zhangls.android.attendance.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

/**
 * 用户个人信息数据结构体
 *
 * @author zhangls
 */
@Entity(tableName = "user", indices = [Index(value = ["bleMac"], unique = true)])
data class UserModel(
        @PrimaryKey(autoGenerate = true)
        var userId: Int,
        var userName: String,
        var bleMac: String,
        var groupId: Int,
        var groupName: String,
        var studentId: String,
        var parentPhone: String,
        var status: Boolean = false,
        var modifyTime: Long = 0
)