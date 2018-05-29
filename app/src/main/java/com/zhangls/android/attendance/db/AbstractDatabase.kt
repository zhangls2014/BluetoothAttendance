package com.zhangls.android.attendance.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.zhangls.android.attendance.db.dao.GroupDao
import com.zhangls.android.attendance.db.dao.UserDao
import com.zhangls.android.attendance.db.entity.GroupModel
import com.zhangls.android.attendance.db.entity.UserModel


/**
 * 数据库
 *
 * @author zhangls
 */
@Database(entities = [GroupModel::class, UserModel::class], version = 4)
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

        @Volatile
        private var mAppDatabase: AbstractDatabase? = null

        fun getInstance(context: Context): AbstractDatabase =
                mAppDatabase ?: synchronized(this) {
                    mAppDatabase ?: buildDatabase(context).also { mAppDatabase = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AbstractDatabase::class.java,
                        "Bluetooth.db")
                        .allowMainThreadQueries()
                        .build()
    }
}
