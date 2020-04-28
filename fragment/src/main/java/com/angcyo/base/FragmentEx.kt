package com.angcyo.base

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.angcyo.DslAHelper
import com.angcyo.DslFHelper
import com.angcyo.fragment.IFragment

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**当需要操作[Activity]时*/
fun Fragment.withActivity(config: Activity.() -> Unit) {
    activity?.run { config() } ?: (context as? Activity)?.run { config() }
}

/**[Fragment]中*/
fun Fragment.dslFHelper(config: DslFHelper.() -> Unit) {
    withActivity {
        if (this is FragmentActivity) {
            this.dslFHelper {
                this.config()
            }
        }
    }
}

/**开始[Window]转场动画, 请调用[transition]*/
fun Fragment.dslAHelper(action: DslAHelper.() -> Unit) {
    context?.run {
        DslAHelper(this).apply {
            this.action()
            doIt()
        }
    }
}

/**[Fragment]中的[childFragmentManager]*/
fun Fragment.dslChildFHelper(config: DslFHelper.() -> Unit) {
    childFragmentManager.dslFHelper(context, config)
}

fun Fragment.getFragmentTag(): String? {
    return if (this is IFragment) this.getFragmentTag() else this.javaClass.name
}

fun Fragment.log(builder: StringBuilder = StringBuilder()): StringBuilder {
    builder.append(Integer.toHexString(getFragmentContainerId()).toUpperCase())
    builder.append(" isAdd:")
    builder.append(if (isAdded) "√" else "×")
    builder.append(" isDetach:")
    builder.append(if (isDetached) "√" else "×")
    builder.append(" isHidden:")
    builder.append(if (isHidden) "√" else "×")
    builder.append(" isVisible:")
    builder.append(if (isVisible) "√" else "×")
    builder.append(" isResumed:")
    builder.append(if (isResumed) "√" else "×")
    builder.append(" userVisibleHint:")
    builder.append(if (userVisibleHint) "√" else "×")

    val view = view
    if (view != null) {
        builder.append(" visible:")

        builder.append(view.visibility.toVisibilityString())

        if (view.parent == null) {
            builder.append(" parent:×")
        } else {
            builder.append(" parent:√")
        }
    } else {
        builder.append(" view:×")
    }
    if (this is IFragment) {
        //builder.append(" 可视:")
        //builder.append(if (!(fragment as IFragment).isFragmentHide()) "√" else "×")
        builder.append(" TAG:").append(getFragmentTag())
    }
    if (view != null) {
        builder.append(" view:")
        builder.append(view)
    }
    return builder
}

/**
 * 通过反射, 获取Fragment所在视图的Id
 */
fun Fragment.getFragmentContainerId(): Int {
    var viewId = -1
    val fragmentView = view
    if (fragmentView == null) {
    } else if (fragmentView.parent is View) {
        viewId = (fragmentView.parent as View).id
    }
    if (viewId == View.NO_ID) {
        try {
            val field =
                Fragment::class.java.getDeclaredField("mContainerId")
            field.isAccessible = true
            viewId = field[this] as Int
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
    return viewId
}

/**返回当前的[Fragment]*/
fun Fragment.back() {
    activity?.onBackPressed() ?: Log.w("angcyo", "activity is null.")
}