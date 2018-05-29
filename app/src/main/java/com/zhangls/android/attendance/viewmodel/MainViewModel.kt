package com.zhangls.android.attendance.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import com.zhangls.android.attendance.db.AbstractDatabase
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.http.BaseApiRepository
import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.db.entity.GroupModel
import com.zhangls.android.attendance.util.SharedPreferencesKey
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import android.os.Environment
import me.xiaosai.imagecompress.ImageCompress


/**
 * 主页 ViewModel.
 *
 * @author zhangls
 */
class MainViewModel(application: Application) : BaseViewModel(application) {

    /**
     * 数据库操作对象
     */
    lateinit var database: AbstractDatabase
    /**
     * 获取分组信息的状态.
     *
     * 1: 获取 groupList 数据成功
     * 2: token 过期，或者为空
     * 3: 请求数据异常，网络请求失败
     * 4: 上传信息成功
     */
    val groupStatus = MutableLiveData<Int>()
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
    }

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
            groupStatus.value = STATUS_TOKEN
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
                        groupStatus.value = STATUS_SUCCESS
                        doAsync {
                            // 保存分组信息
                            database.groupDao().insertGroup(t.data)
                        }
                    } else {
                        groupStatus.value = STATUS_ERROR
                        getToastString().value = context.getString(R.string.toastNetworkError)
                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                    groupStatus.value = STATUS_ERROR
                    getToastString().value = context.getString(R.string.toastNetworkError)
                }
            })
        }
    }

    /**
     * 添加分组
     *
     * @param groupName 分组名称
     */
    fun addGroup(groupName: String) {
        doAsync {
            val groupModel = GroupModel(0, groupName, false, System.currentTimeMillis())
            database.groupDao().insertGroup(groupModel)
        }
    }

    fun updateGroup(context: Context, id: Int) {
        // baseApiRepository 若没初始化，则先初始化
        if (!this::baseApiRepository.isInitialized) {
            baseApiRepository = BaseApiRepository(provider)
        }

        doAsync {
            val group = database.groupDao().queryGroup(id)
            val user = database.userDao().getGroupUser(id)

            val groupJson = Gson().toJson(group)
            val userJson = Gson().toJson(user)

            ImageCompress.with(context)
                    .load(group.imagePath)
                    .setTargetDir(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).path)
                    .ignoreBy(300)
                    .setOnCompressListener(object : ImageCompress.OnCompressListener {
                        override fun onStart() {}

                        override fun onSuccess(filePath: String) {
                            baseApiRepository.updateGroup(groupJson, userJson, File(filePath), object : Observer<BaseModel<String>> {
                                override fun onComplete() {
                                }

                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onNext(t: BaseModel<String>) {
                                    if (t.status == 200) {
                                        doAsync {
                                            group.upload = true
                                            group.imagePath = filePath
                                            database.groupDao().updateGroup(group)
                                        }

                                        groupStatus.value = STATUS_SUCCESS
                                        getToastString().value = context.getString(R.string.toastUploadSuccess)
                                    } else {
                                        groupStatus.value = STATUS_ERROR
                                        getToastString().value = context.getString(R.string.toastNetworkError)
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    e.printStackTrace()
                                    groupStatus.value = STATUS_ERROR
                                    getToastString().value = context.getString(R.string.toastNetworkError)
                                }

                            })
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            uiThread {
                                groupStatus.value = STATUS_ERROR
                                getToastString().value = context.getString(R.string.toastNetworkError)
                            }
                        }
                    })
                    .launch()
        }
    }
}