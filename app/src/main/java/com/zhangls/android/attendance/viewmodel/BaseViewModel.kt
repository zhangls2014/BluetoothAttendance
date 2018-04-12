package com.zhangls.android.attendance.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.MutableLiveData
import com.trello.rxlifecycle2.LifecycleProvider

/**
 * ViewModel 基类
 *
 * @author zhangls
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    protected lateinit var provider: LifecycleProvider<Lifecycle.Event>
    protected var context = application.applicationContext
    /**
     * 提示文字
     */
    private val toastString = MutableLiveData<String>()


    fun setupProvider(provider: LifecycleProvider<Lifecycle.Event>) {
        this@BaseViewModel.provider = provider
    }

    /**
     * 获取提示文字
     *
     * @return 提示文字
     */
    fun getToastString(): MutableLiveData<String> {
        return toastString
    }
}