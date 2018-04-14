package com.zhangls.android.attendance.model


/**
 * 分组信息数据结构体
 *
 * @author zhangls
 */
data class GroupModel(
        /**
         * ID
         */
        val groupId: String,
        /**
         * 名称
         */
        val groupName: String,
        /**
         * 考勤状态
         */
        val status: Boolean
)