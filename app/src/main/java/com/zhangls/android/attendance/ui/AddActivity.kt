package com.zhangls.android.attendance.ui

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.db.AbstractDatabase
import com.zhangls.android.attendance.db.entity.UserModel
import com.zhangls.android.attendance.util.closeKeyboard
import com.zhangls.android.attendance.util.snack
import kotlinx.android.synthetic.main.activity_add.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


/**
 * 添加分组
 *
 * @author zhangls
 */
class AddActivity : AppCompatActivity() {

    private var groupId: Int = 0
    private var groupName: String = ""

    companion object {
        /**
         * 分组 ID 标识符
         */
        private const val GROUP_ID = "group_id"
        /**
         * 分组名称标识符
         */
        private const val GROUP_NAME = "group_name"

        /**
         * Activity 入口方法
         */
        fun activityStart(context: Context, groupId: Int, groupName: String) {
            val intent = Intent(context, AddActivity::class.java)
            intent.putExtra(GROUP_ID, groupId)
            intent.putExtra(GROUP_NAME, groupName)

            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        // 设置标题
        title = getString(R.string.titleAddActivity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        groupId = intent.extras.getInt(GROUP_ID)
        groupName = intent.extras.getString(GROUP_NAME)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when {
            item?.itemId == android.R.id.home -> {
                finish()
                true
            }
            item?.itemId == R.id.menu_add_save -> {
                saveData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveData() {
        // 关闭键盘
        closeKeyboard(this, appEtName)
        closeKeyboard(this, appEtMac)
        closeKeyboard(this, appEtStudentId)
        closeKeyboard(this, appEtParentPhone)

        appEtName.error = null
        appEtMac.error = null
        appEtStudentId.error = null
        appEtParentPhone.error = null

        val nameStr = appEtName.text.toString()
        val macStr = appEtMac.text.toString()
        val studentIdStr = appEtStudentId.text.toString()
        val parentPhoneStr = appEtParentPhone.text.toString()

        // 检查姓名是否符合要求
        if (TextUtils.isEmpty(nameStr)) {
            appEtName.error = getString(R.string.error_invalid_name)
            appEtName.requestFocus()
            return
        }

        // 检查 MAC 是否符合要求
        if (TextUtils.isEmpty(macStr) || macStr.length != 17) {
            appEtMac.error = getString(R.string.error_invalid_mac)
            appEtMac.requestFocus()
            return
        }

        // 检查学生 ID 是否符合要求
        if (TextUtils.isEmpty(studentIdStr)) {
            appEtStudentId.error = getString(R.string.error_invalid_student_id)
            appEtStudentId.requestFocus()
            return
        }

        // 检查家长联系电话是否符合要求
        if (TextUtils.isEmpty(parentPhoneStr) || parentPhoneStr.length != 11) {
            appEtParentPhone.error = getString(R.string.error_invalid_parent_phone)
            appEtParentPhone.requestFocus()
            return
        }

        doAsync {
            val database = AbstractDatabase.getInstance(this@AddActivity)
            val user = UserModel(
                    0,
                    nameStr,
                    macStr.toUpperCase(),
                    groupId,
                    groupName,
                    studentIdStr,
                    parentPhoneStr)

            database.userDao().insertUser(user)
            uiThread {
                appEtName.setText("")
                appEtMac.setText("")
                appEtStudentId.setText("")
                appEtParentPhone.setText("")
                snack(appInputLayoutName, R.string.toastSaveSuccess)
            }
        }
    }
}
