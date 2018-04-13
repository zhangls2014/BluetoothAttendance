package com.zhangls.android.attendance.viewmodel

import android.app.Application
import com.zhangls.android.attendance.http.BaseApiRepository

/**
 * 主页 ViewModel
 *
 * @author zhangls
 */
class MainViewModel(application: Application) : BaseViewModel(application) {

    private lateinit var baseApiRepository: BaseApiRepository


}