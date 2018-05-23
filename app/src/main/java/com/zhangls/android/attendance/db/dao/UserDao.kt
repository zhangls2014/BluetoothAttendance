package com.zhangls.android.attendance.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.zhangls.android.attendance.db.entity.UserModel

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

    @Update
    fun updateUser(userModel: UserModel)

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
    @Query("DELETE FROM user")
    fun deleteUser()

    /**
     * 查询指定分组下特定的蓝牙 MAC 地址的人员
     */
    @Query("SELECT * FROM user WHERE groupId = :id AND bleMac LIKE :mac LIMIT 1")
    fun attendance(id: Int, mac: String): UserModel?
}