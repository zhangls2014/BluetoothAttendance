package com.zhangls.android.attendance.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Base64
import com.zhangls.android.attendance.AbstractDatabase
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.http.BaseApiRepository
import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.model.UserModel
import com.zhangls.android.attendance.util.SharedPreferencesKey
import io.reactivex.Observer
import io.reactivex.disposables.Disposable


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
        database = AbstractDatabase.get(context)
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
        if (!this::database.isInitialized) {
            database = AbstractDatabase.get(context)
        }
        listStatus.value = STATUS_SUCCESS
        listMember.value = database.userDao().getGroupUser(groupId)
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
                // 保存考勤人员信息
                database.userDao().insertUser(t.data)
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
}