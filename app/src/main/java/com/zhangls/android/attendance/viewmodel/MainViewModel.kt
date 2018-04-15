package com.zhangls.android.attendance.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Base64
import com.zhangls.android.attendance.AbstractDatabase
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.http.BaseApiRepository
import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.model.GroupModel
import com.zhangls.android.attendance.util.SharedPreferencesKey
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.doAsync

/**
 * 主页 ViewModel.
 *
 * @author zhangls
 */
class MainViewModel(application: Application) : BaseViewModel(application) {

    /**
     * 数据库操作对象
     */
    private lateinit var database: AbstractDatabase


    companion object {
        /**
         * 请求成功
         */
        const val GROUP_STATUS_SUCCESS = 1
        /**
         * token 问题
         */
        const val GROUP_STATUS_TOKEN = 2
        /**
         * 请求出错
         */
        const val GROUP_STATUS_ERROR = 3
    }

    /**
     * 分组列表
     */
    val groupList = MutableLiveData<ArrayList<GroupModel>>()
    /**
     * 获取分组信息的状态.
     *
     * 1: 获取 groupList 数据成功
     * 2: token 过期，或者为空
     * 3: 请求数据异常，网络请求失败
     */
    val groupStatus = MutableLiveData<Int>()
    private lateinit var baseApiRepository: BaseApiRepository

    /**
     * 获取分组信息
     */
    fun groupListRequest(context: Context) {
        // baseApiRepository 若没初始化，则先初始化
        if (!this::baseApiRepository.isInitialized) {
            baseApiRepository = BaseApiRepository(provider)
        }

        val preferences = context.getSharedPreferences(
                SharedPreferencesKey.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE)
        val encodeToken = preferences.getString(SharedPreferencesKey.TOKEN, "")
        if (encodeToken.isEmpty()) {
            groupStatus.value = GROUP_STATUS_TOKEN
            getToastString().value = context.getString(R.string.toastTokenError)
        } else {
            // 读取 token，并解密
            val tokenByteArray = Base64.decode(encodeToken, Base64.DEFAULT)
            val token = String(tokenByteArray)

            // 获取分组信息
            baseApiRepository.group(token, object : Observer<BaseModel<ArrayList<GroupModel>>> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: BaseModel<ArrayList<GroupModel>>) {
                    if (t.status == 200) {
                        groupStatus.value = GROUP_STATUS_SUCCESS
                        groupList.value = t.data
                        // 保存分组信息
                        saveGroup(t.data)
                    } else {
                        groupStatus.value = GROUP_STATUS_ERROR
                        getToastString().value = context.getString(R.string.toastNetworkError)
                    }
                }

                override fun onError(e: Throwable) {
                    groupStatus.value = GROUP_STATUS_ERROR
                    getToastString().value = context.getString(R.string.toastNetworkError)
                }
            })
        }

    }

    /**
     * 每次用户登录成功时，清空数据库数据（因为没有服务器，导致数据不能实时处理），本操作需要异步
     */
    fun clearDatabase(context: Context) {
        doAsync {
            if (!this@MainViewModel::database.isInitialized) {
                database = AbstractDatabase.get(context)
            }
            // 清空分组
            val allGroup = database.groupDao().getAllGroup()
            database.groupDao().deleteGroup(allGroup)
            // 清空用户
            val allUer = database.userDao().getAllUser()
            database.userDao().deleteUser(allUer)
        }
    }

    /**
     * 保存分组信息，异步
     */
    private fun saveGroup(groups: ArrayList<GroupModel>) {
        doAsync {
            if (this@MainViewModel::database.isInitialized) {
                database.groupDao().insertGroup(groups)
            } else {
                // 数据库未初始化，出现异常
            }
        }
    }
}