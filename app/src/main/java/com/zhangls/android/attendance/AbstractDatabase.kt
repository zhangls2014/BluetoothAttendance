package com.zhangls.android.attendance

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.zhangls.android.attendance.dao.GroupDao
import com.zhangls.android.attendance.dao.UserDao
import com.zhangls.android.attendance.model.GroupModel
import com.zhangls.android.attendance.model.UserModel


/**
 * 数据库
 *
 * @author zhangls
 */
@Database(entities = [GroupModel::class, UserModel::class], version = 1)
abstract class AbstractDatabase : RoomDatabase() {

    /**
     * 获取分组信息
     *
     * @return GroupDao 可操作对象
     */
    abstract fun groupDao(): GroupDao

    /**
     * 获取用户信息
     *
     * @return UserDao 可操作对象
     */
    abstract fun userDao(): UserDao


    companion object {

        private var mAppDatabase: AbstractDatabase? = null

        @Synchronized
        fun get(context: Context): AbstractDatabase {
            if (mAppDatabase == null) {
                mAppDatabase = Room
                        .databaseBuilder(context.applicationContext,
                                AbstractDatabase::class.java,
                                "Bluetooth.db")
                        .build()
            }
            return mAppDatabase!!
        }
    }
}
