package com.zhangls.android.attendance.ui

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle2.LifecycleProvider
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.Permission
import com.zhangls.android.attendance.R
import com.zhangls.android.attendance.db.entity.UserModel
import com.zhangls.android.attendance.type.UserViewBinder
import com.zhangls.android.attendance.util.snack
import com.zhangls.android.attendance.viewmodel.ListViewModel
import com.zhangls.android.attendance.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton


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
    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    /**
     * 创建一个广播接收器，接受蓝牙扫描到的信息
     */
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // 扫描到设备
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                Log.d("bluetooth======", "${device.name} address is ${device.address}")
                listViewModel.attendance(intent.extras.getInt(GROUP_ID), device.address.orEmpty())
            }
        }
    }


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
         * 请求码
         */
        private const val REQUEST_ENABLE_BT = 1

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
            listViewModel.getListMember(this, intent.extras.getInt(GROUP_ID))
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
            openBluetooth()
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
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!intent.extras.getBoolean(GROUP_VIEW)) {
            menuInflater.inflate(R.menu.menu_attendance, menu)
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_attendance -> {
                listViewModel.completedAttendance(this, intent.extras.getInt(GROUP_ID))
                alert(R.string.groupAttendanceFinish) {
                    isCancelable = false
                    titleResource = R.string.groupAttendance
                    yesButton { finish() }
                }.show()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 打开蓝牙
     */
    private fun openBluetooth() {
        if (mBluetoothAdapter == null) {
            // 如果该设备不支持蓝牙设备
            // 显示对话框
            alert(R.string.devices_does_not_support_bluetooth) {
                title = getString(R.string.title_alert_bluetooth_warning)
                yesButton {
                    // 退出界面
                    finish()
                }
            }.show()
            return
        }

        // 启用蓝牙
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            getPermissions()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                toast("蓝牙打开成功")
                getPermissions()
            } else {
                toast("蓝牙未开启，无法考勤")
                // 退出该界面
                finish()
            }
        }
    }

    /**
     * 扫描附近设备
     */
    private fun scanDevices() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)
        mBluetoothAdapter.startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!intent.extras.getBoolean(GROUP_VIEW)) {
            // 在退出界面的时候关闭蓝牙扫描功能
            mBluetoothAdapter.cancelDiscovery()

            unregisterReceiver(mReceiver)
        }
    }

    /**
     * 位置权限
     */
    private fun getPermissions() {
        AndPermission.with(this)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .onGranted({
                    scanDevices()
                })
                .onDenied({ permissions ->
                    if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                        // 权限申请被拒绝时，检查，若勾选了始终拒绝权限授予，则弹出提示框
                        val settingService = AndPermission.permissionSetting(this)
                        alert(R.string.permission_location_reason) {
                            title = getString(R.string.title_alert_bluetooth_permission)
                            yesButton { settingService.execute() }
                            noButton { settingService.cancel() }
                        }.show()
                    }
                })
                .rationale({ _, _, executor ->
                    // 弹出权限申请说明提示框
                    alert(R.string.permission_location_reason) {
                        title = getString(R.string.title_alert_bluetooth_permission)
                        yesButton { executor.execute() }
                        noButton { executor.cancel() }
                    }.show()
                })
                .start()
    }
}