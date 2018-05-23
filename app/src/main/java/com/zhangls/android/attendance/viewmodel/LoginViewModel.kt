package com.zhangls.android.attendance.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import com.zhangls.android.attendance.db.AbstractDatabase
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.http.BaseApiRepository
import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.model.LoginModel
import com.zhangls.android.attendance.util.SharedPreferencesKey
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * 登录功能 ViewModel
 *
 * @author zhangls
 */
class LoginViewModel(application: Application) : BaseViewModel(application) {

    val loginStatus = MutableLiveData<Boolean>()
    private lateinit var baseApiRepository: BaseApiRepository

    /**
     * 判断账户是否有效。因为没有正式的服务器，所以在本地判断账户是否正确
     */
    fun isAccountValid(email: String): Boolean {
        return email.length == 11
    }

    /**
     * 判断密码是否有效。因为没有正式的服务器，所以在本地判断密码是否正确
     */
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * 从服务器获取登录信息
     */
    fun loginRequest(context: Context, username: String, password: String) {
        if (!this::baseApiRepository.isInitialized) {
            baseApiRepository = BaseApiRepository(provider)
        }

        baseApiRepository.login(username, password, object : Observer<BaseModel<LoginModel>> {
            override fun onComplete() {

            }

            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: BaseModel<LoginModel>) {
                if (t.status == 200) {
                    doAsync {
                        // 保存登录信息, 通过 Base64 加密
                        val preferences = context.getSharedPreferences(
                                SharedPreferencesKey.SHARED_PREFERENCES_NAME,
                                Context.MODE_PRIVATE)
                        preferences.edit {
                            putString(SharedPreferencesKey.TOKEN,
                                    Base64.encodeToString(t.data.token.toByteArray(), Base64.DEFAULT))
                            putString(SharedPreferencesKey.PHONE_NUM,
                                    Base64.encodeToString(t.data.phoneNum.toByteArray(), Base64.DEFAULT))
                            putString(SharedPreferencesKey.USER_ID,
                                    Base64.encodeToString(t.data.userId.toByteArray(), Base64.DEFAULT))
                            putString(SharedPreferencesKey.USERNAME,
                                    Base64.encodeToString(t.data.userName.toByteArray(), Base64.DEFAULT))
                        }
                        // 清空数据库
                        val database = AbstractDatabase.getInstance(context)
                        // 清空分组
                        database.groupDao().deleteGroup()
                        // 清空用户
                        database.userDao().deleteUser()
                        uiThread { loginStatus.value = true }
                    }
                } else {
                    loginStatus.value = false
                    getToastString().value = context.getString(R.string.toastNetworkError)
                }
            }

            override fun onError(e: Throwable) {
                loginStatus.value = false
                getToastString().value = context.getString(R.string.toastNetworkError)
            }
        })
    }
}