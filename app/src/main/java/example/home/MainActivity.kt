package example.home

import android.Manifest
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Display
import com.style.data.app.AppActivityManager

import com.style.toast.ToastManager
import com.style.base.BaseActivity
import com.style.framework.R
import com.style.framework.databinding.ActivityMainBinding
import com.style.utils.DeviceInfoUtil
import com.style.utils.NetWorkUtil
import example.home.contact.HomeListFragment
import kotlinx.android.synthetic.main.activity_main.*

import org.simple.eventbus.EventBus
import kotlin.system.exitProcess


class MainActivity : BaseActivity() {
    private lateinit var mViewModel: MainViewModel
    private lateinit var bd: ActivityMainBinding

    companion object {
        const val REQUEST_ENABLE_BT = 6
        const val NET_CHANGE = "net_change"

        private val fragTags = arrayOf("tag1", "tag20", "tag2", "tag3", "tag4", "tag5")
        private val titles = arrayOf("View相关", "手势", "列表", "原生相关", "其他框架", "客户")
    }

    private lateinit var homeFragment1: CustomViewFragment
    private lateinit var homeFragment20: GestureFragment
    private lateinit var homeFragment2: HomeListFragment
    private lateinit var homeFragment3: OriginalAPIFragment
    private lateinit var homeFragment4: OtherFrameworkFragment
    private lateinit var fm: FragmentManager
    private lateinit var bt: FragmentTransaction
    //private lateinit var mTabs: Array<TextView>
    private var currentTabIndex = 0
    lateinit var fragments: Array<Fragment>
    private var appStateReceiver: DeviceStateBroadcastReceiver? = null
    //默认屏幕处于解锁状态
    private var isLocked = false

    /**
     * 解决方案为以下两种：
     * 方法1：在fragmentActivity里oncreate方法判断savedInstanceState==null才生成新Fragment，否则不做处理。
     * 方法2：在fragmentActivity里重写onSaveInstanceState方法，但不做实现，也就是将super.onSaveInstanceState(outState)注释掉。
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        //super.onSaveInstanceState(outState);//将super调用取消即可，表明当意外(比如系统内存吃紧将应用杀死)发生我不需要保存Fragment状态和数据等,当activity销毁时不保存其内部的view的状态
    }

    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        setContentView(R.layout.activity_main)
        setFullScreenStableDarkMode(false)

        initData()
    }

    private var isRegisterBroadcastReceiver: Boolean = false

    private fun initData() {
        AppActivityManager.getInstance().setMainTaskId(taskId)
        bd = getBinding()
        mViewModel = getViewModel(MainViewModel::class.java)
        appStateReceiver = DeviceStateBroadcastReceiver()
        val filter = IntentFilter(NET_CHANGE)
        filter.addAction(Intent.ACTION_SCREEN_ON)//屏幕点亮
        filter.addAction(Intent.ACTION_SCREEN_OFF)//屏幕关闭,会有延迟，如果刚好这个时候启动activity也会造成崩溃
        filter.addAction(Intent.ACTION_USER_PRESENT)//用户解锁
        registerReceiver(appStateReceiver, filter)
        isRegisterBroadcastReceiver = true
        EventBus.getDefault().register(this)
        setToolbarTitle(titles[0])
        /*Intent i = new Intent(this, MQTTService.class);
        i.setAction(MQTTService.ACTION_LOGIN);
        ComponentName componentName0 = startService(i);
        componentName0.getClassName();*/
        //HotFixManager.getInstance().query()
        val permissions = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)

        if (ContextCompat.checkSelfPermission(this.application, permissions[0]) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this.application, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
            logE(TAG, "没有权限")
            /*if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[1])) {
                logE(TAG, "上次拒绝")
                ActivityCompat.requestPermissions(this, permissions, REQUEST_ENABLE_BT)
            } else {
                logE(TAG, "请求权限")
                ActivityCompat.requestPermissions(this, permissions, REQUEST_ENABLE_BT)
            }
        } else {
            startBleService()
        }

        //mTabs = arrayOf(bd.viewHomeTap1, bd.viewHomeTap20, bd.viewHomeTap2, bd.viewHomeTap3, bd.viewHomeTap4)
        homeFragment1 = CustomViewFragment()
        homeFragment20 = GestureFragment()
        homeFragment2 = HomeListFragment()
        homeFragment3 = OriginalAPIFragment()
        homeFragment4 = OtherFrameworkFragment()

        fragments = arrayOf(homeFragment1, homeFragment20, homeFragment2, homeFragment3, homeFragment4)
        fm = supportFragmentManager
        bt = fm.beginTransaction()
        for (i in fragments.indices) {
            bt.add(R.id.content, fragments[i], fragTags[i]).hide(fragments[i])
        }
        bt.commit()
        changePage()
        bottom_tap_holder.setOnClickChildListener { position ->
            onBottomTapChecked(position)
        }
        /*int num = fragments.length;
        pgAdapyer = new MyAdapter(getSupportFragmentManager());
        pager.setAdapter(pgAdapyer);
        pager.setOffscreenPageLimit(num);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentTabIndex = position;
                showSelectedTab();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/

    }

    private fun onBottomTapChecked(position: Int) {
        if (position == currentTabIndex)
            return
        currentTabIndex = position
        changePage()
    }

    private fun startBleService() {
        //BleManager.getInstance().init(this);
    }

    private fun setToolbarTitle(s: String) {
        bd.layoutTitleBar.tvTitle.text = s
    }

    private fun changePage() {
        bottom_tap_holder.changeSelected(currentTabIndex)
        setToolbarTitle(titles[currentTabIndex])

        bt = fm.beginTransaction()
        for (i in fragments.indices) {
            if (currentTabIndex != i) {
                bt.hide(fragments[i])
                //mTabs[i].isSelected = false
            } else {
                bt.show(fragments[i])
                // 把当前tab设为选中状态
                //mTabs[i].isSelected = true
            }
        }
        bt.commitAllowingStateLoss()

    }

    /*fun onTabClicked(view: View) {
        var index = 0
        when (view.id) {
            R.id.layout_home_tap_1 -> index = 0
            R.id.layout_home_tap_20 -> index = 1
            R.id.layout_home_tap_2 -> index = 2
            R.id.layout_home_tap_3 -> index = 3
            R.id.layout_home_tap_4 -> index = 4
        }
        currentTabIndex = index
        showSelectedTab()
    }*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                logE(TAG, "权限允许")
                startBleService()
            } else {
                logE(TAG, "权限拒绝")
                // Permission Denied
                showToast("权限拒绝")
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        //updateUnreadMsg();
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val a = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mList = a.runningAppProcesses
        mList.forEach {
            if (it.pid != Process.myPid()) {
                Process.killProcess(it.pid)
            }
        }
        Process.killProcess(Process.myPid())
    }

    override fun onDestroy() {
        //取消事件注册
        EventBus.getDefault().unregister(this)
        //BleManager.getInstance().close();
        if (isRegisterBroadcastReceiver && appStateReceiver != null) {
            unregisterReceiver(appStateReceiver)
            isRegisterBroadcastReceiver = false
            appStateReceiver = null
        }
        super.onDestroy()
        AppActivityManager.getInstance().setMainTaskId(-1)
    }

    inner class DeviceStateBroadcastReceiver : BroadcastReceiver() {
        val TAG = this.javaClass.simpleName

        override fun onReceive(context: Context, intent: Intent) {
            logE(TAG, intent.action!!)
            if (NET_CHANGE == intent.action) {
                if (!NetWorkUtil.isNetWorkActive(context))
                    ToastManager.showToast(context, "网络不可用")
                else
                    Log.e(TAG, "网络连接")
            } else if (Intent.ACTION_USER_PRESENT == intent.action) {
                onDisplayUnlocked()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val state = DeviceInfoUtil.getDisplay(context).state
                    logE(TAG, "display state $state")
                    if (state == Display.STATE_OFF) {
                        onDisplayOff()
                    }
                }
            }
        }

    }

    //用户解锁屏幕
    private fun onDisplayUnlocked() {
        isLocked = false
    }

    //屏幕关闭
    private fun onDisplayOff() {
        isLocked = true
    }

}
