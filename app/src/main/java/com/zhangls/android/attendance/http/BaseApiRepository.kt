package com.zhangls.android.attendance.http

import android.arch.lifecycle.Lifecycle
import com.trello.rxlifecycle2.LifecycleProvider
import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.db.entity.GroupModel
import com.zhangls.android.attendance.model.LoginModel
import com.zhangls.android.attendance.db.entity.UserModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


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
    fun attendanceList1(token: String, groupId: Int,
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
    fun attendanceList2(token: String, groupId: Int,
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
    fun attendanceList3(token: String, groupId: Int,
                        observer: Observer<BaseModel<ArrayList<UserModel>>>) {
        ApiService.default
                .create(ApiMethod::class.java)
                .attendanceList3(token, groupId)
                .subscribeOn(Schedulers.io())
                .compose(provider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    /**
     * @see ApiMethod.updateGroup
     */
    fun updateGroup(group: String, user: String, image: File, observer: Observer<BaseModel<String>>) {
        val builder = MultipartBody.Builder()

        builder.addFormDataPart("group", null,
                RequestBody.create(MediaType.parse("text/plain"), group))
        builder.addFormDataPart("user", null,
                RequestBody.create(MediaType.parse("text/plain"), user))
        val requestBody = RequestBody.create(MediaType.parse("image/*"), image)
        builder.addFormDataPart("publish_img", image.name, requestBody)

        builder.setType(MultipartBody.FORM)
        ApiService.default
                .create(ApiMethod::class.java)
                .updateGroup(builder.build())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .compose(provider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }
}