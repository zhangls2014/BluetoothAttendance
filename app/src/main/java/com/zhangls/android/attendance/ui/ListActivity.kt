package com.zhangls.android.attendance.ui

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.zhangls.android.attendance.util.IntentUtils
import com.zhangls.android.attendance.util.UriParseUtils
import com.zhangls.android.attendance.util.snack
import com.zhangls.android.attendance.viewmodel.ListViewModel
import com.zhangls.android.attendance.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import org.jetbrains.anko.*


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
    private var groupId: Int = 0
    private var groupView: Boolean = false
    private var groupName: String = ""
    private var imageUri: Uri? = null
    /**
     * 接受蓝牙扫描到的信息
     */
    private val mCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            Log.d("onScanFailed======", "errorCode is $errorCode")
            alert(R.string.bluetoothScanFailed) {
                titleResource = R.string.attendanceWarning
                yesButton {
                    titleResource = R.string.bluetoothScanRetry
                    openBluetooth()
                }
                cancelButton {
                    finish()
                }
                isCancelable = false
            }.show()
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result != null) {
                Log.d("bluetooth======", "${ result.device.name } address is ${ result.device.address }")
                listViewModel.attendance(groupId, result.device.address.orEmpty())
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
         * 打开相机请求码
         */
        private const val REQUEST_IMAGE_CAPTURE = 2211

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

        groupName = intent.extras.getString(GROUP_NAME)
        groupId = intent.extras.getInt(GROUP_ID)
        groupView = intent.extras.getBoolean(GROUP_VIEW)
        if (groupView) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        // 设置标题
        title = groupName

        // 设置下拉刷新组件的颜色
        srlRefresh.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPink,
                R.color.colorPurple,
                R.color.colorLime,
                R.color.colorOrange)
        srlRefresh.setOnRefreshListener {
            listViewModel.getListMember(this, groupId)
        }

        listViewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        listViewModel.setupProvider(provider)
        listViewModel.initDatabase(this)

        observeData()

        // 配置 RecyclerView
        adapter.register(UserModel::class.java, UserViewBinder(groupView))
        rvGroupList.layoutManager = LinearLayoutManager(this)
        rvGroupList.adapter = adapter
        // 设置动画
        rvGroupList.layoutAnimation =
                AnimationUtils.loadLayoutAnimation(this, R.anim.layout_anim_full_down)

        fabAdd.setOnClickListener { AddActivity.activityStart(this, groupId, groupName) }

        // 显示进度条，获取考勤人员信息
        mainProgress.show()
        if (groupView) {
            listViewModel.getListMember(this, groupId)
        } else {
            openBluetooth()
            if (groupId > 3) {
                listViewModel.getListMember(this, groupId)
            } else {
                listViewModel.listMemberRequest(this, groupId)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_attendance, menu)
        if (!groupView) {
            menu!!.findItem(R.id.menu_picture).isVisible = false
        } else {
            menu!!.findItem(R.id.menu_attendance).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_attendance -> {
                listViewModel.completedAttendance(this, groupId)
                alert(R.string.groupAttendanceFinish) {
                    isCancelable = false
                    titleResource = R.string.groupAttendance
                    yesButton { getStoragePermissions() }
                }.show()
                return true
            }
            R.id.menu_picture -> {
                PictureActivity.activityStart(this, listViewModel.getFilePath(groupId))
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
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
        listViewModel.database.userDao().getGroupUserData(groupId)
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

        listViewModel.attendanceFinish.observe(this, Observer {
            if (it != null && it) {
                alert(R.string.groupAttendanceFinish) {
                    isCancelable = false
                    titleResource = R.string.groupAttendance
                    yesButton { getStoragePermissions() }
                }.show()
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
        if (mBluetoothAdapter.isEnabled) {
            getPermissions()
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
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
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if(resultCode == Activity.RESULT_OK) {
                listViewModel.addFilePath(UriParseUtils.getPathWithUri(this, imageUri!!), groupId)
                finish()
                toast(R.string.toastImageSaveSuccess)
            } else {
                toast(R.string.toastImageSaveFail)
            }
        }
    }

    /**
     * 扫描附近设备
     */
    private fun scanDevices() {
        mBluetoothAdapter.bluetoothLeScanner.startScan(mCallback)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!groupView) {
            // 在退出界面的时候关闭蓝牙扫描功能
            mBluetoothAdapter.bluetoothLeScanner.stopScan(mCallback)
        }
    }

    override fun onBackPressed() {
        // 如果是查看考勤结果时，返回键退出
        // 如果是考勤时，返回键提示结束考勤
        if (groupView) {
            super.onBackPressed()
        } else {
            alert(R.string.groupAttendanceProcessing) {
                titleResource = R.string.attendanceWarning
                yesButton {
                    titleResource = R.string.attendanceFinishAlertYesButton
                    listViewModel.completedAttendance(this@ListActivity, groupId)
                    getStoragePermissions()
                }
                cancelButton {}
            }.show()
        }
    }

    /**
     * 位置权限
     */
    private fun getPermissions() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .onGranted({
                    scanDevices()
                })
                .onDenied({ permissions ->
                    if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                        // 权限申请被拒绝时，检查，若勾选了始终拒绝权限授予，则弹出提示框
                        alert(R.string.permission_location_reason) {
                            title = getString(R.string.title_alert_permission)
                            yesButton {
                                AndPermission.with(this@ListActivity)
                                        .runtime()
                                        .setting()
                                        .onComeback {

                                        }
                                        .start()
                            }
                            noButton { finish() }
                        }.show()
                    }
                })
                .rationale({ _, _, executor ->
                    // 弹出权限申请说明提示框
                    alert(R.string.permission_location_reason) {
                        title = getString(R.string.title_alert_permission)
                        yesButton { executor.execute() }
                        noButton {
                            executor.cancel()
                            finish()
                        }
                    }.show()
                })
                .start()
    }

    /**
     * 存储空间权限
     */
    private fun getStoragePermissions() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted({
                    openCamera()
                })
                .onDenied({ permissions ->
                    if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                        // 权限申请被拒绝时，检查，若勾选了始终拒绝权限授予，则弹出提示框
                        alert(R.string.permission_storage_reason) {
                            title = getString(R.string.title_alert_permission)
                            yesButton {
                                AndPermission.with(this@ListActivity)
                                        .runtime()
                                        .setting()
                                        .onComeback {

                                        }
                                        .start()
                            }
                            noButton {}
                        }.show()
                    }
                })
                .rationale({ _, _, executor ->
                    // 弹出权限申请说明提示框
                    alert(R.string.permission_storage_reason) {
                        title = getString(R.string.title_alert_permission)
                        yesButton { executor.execute() }
                        noButton { executor.cancel() }
                    }.show()
                })
                .start()
    }

    /**
     * 打开相机
     */
    private fun openCamera() {
        imageUri = UriParseUtils.convertToFileProvider(this, UriParseUtils.getTempImageUri(this))
        startActivityForResult(IntentUtils.getCaptureIntent(imageUri!!), REQUEST_IMAGE_CAPTURE)
    }
}