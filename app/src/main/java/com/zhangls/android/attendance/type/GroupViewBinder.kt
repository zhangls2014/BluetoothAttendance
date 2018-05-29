package com.zhangls.android.attendance.type

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.db.AbstractDatabase
import com.zhangls.android.attendance.db.entity.GroupModel
import com.zhangls.android.attendance.ui.ListActivity

import me.drakeet.multitype.ItemViewBinder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*


/**
 * 分组列表 Item
 *
 * @author zhangls
 */
class GroupViewBinder : ItemViewBinder<GroupModel, GroupViewBinder.ViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val root = inflater.inflate(R.layout.item_group, parent, false)
        return ViewHolder(root)
    }

    override fun onBindViewHolder(holder: ViewHolder, group: GroupModel) {
        val context = holder.itemView.context

        holder.name.text = group.groupName
        val date = Date(if (group.modifyTime == 0L) System.currentTimeMillis() else group.modifyTime)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        holder.time.text = format.format(date)
        if (group.status) {
            holder.status.text = context.getString(R.string.groupAttendanceCompleted)
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.colorOrange))
            doAsync {
                val database = AbstractDatabase.getInstance(context)
                val user = database.userDao().getGroupUser(group.groupId)
                val attendance = database.userDao().attendanceFinish(group.groupId)
                uiThread {
                    holder.count.visibility = View.VISIBLE
                    when {
                        user == null -> holder.count.text = String.format(
                                context.getString(R.string.formatAttendanceRatio),
                                0,
                                0
                        )
                        attendance == null -> holder.count.text = String.format(
                                context.getString(R.string.formatAttendanceRatio),
                                user.size,
                                0
                        )
                        else -> holder.count.text = String.format(
                                context.getString(R.string.formatAttendanceRatio),
                                user.size - attendance.size,
                                attendance.size
                        )
                    }
                }
            }

            holder.operate.text = context.getString(R.string.groupAttendanceRecord)
            holder.operate.setTextColor(ContextCompat.getColor(context, R.color.colorOrange))
            holder.operate.setOnClickListener({
                ListActivity.activityStart(context, true, group.groupId, group.groupName)
            })
        } else {
            holder.count.visibility = View.GONE

            holder.status.text = context.getString(R.string.groupAttendanceUnstarted)
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))

            holder.operate.text = context.getString(R.string.groupAttendanceStart)
            holder.operate.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            holder.operate.setOnClickListener({
                ListActivity.activityStart(context, false, group.groupId, group.groupName)
            })
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.find(R.id.tvName)
        val status: TextView = itemView.find(R.id.tvStatus)
        val time: TextView = itemView.find(R.id.tvTime)
        val operate: TextView = itemView.find(R.id.tvOperate)
        val count: TextView = itemView.find(R.id.tvCount)
    }
}
