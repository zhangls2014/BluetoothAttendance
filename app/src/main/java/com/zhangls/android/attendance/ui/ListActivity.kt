package com.zhangls.android.attendance.ui

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.animation.AnimationUtils
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle2.LifecycleProvider
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.model.UserModel
import com.zhangls.android.attendance.type.UserViewBinder
import com.zhangls.android.attendance.util.snack
import com.zhangls.android.attendance.viewmodel.ListViewModel
import com.zhangls.android.attendance.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter


/**
 * 考勤人员列表
 *
 * @author zhangls
 */
class ListActivity : AppCompatActivity() {

    private val provider: LifecycleProvider<Lifecycle.Event> = AndroidLifecycle.createLifecycleProvider(this)
    private lateinit var listViewModel: ListViewModel
    private val items = Items()
    private val adapter = MultiTypeAdapter(items)


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
         * 是否是查看信息
         */
        private const val GROUP_VIEW = "group_view"

        /**
         * Activity 入口方法
         */
        fun activityStart(context: Context, isView: Boolean, groupId: Int, groupName: String) {
            val intent = Intent(context, ListActivity::class.java)
            intent.putExtra(GROUP_VIEW, isView)
            intent.putExtra(GROUP_ID, groupId)
            intent.putExtra(GROUP_NAME, groupName)

            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        // 设置标题
        title = intent.extras.getString(GROUP_NAME)

        // 设置下拉刷新组件的颜色
        srlRefresh.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPink,
                R.color.colorPurple,
                R.color.colorLime,
                R.color.colorOrange)
        srlRefresh.setOnRefreshListener {
            if (intent.extras.getBoolean(GROUP_VIEW)) {
                listViewModel.getListMember(this, intent.extras.getInt(GROUP_ID))
            } else {
                listViewModel.listMemberRequest(this, intent.extras.getInt(GROUP_ID))
            }
        }

        listViewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        listViewModel.setupProvider(provider)
        listViewModel.initDatabase(this)

        observeData()

        // 配置 RecyclerView
        adapter.register(UserModel::class.java, UserViewBinder(intent.extras.getBoolean(GROUP_VIEW)))
        rvGroupList.layoutManager = LinearLayoutManager(this)
        rvGroupList.adapter = adapter
        // 设置动画
        rvGroupList.layoutAnimation =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_anim_full_down)

        // 显示进度条，获取考勤人员信息
        mainProgress.show()
        if (intent.extras.getBoolean(GROUP_VIEW)) {
            listViewModel.getListMember(this, intent.extras.getInt(GROUP_ID))
        } else {
            listViewModel.listMemberRequest(this, intent.extras.getInt(GROUP_ID))
        }
    }

    /**
     * 实现 MainViewModel 变量监听
     */
    private fun observeData() {
        // 消息显示监听
        listViewModel.getToastString().observe(this, Observer {
            snack(rvGroupList, it!!)
        })
        // 获取网络请求监听
        listViewModel.listStatus.observe(this, Observer {
            when (it) {
                MainViewModel.STATUS_TOKEN -> {
                    LoginActivity.activityStart(this)
                    finish()
                }
                MainViewModel.STATUS_SUCCESS -> {
                    if (srlRefresh.isRefreshing) srlRefresh.isRefreshing = false
                    rvGroupList.visibility = View.VISIBLE
                    mainProgress.hide()
                }
                MainViewModel.STATUS_ERROR -> {
                    if (srlRefresh.isRefreshing) srlRefresh.isRefreshing = false
                    rvGroupList.visibility = View.VISIBLE
                    mainProgress.hide()
                }
            }
        })

        // 分组信息监听
        listViewModel.database.userDao().getGroupUserData(intent.extras.getInt(GROUP_ID))
                .observe(this, Observer {
                    if (it == null || it.isEmpty()) {
                        snack(rvGroupList, R.string.toastDataEmpty)
                    } else {
                        loadData(it)
                    }
                })
        // 获取本地数据
        listViewModel.listMember.observe(this, Observer {
            if (it == null || it.isEmpty()) {
                snack(rvGroupList, R.string.toastDataEmpty)
            } else {
                loadData(it)
            }
        })
    }

    private fun loadData(it: List<UserModel>) {
        val size = items.size
        // 如果之前有数据，则先清除数据，再加载新的数据
        if (size > 0) {
            items.clear()
            adapter.notifyItemRangeRemoved(0, size)
        }
        items.addAll(it)
        adapter.notifyItemRangeInserted(0, items.size)
        rvGroupList.scheduleLayoutAnimation()
    }
}
