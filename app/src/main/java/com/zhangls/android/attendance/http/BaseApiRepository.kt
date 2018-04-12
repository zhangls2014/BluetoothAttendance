package com.zhangls.android.attendance.http

import android.arch.lifecycle.Lifecycle
import com.trello.rxlifecycle2.LifecycleProvider
import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.model.LoginModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class BaseApiRepository(private val provider: LifecycleProvider<Lifecycle.Event>) {

    /**
     * @see ApiMethod.login
     */
    fun login(username: String, password: String, observer: Observer<BaseModel<LoginModel>>) {
        ApiService.default
                .create(ApiMethod::class.java)
                .login(username, password)
                .subscribeOn(Schedulers.io())
                .compose(provider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }
}