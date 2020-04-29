package com.angcyo.base

import android.animation.Animator
import android.animation.AnimatorInflater
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.core.app.ActivityCompat

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**整型数中, 是否包含另一个整数*/
fun Int.have(value: Int): Boolean = if (this == 0 || value == 0) {
    false
} else if (this == 0 && value == 0) {
    true
} else {
    ((this > 0 && value > 0) || (this < 0 && value < 0)) && this and value == value
}

/**是否具有指定的权限*/
fun Context.havePermissions(vararg permissions: String): Boolean {
    return permissions.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Context.havePermission(permissionList: List<String>): Boolean {
    return permissionList.all {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Intent.FILL_IN_ACTION or
 * Intent.FILL_IN_CATEGORIES or
 * Intent.FILL_IN_CLIP_DATA or
 * Intent.FILL_IN_COMPONENT or
 * Intent.FILL_IN_DATA or
 * Intent.FILL_IN_IDENTIFIER or
 * Intent.FILL_IN_PACKAGE or
 * Intent.FILL_IN_SELECTOR or
 * Intent.FILL_IN_SOURCE_BOUNDS
 *
 * 默认填充所有类型的数据.
 *
 * 使用0, 可以只填充[mExtras]的数据
 *
 * */
fun Intent.fillFrom(other: Intent?, flag: Int = 255): Intent {
    if (other != null) {
        fillIn(other, flag)
    }
    return this
}

/**
 * 设置字段的值
 */
fun Any?.setFieldValue(clazz: Class<*>, fieldName: String, value: Any?) {
    var cls = clazz
    if (this == null || TextUtils.isEmpty(fieldName)) {
        return
    }
    var error: Exception? = null
    while (cls != Any::class.java) {
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            field[this] = value
            error = null
            break
        } catch (e: Exception) {
            error = e
        }
        cls = clazz.superclass!!
    }
    if (error != null) {
        Log.e("angcyo", "错误:" + error.message)
    }
}

/**获取应用程序正在运行的[RunningTaskInfo], 5.0之后获取不到其他应用程序的信息了*/
fun Context?.runningTasks(maxNum: Int = Int.MAX_VALUE): List<ActivityManager.RunningTaskInfo> {
    val activityManager: ActivityManager? =
        this?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    val runningTaskInfoList = activityManager?.getRunningTasks(maxNum)
    return runningTaskInfoList ?: emptyList()
}

/**查询对应的信息*/
fun Intent.queryActivities(context: Context, queryFlag: Int = 0): List<ResolveInfo> {
    val packageManager = context.packageManager
    return packageManager.queryIntentActivities(this, queryFlag)
}

fun View?.isVisible() = this?.visibility == View.VISIBLE

/**如果为空, 则执行[action].
 * 原样返回*/
fun <T> T?.elseNull(action: () -> Unit = {}): T? {
    if (this == null) {
        action()
    }
    return this
}

/**从指定资源id中, 加载动画[Animation]*/
fun animationOf(context: Context, @AnimRes id: Int): Animation? {
    try {
        if (id == 0 || id == -1) {
            return null
        }
        return AnimationUtils.loadAnimation(context, id)
    } catch (e: Exception) {
        //L.w(e)
        return null
    }
}

/**从指定资源id中, 加载动画[Animator]*/
fun animatorOf(context: Context, @AnimatorRes id: Int): Animator? {
    try {
        if (id == 0 || id == -1) {
            return null
        }
        return AnimatorInflater.loadAnimator(context, id)
    } catch (e: Exception) {
        //L.w(e)
        return null
    }
}

fun Context.getScreenWidth() = resources.displayMetrics.widthPixels

/**排除了显示的状态栏高度和导航栏高度*/
fun Context.getScreenHeight() = resources.displayMetrics.heightPixels