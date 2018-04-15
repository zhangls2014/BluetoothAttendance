package com.zhangls.android.attendance.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


/**
 * 分组信息数据结构体
 *
 * @author zhangls
 */
@Entity(tableName = "grouping")
data class GroupModel(
        /**
         * ID
         */
        @PrimaryKey
        var groupId: Int,
        /**
         * 名称
         */
        var groupName: String,
        /**
         * 考勤状态
         */
        var status: Boolean
)