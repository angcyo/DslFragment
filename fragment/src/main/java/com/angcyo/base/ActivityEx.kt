package com.angcyo.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.angcyo.DslAHelper
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.fragment.FragmentBridge

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**激活布局全屏*/
fun Activity.enableLayoutFullScreen(enable: Boolean = true) {
    window.enableLayoutFullScreen(enable)
}

fun Window.enableLayoutFullScreen(enable: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //去掉半透明状态栏
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //去掉半透明导航栏
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val decorView = decorView
        var systemUiVisibility = decorView.systemUiVisibility
        if (enable) { //https://blog.csdn.net/xiaonaihe/article/details/54929504
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE /*沉浸式, 用户显示状态, 不会清楚原来的状态*/
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        } else {
            systemUiVisibility =
                systemUiVisibility.remove(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            decorView.systemUiVisibility = systemUiVisibility
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //https://www.jianshu.com/p/2f7ac7a05c30
            //支持刘海屏
            //https://developer.android.google.cn/guide/topics/display-cutout

            //val windowInsets = getDecorView().rootView.rootWindowInsets

            val lp: WindowManager.LayoutParams = attributes
            if (enable) {
                //https://developer.android.google.cn/guide/topics/display-cutout#choose_how_your_app_handles_cutout_areas
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            } else {
                lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            }
            attributes = lp
        }
    }
}

/** 是否是白色状态栏. 如果是, 那么系统的状态栏字体会是灰色 */
fun Activity.lightStatusBar(light: Boolean = true) {
    window.lightStatusBar(light)
}

fun Window.lightStatusBar(light: Boolean = true) {
    //android 6
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val decorView = decorView
        val systemUiVisibility = decorView.systemUiVisibility
        if (light) {
            if (systemUiVisibility.have(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                return
            }
            decorView.systemUiVisibility =
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            if (!systemUiVisibility.have(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)) {
                return
            }
            decorView.systemUiVisibility =
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}

fun Activity.moveToBack(nonRoot: Boolean = true): Boolean {
    return moveTaskToBack(nonRoot)
}

/**拦截TouchEvent*/
fun Context.interceptTouchEvent(intercept: Boolean = true) {
    if (this is BaseAppCompatActivity) {
        this.interceptTouchEvent = intercept
    }
}

/**设置状态栏颜色*/
fun Activity.setStatusBarColor(color: Int = Color.TRANSPARENT) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.statusBarColor = color
    }
}

/**设置导航栏颜色*/
fun Activity.setNavigationBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.navigationBarColor = color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = color
        }
    }
}

/**半透明/全透明 状态栏
 * https://www.jianshu.com/p/add47d6bde29*/
fun Activity.translucentStatusBar(full: Boolean = false) {
    window.translucentStatusBar(full)
}

fun Window.translucentStatusBar(full: Boolean = false) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }
    if (full) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            statusBarColor = Color.TRANSPARENT//全透明
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }
}

/**半透明/全透明 导航栏
 * https://www.jianshu.com/p/add47d6bde29*/
fun Activity.translucentNavigationBar(full: Boolean = false) {
    window.translucentNavigationBar(full)
}

fun Window.translucentNavigationBar(full: Boolean = false) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }
    if (full) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            navigationBarColor = Color.TRANSPARENT//全透明
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }
}

private fun Int.remove(value: Int): Int = this and value.inv()

/**开始[Window]转场动画, 请调用[transition]*/
fun Context.dslAHelper(action: DslAHelper.() -> Unit) {
    DslAHelper(this).apply {
        this.action()
        doIt()
    }
}

/**返回true, 表示可以关闭界面*/
fun ComponentActivity.checkBackPressedDispatcher(): Boolean {
    if (onBackPressedDispatcher.hasEnabledCallbacks()) {
        onBackPressedDispatcher.onBackPressed()
        return false
    }
    return true
}

//<editor-fold desc="权限相关">

fun Activity.requestPermission(
    permissions: List<String>,
    requestCode: Int = FragmentBridge.generateCode()
) {
    requestPermission(permissions.toTypedArray(), requestCode)
}

fun Activity.requestPermission(
    permissions: Array<out String>,
    requestCode: Int = FragmentBridge.generateCode()
) {
    ActivityCompat.requestPermissions(this, permissions, requestCode)
}

fun FragmentActivity.checkAndRequestPermission(
    permissions: List<String>,
    onPermissionGranted: () -> Unit = {}
) {
    checkAndRequestPermission(permissions.toTypedArray(), onPermissionGranted)
}

/**检查或者请求权限*/
fun FragmentActivity.checkAndRequestPermission(
    permissions: Array<out String>,
    onPermissionGranted: () -> Unit = {}
) {
    if (havePermissions(*permissions)) {
        onPermissionGranted()
    } else {
        //请求权限
        FragmentBridge.install(supportFragmentManager).run {
            startRequestPermissions(permissions) { permissions, _ ->
                if (havePermissions(*permissions)) {
                    onPermissionGranted()
                }
            }
        }
    }
}

//</editor-fold desc="权限相关">