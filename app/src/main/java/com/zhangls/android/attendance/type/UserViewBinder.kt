package com.zhangls.android.attendance.type

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zhangls.android.attendance.db.AbstractDatabase

import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.db.entity.UserModel

import me.drakeet.multitype.ItemViewBinder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*


/**
 * 考勤人员信息 Item
 *
 * @author zhangls
 */
class UserViewBinder(private val isView: Boolean) : ItemViewBinder<UserModel, UserViewBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val root = inflater.inflate(R.layout.item_user, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, user: UserModel) {
        val context = holder.itemView.context

        holder.name.text = user.userName

        val date = Date(if (user.modifyTime == 0L) System.currentTimeMillis() else user.modifyTime)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        holder.time.text = format.format(date)
        if (user.status) {
            holder.status.text = context.getString(R.string.groupAttendanceCompleted)
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.colorOrange))

            holder.operate.visibility = View.GONE
        } else {
            holder.status.text = context.getString(R.string.groupAttendanceUnstarted)
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))

            holder.operate.visibility = if (isView) View.GONE else View.VISIBLE
            holder.operate.text = context.getString(R.string.groupAttendanceStart)
            holder.operate.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            holder.operate.setOnClickListener({
                // 教师可以通过手动点击考勤按钮给学生考勤
                doAsync {
                    val database = AbstractDatabase.getInstance(context)

                    user.status = true
                    user.modifyTime = System.currentTimeMillis()

                    database.userDao().updateUser(user)
                }
            })
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.find(R.id.tvName)
        val status: TextView = itemView.find(R.id.tvStatus)
        val time: TextView = itemView.find(R.id.tvTime)
        val operate: TextView = itemView.find(R.id.tvOperate)
    }
}
