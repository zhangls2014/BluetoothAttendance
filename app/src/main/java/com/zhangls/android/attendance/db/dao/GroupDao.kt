package com.zhangls.android.attendance.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.zhangls.android.attendance.db.entity.GroupModel

/**
 * 分组信息 Dao 类
 *
 * @author zhangls
 */
@Dao
interface GroupDao {

    /**
     * 添加分组数据
     *
     * @param groupModel 分组
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(groupModel: List<GroupModel>)

    /**
     * 添加分组数据
     *
     * @param groupModel 分组
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGroup(groupModel: GroupModel)

    /**
     * 获取所有数据
     *
     * @return 数据列表
     */
    @Query("SELECT * FROM grouping")
    fun getAllGroup(): List<GroupModel>

    /**
     * 获取所有数据
     *
     * @return 数据列表
     */
    @Query("SELECT * FROM grouping")
    fun getAllGroupData(): LiveData<List<GroupModel>>

    /**
     * 删除数据
     */
    @Query("DELETE FROM grouping")
    fun deleteGroup()

    /**
     * 查询某一个分组数据
     */
    @Query("SELECT * FROM grouping WHERE groupId = :groupId LIMIT 1")
    fun queryGroup(groupId: Int): GroupModel

    /**
     * 更新所有分组
     */
    @Update
    fun updateGroup(groupModel: List<GroupModel>)

    /**
     * 更新分组
     */
    @Update
    fun updateGroup(groupModel: GroupModel)
}