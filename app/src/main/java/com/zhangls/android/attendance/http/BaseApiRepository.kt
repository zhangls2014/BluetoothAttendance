package com.zhangls.android.attendance.http

import android.arch.lifecycle.Lifecycle
import com.trello.rxlifecycle2.LifecycleProvider
import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.model.GroupModel
import com.zhangls.android.attendance.model.LoginModel
import com.zhangls.android.attendance.model.UserModel
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

    /**
     * @see ApiMethod.group
     */
    fun group(token: String, observer: Observer<BaseModel<ArrayList<GroupModel>>>) {
        ApiService.default
                .create(ApiMethod::class.java)
                .group(token)
                .subscribeOn(Schedulers.io())
                .compose(provider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    /**
     * @see ApiMethod.attendanceList1
     */
    fun attendanceList1(token: String, groupId: String,
                        observer: Observer<BaseModel<ArrayList<UserModel>>>) {
        ApiService.default
                .create(ApiMethod::class.java)
                .attendanceList1(token, groupId)
                .subscribeOn(Schedulers.io())
                .compose(provider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    /**
     * @see ApiMethod.attendanceList2
     */
    fun attendanceList2(token: String, groupId: String,
                        observer: Observer<BaseModel<ArrayList<UserModel>>>) {
        ApiService.default
                .create(ApiMethod::class.java)
                .attendanceList2(token, groupId)
                .subscribeOn(Schedulers.io())
                .compose(provider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    /**
     * @see ApiMethod.attendanceList3
     */
    fun attendanceList3(token: String, groupId: String,
                        observer: Observer<BaseModel<ArrayList<UserModel>>>) {
        ApiService.default
                .create(ApiMethod::class.java)
                .attendanceList3(token, groupId)
                .subscribeOn(Schedulers.io())
                .compose(provider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }
}