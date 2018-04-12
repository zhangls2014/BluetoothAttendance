package com.zhangls.android.attendance.http

import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.model.LoginModel
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * 网络请求接口
 *
 * @author zhangls
 */
interface ApiMethod {

    /**
     * A3 手机号登陆
     *
     * @param phone    手机号码
     * @param password 密码
     * @return 登录状态
     */
    @FormUrlEncoded
    @POST("v1/login")
    fun login(
            @Field("username") phone: String,
            @Field("password") password: String
    ): Observable<BaseModel<LoginModel>>
}