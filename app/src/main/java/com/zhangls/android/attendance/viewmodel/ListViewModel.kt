package com.zhangls.android.attendance.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Base64
import com.zhangls.android.attendance.db.AbstractDatabase
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.http.BaseApiRepository
import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.db.entity.UserModel
import com.zhangls.android.attendance.util.SharedPreferencesKey
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


/**
 * 考勤人员列表 ViewModel
 *
 * @author zhangls
 */
class ListViewModel(application: Application) : BaseViewModel(application) {

    /**
     * 数据库操作对象
     */
    lateinit var database: AbstractDatabase
    /**
     * 获取网络请求状态.
     *
     * 1: 获取 memberList 数据成功
     * 2: token 过期，或者为空
     * 3: 请求数据异常，网络请求失败
     */
    val listStatus = MutableLiveData<Int>()
    /**
     * 考勤人员信息
     */
    val listMember = MutableLiveData<List<UserModel>>()
    val attendanceFinish = MutableLiveData<Boolean>()
    private lateinit var baseApiRepository: BaseApiRepository


    companion object {
        /**
         * 请求成功
         */
        const val STATUS_SUCCESS = 1
        /**
         * token 问题
         */
        const val STATUS_TOKEN = 2
        /**
         * 请求出错
         */
        const val STATUS_ERROR = 3
    }

    /**
     * 初始化数据库
     */
    fun initDatabase(context: Context) {
        database = AbstractDatabase.getInstance(context)

        attendanceFinish.value = false
    }

    /**
     * 获取考勤人员信息
     *
     * @param groupId 分组 ID
     */
    fun listMemberRequest(context: Context, groupId: Int) {
        // baseApiRepository 若没初始化，则先初始化
        if (!this::baseApiRepository.isInitialized) {
            baseApiRepository = BaseApiRepository(provider)
        }

        val preferences = context.getSharedPreferences(
                SharedPreferencesKey.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE)
        val encodeToken = preferences.getString(SharedPreferencesKey.TOKEN, "")
        if (encodeToken.isEmpty()) {
            listStatus.value = STATUS_TOKEN
            getToastString().value = context.getString(R.string.toastTokenError)
        } else {
            // 读取 token，并解密
            val tokenByteArray = Base64.decode(encodeToken, Base64.DEFAULT)
            val token = String(tokenByteArray)

            // 获取考勤信息
            getData(token, groupId, QuestObserver(context))
        }
    }

    /**
     * 抓取所有的分组成员信息
     */
    fun getListMember(context: Context, groupId: Int) {
        doAsync {
            if (!this@ListViewModel::database.isInitialized) {
                database = AbstractDatabase.getInstance(context)
            }
            val user = database.userDao().getGroupUser(groupId)
            uiThread {
                listMember.value = user
                listStatus.value = STATUS_SUCCESS
            }
        }
    }

    /**
     * 获取数据
     *
     * @param token token
     * @param groupId 分组 ID
     */
    private fun getData(token: String, groupId: Int, observer: QuestObserver) {
        when (groupId) {
            1 -> baseApiRepository.attendanceList1(token, groupId, observer)
            2 -> baseApiRepository.attendanceList2(token, groupId, observer)
            3 -> baseApiRepository.attendanceList3(token, groupId, observer)
            else -> {
            }
        }
    }

    /**
     * 网络请求回调
     */
    inner class QuestObserver(val context: Context) : Observer<BaseModel<ArrayList<UserModel>>> {
        override fun onComplete() {

        }

        override fun onSubscribe(d: Disposable) {
        }

        override fun onNext(t: BaseModel<ArrayList<UserModel>>) {
            if (t.status == 200) {
                listStatus.value = STATUS_SUCCESS
                doAsync {
                    // 保存考勤人员信息
                    database.userDao().insertUser(t.data)
                }
            } else {
                listStatus.value = STATUS_ERROR
                getToastString().value = context.getString(R.string.toastNetworkError)
            }
        }

        override fun onError(e: Throwable) {
            listStatus.value = STATUS_ERROR
            getToastString().value = context.getString(R.string.toastNetworkError)
        }
    }

    /**
     * 停止考勤
     */
    fun completedAttendance(context: Context, groupId: Int) {
        doAsync {
            if (!this@ListViewModel::database.isInitialized) {
                database = AbstractDatabase.getInstance(context)
            }

            val group = database.groupDao().queryGroup(groupId)
            group.status = true
            group.modifyTime = System.currentTimeMillis()
            database.groupDao().insertGroup(listOf(group))
        }
    }

    /**
     * 考勤
     *
     * @param id 分组 ID
     * @param bleMac 扫描的蓝牙 MAC 地址
     */
    fun attendance(id: Int, bleMac: String) {
        doAsync {
            val userModel = database.userDao().attendance(id, bleMac)

            if (userModel != null) {
                userModel.status = true
                userModel.modifyTime = System.currentTimeMillis()

                database.userDao().updateUser(userModel)
            }
            if (!attendanceFinish.value!!) {
                // 判断该分组内是否存在未考勤人员，如果不错在则结束考勤
                if (database.userDao().attendanceFinish(id) == null
                        || database.userDao().attendanceFinish(id)?.size == 0) {
                    uiThread {
                        attendanceFinish.value = true
                    }
                }
            }
        }
    }

    /**
     * 添加照片路径
     *
     * @param path 照片路径
     * @param id 分组 ID
     */
    fun addFilePath(path: String, id: Int) {
        doAsync {
            val group = database.groupDao().queryGroup(id)

            group.imagePath = path
            database.groupDao().insertGroup(group)
        }
    }

    /**
     * 获取照片路径
     *
     * @param id 分组 ID
     */
    fun getFilePath(id: Int): String {
        return database.groupDao().queryGroup(id).imagePath.orEmpty()
    }
}