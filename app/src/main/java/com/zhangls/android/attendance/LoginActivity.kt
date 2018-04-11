package com.zhangls.android.attendance

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_login.*

/**
 * 登录页面
 *
 * @author zhangls
 */
class LoginActivity : AppCompatActivity() {

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
        appEtUsername.error = null
        appEtPassword.error = null

        val accountStr = appEtUsername.text.toString()
        val passwordStr = appEtPassword.text.toString()

        var cancel = false
        var focusView: View? = null

        // 检查密码是否符合要求
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            appEtPassword.error = getString(R.string.error_invalid_password)
            focusView = appEtPassword
            cancel = true
        }

        // 检查账号是否有效
        if (TextUtils.isEmpty(accountStr)) {
            appEtUsername.error = getString(R.string.error_field_required)
            focusView = appEtUsername
            cancel = true
        } else if (!isAccountValid(accountStr)) {
            appEtUsername.error = getString(R.string.error_invalid_account)
            focusView = appEtUsername
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            showProgress(true)

            login()
        }
    }

    /**
     * 判断账户是否有效。因为没有正式的服务器，所以在本地判断账户是否正确
     */
    private fun isAccountValid(email: String): Boolean {
        return email.length == 11
    }

    /**
     * 判断密码是否有效。因为没有正式的服务器，所以在本地判断密码是否正确
     */
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        setVisibility(show)

        setAnimate(show, appLoginProgress, shortAnimTime)
        setAnimate(show, appInputLayoutUserName, shortAnimTime)
        setAnimate(show, appInputLayoutPassword, shortAnimTime)
        setAnimate(show, appBtnLogin, shortAnimTime)
    }

    /**
     * 设置界面的可见性
     */
    private fun setVisibility(show: Boolean) {
        if (show) {
            appInputLayoutUserName.visibility = View.GONE
            appInputLayoutPassword.visibility = View.GONE
            appBtnLogin.visibility = View.GONE

            appLoginProgress.visibility = View.VISIBLE
        } else {
            appInputLayoutUserName.visibility = View.VISIBLE
            appInputLayoutPassword.visibility = View.VISIBLE
            appBtnLogin.visibility = View.VISIBLE

            appLoginProgress.visibility = View.GONE
        }
    }

    /**
     * 为界面显隐设置动画
     *
     * @param show 是否可见
     * @param view 视图
     * @param shortAnimTime 动画时长
     */
    private fun setAnimate(show: Boolean, view: View, shortAnimTime: Long) {
        view.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })
    }

    /**
     * 登录
     */
    private fun login() {
        if (!appEtUsername.text.toString().contentEquals("18166668888")) {
            showProgress(false)
            appEtUsername.error = getString(R.string.error_invalid_account)
            appEtUsername.requestFocus()
        } else if (!appEtPassword.text.toString().contentEquals("123456")) {
            showProgress(false)
            appEtPassword.error = getString(R.string.error_invalid_password)
            appEtPassword.requestFocus()
        }
    }
}
