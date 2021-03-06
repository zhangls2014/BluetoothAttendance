package com.zhangls.android.attendance.db.entity

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
        @PrimaryKey(autoGenerate = true)
        var groupId: Int,
        /**
         * 名称
         */
        var groupName: String,
        /**
         * 考勤状态
         */
        var status: Boolean,
        /**
         * 考勤完成时间
         */
        var modifyTime: Long = 0,
        /**
         * 照片路径
         */
        var imagePath: String? = null,
        /**
         * 是否已经上传
         */
        var upload: Boolean = false
)