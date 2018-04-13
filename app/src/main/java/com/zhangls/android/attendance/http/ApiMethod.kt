package com.zhangls.android.attendance.http

import com.zhangls.android.attendance.model.BaseModel
import com.zhangls.android.attendance.model.GroupModel
import com.zhangls.android.attendance.model.LoginModel
import com.zhangls.android.attendance.model.UserModel
import io.reactivex.Observable
import retrofit2.http.*

/**
 * 网络请求接口
 *
 * @author zhangls
 */
interface ApiMethod {

    /**
     * U1 账号登陆
     *
     * @param username 账号
     * @param password 密码
     * @return 登录信息
     */
    @FormUrlEncoded
    @POST("v1/login")
    fun login(
            @Field("username") username: String,
            @Field("password") password: String
    ): Observable<BaseModel<LoginModel>>

    /**
     * U2 获取分组信息
     *
     * @param token token
     * @return 分组列表
     */
    @GET("v1/groupList")
    fun group(@Header("token") token: String): Observable<BaseModel<ArrayList<GroupModel>>>

    /**
     * U301 待考勤人员列表-1
     *
     * @param token token
     * @param groupId 分组 ID
     * @return 待考勤人员列表
     */
    @GET("v1/attendanceList1")
    fun attendanceList1(
            @Header("token") token: String,
            @Query("groupId") groupId: String
    ): Observable<BaseModel<ArrayList<UserModel>>>

    /**
     * U301 待考勤人员列表-2
     *
     * @param token token
     * @param groupId 分组 ID
     * @return 待考勤人员列表
     */
    @GET("v1/attendanceList2")
    fun attendanceList2(
            @Header("token") token: String,
            @Query("groupId") groupId: String
    ): Observable<BaseModel<ArrayList<UserModel>>>

    /**
     * U301 待考勤人员列表-3
     *
     * @param token token
     * @param groupId 分组 ID
     * @return 待考勤人员列表
     */
    @GET("v1/attendanceList3")
    fun attendanceList3(
            @Header("token") token: String,
            @Query("groupId") groupId: String
    ): Observable<BaseModel<ArrayList<UserModel>>>
}