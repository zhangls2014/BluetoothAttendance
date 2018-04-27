package com.zhangls.android.attendance.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.zhangls.android.attendance.model.UserModel

/**
 * 用户信息 Dao 类
 *
 * @author zhangls
 */
@Dao
interface UserDao {

    /**
     * 添加用户数据
     *
     * @param userModel 用户
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userModel: List<UserModel>)

    /**
     * 获取指定分组下的所有数据
     *
     * @return 数据列表
     */
    @Query("SELECT * FROM user")
    fun getAllUser(): List<UserModel>

    /**
     * 获取指定分组下的所有数据
     *
     * @return 数据列表
     */
    @Query("SELECT * FROM user WHERE groupId = :id")
    fun getGroupUser(id: Int): List<UserModel>

    /**
     * 获取指定分组下的所有数据
     *
     * @return 数据列表
     */
    @Query("SELECT * FROM user WHERE groupId = :id")
    fun getGroupUserData(id: Int): LiveData<List<UserModel>>

    /**
     * 删除数据
     */
    @Delete
    fun deleteUser(userModel: List<UserModel>)
}