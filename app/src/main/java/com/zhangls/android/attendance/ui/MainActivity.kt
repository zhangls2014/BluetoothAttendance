package com.zhangls.android.attendance.ui

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Base64
import android.view.View
import android.view.animation.AnimationUtils
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle2.LifecycleProvider
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.db.entity.GroupModel
import com.zhangls.android.attendance.type.GroupViewBinder
import com.zhangls.android.attendance.util.SharedPreferencesKey
import com.zhangls.android.attendance.util.snack
import com.zhangls.android.attendance.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import org.jetbrains.anko.*


/**
 * 主页
 *
 * @author zhangls
 */
class MainActivity : AppCompatActivity() {

    private val provider: LifecycleProvider<Lifecycle.Event> = AndroidLifecycle.createLifecycleProvider(this)
    private lateinit var mainViewModel: MainViewModel
    private val items = Items()
    private val adapter = MultiTypeAdapter(items)


    companion object {

        /**
         * Activity 入口方法
         */
        fun activityStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        // 设置标题
        val preferences = getSharedPreferences(
                SharedPreferencesKey.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE)
        val username = preferences.getString(SharedPreferencesKey.USERNAME, "")
        if (username.isNotEmpty()) {
            title = String(Base64.decode(username, Base64.DEFAULT))
        }
        // 添加分组按钮
        fabAdd.setOnClickListener { it ->
            alert(R.string.addGroupContent) {
                titleResource = R.string.addGroupTitle
                isCancelable = false

                customView {
                    linearLayout {
                        val group = editText {
                            hint = getString(R.string.hintGroupName)
                            padding = dip(8)
                        }.lparams(width = matchParent) {
                            marginStart = dip(24)
                            marginEnd = dip(24)
                        }

                        yesButton {
                            if (group.text.toString().isEmpty()) {
                                snack(fabAdd, R.string.toastGroupNameEmpty)
                            } else {
                                mainViewModel.addGroup(group.text.toString())
                            }
                        }
                    }
                }

                cancelButton {  }
            }.show()
        }

        // 设置下拉刷新组件的颜色
        srlRefresh.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPink,
                R.color.colorPurple,
                R.color.colorLime,
                R.color.colorOrange)
        srlRefresh.setOnRefreshListener {
            doAsync {
                val allGroup = mainViewModel.database.groupDao().getAllGroup()
                loadData(allGroup)
                srlRefresh.isRefreshing = false
            }
        }

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mainViewModel.setupProvider(provider)
        mainViewModel.initDatabase(this)

        observeData()

        // 配置 RecyclerView
        adapter.register(GroupModel::class.java, GroupViewBinder())
        rvGroupList.layoutManager = LinearLayoutManager(this)
        rvGroupList.adapter = adapter
        rvGroupList.layoutAnimation =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_anim_full_down)

        // 显示进度条，获取分组信息
        mainProgress.show()
        mainViewModel.groupListRequest(this)
    }

    /**
     * 实现 MainViewModel 变量监听
     */
    private fun observeData() {
        // 消息显示监听
        mainViewModel.getToastString().observe(this, Observer {
            snack(rvGroupList, it!!)
        })
        // 获取分组信息监听
        mainViewModel.groupStatus.observe(this, Observer {
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
        mainViewModel.database.groupDao().getAllGroupData().observe(this, Observer {
            loadData(it)
        })
    }

    /**
     * 加载数据
     */
    private fun loadData(it: List<GroupModel>?) {
        if (it == null || it.isEmpty()) {
            snack(rvGroupList, R.string.toastDataEmpty)
        } else {
            val size = items.size
            // 如果之前有数据，则先清除数据，再加载新的数据
            if (size > 0) {
                items.clear()
                adapter.notifyItemRangeRemoved(0, size)
            }
            items.addAll(it)
            adapter.notifyItemRangeInserted(0, items.size)
            rvGroupList.scheduleLayoutAnimation()

            it.forEach {
                if (it.status && !it.upload) {
                    mainViewModel.updateGroup(this@MainActivity, it.groupId)
                }
            }
        }
    }
}