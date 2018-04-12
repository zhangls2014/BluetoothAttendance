package com.zhangls.android.attendance.util


/**
 * 设置信息(单例模式)
 *
 *
 * SharedPreference 工具类
 *
 * @author zhangls
 */
class SharedPreferencesKey {

    companion object {
        /**
         * SharedPreferences 文件名
         */
        const val SHARED_PREFERENCES_NAME = "bluetooth"
        /**
         * 用户名
         */
        const val USERNAME = "username"
        /**
         * 密码
         */
        const val PASSWORD = "password"
        /**
         * 用户 ID
         */
        const val USER_ID = "user_id"
        /**
         * token
         */
        const val TOKEN = "token"
        /**
         * 手机号码
         */
        const val PHONE_NUM = "phone_num"
    }
}
