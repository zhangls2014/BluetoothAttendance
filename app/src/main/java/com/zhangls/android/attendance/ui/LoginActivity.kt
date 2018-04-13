package com.zhangls.android.attendance.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle2.LifecycleProvider
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.util.closeKeyboard
import com.zhangls.android.attendance.util.snack
import com.zhangls.android.attendance.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast


/**
 * 登录页面
 *
 * @author zhangls
 */
class LoginActivity : AppCompatActivity() {

    private val provider: LifecycleProvider<Lifecycle.Event> = AndroidLifecycle.createLifecycleProvider(this)
    private lateinit var loginViewModel: LoginViewModel
    private val myHandler = Handler()
    private val mLoadingRunnable = Runnable { updateUI() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.decorView.post { myHandler.post(mLoadingRunnable) }
    }

    /**
     * 页面已经加载后，加载界面，提升应用启动速度，减少白屏时间
     */
    private fun updateUI() {
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        loginViewModel.setupProvider(provider)

        // 消息显示监听
        loginViewModel.getToastString().observe(this, Observer {
            snack(appBtnLogin, it!!)
        })
        // 登录状态监听
        loginViewModel.loginStatus.observe(this, Observer {
            if (it == null) {
                return@Observer
            }

            if (it) {
                // 如果返回 true，表明已经登录成功，跳转主页
                toast(R.string.toastLoginRequestSuccess)
                MainActivity.activityStart(this@LoginActivity)
                finish()
            } else {
                showProgress(false)
            }
        })

        // 密码数据框键盘点击事件监听
        appEtPassword.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                // 键盘上点击 enter 键进行登录操作
                attemptLogin()
                return@OnEditorActionListener true
            } else {
                false
            }
        })
        // 登录按钮点击事件监听
        appBtnLogin.setOnClickListener { attemptLogin() }
    }

    /**
     * 登录判断
     */
    private fun attemptLogin() {
        // 关闭键盘
        closeKeyboard(this, appEtUsername)
        closeKeyboard(this, appEtPassword)

        appEtUsername.error = null
        appEtPassword.error = null

        val accountStr = appEtUsername.text.toString()
        val passwordStr = appEtPassword.text.toString()

        var cancel = false
        var focusView: View? = null

        // 检查密码是否符合要求
        if (!TextUtils.isEmpty(passwordStr) && !loginViewModel.isPasswordValid(passwordStr)) {
            appEtPassword.error = getString(R.string.error_invalid_password)
            focusView = appEtPassword
            cancel = true
        }

        // 检查账号是否有效
        if (TextUtils.isEmpty(accountStr)) {
            appEtUsername.error = getString(R.string.error_field_required)
            focusView = appEtUsername
            cancel = true
        } else if (!loginViewModel.isAccountValid(accountStr)) {
            appEtUsername.error = getString(R.string.error_invalid_account)
            focusView = appEtUsername
            cancel = true
        }

        if (cancel) focusView?.requestFocus() else login()
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        setAnimate(show, appLoginProgress, shortAnimTime)
        setAnimate(!show, appInputLayoutUserName, shortAnimTime)
        setAnimate(!show, appInputLayoutPassword, shortAnimTime)
        setAnimate(!show, appBtnLogin, shortAnimTime)
    }

    /**
     * 为界面显隐设置动画
     *
     * @param show 界面是否可见
     * @param view 视图
     * @param shortAnimTime 动画时长
     */
    private fun setAnimate(show: Boolean, view: View, shortAnimTime: Long) {
        view.animate().cancel()
        view.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    /**
     * 登录
     */
    private fun login() {
        val accountStr = appEtUsername.text.toString()
        val passwordStr = appEtPassword.text.toString()

        when {
            accountStr != "18166668888" -> {
                appEtUsername.error = getString(R.string.error_invalid_account)
                appEtUsername.requestFocus()
            }
            passwordStr != "123456" -> {
                appEtPassword.error = getString(R.string.error_invalid_password)
                appEtPassword.requestFocus()
            }
            else -> {
                showProgress(true)
                loginViewModel.loginRequest(this, accountStr, passwordStr)
            }
        }

    }
}
