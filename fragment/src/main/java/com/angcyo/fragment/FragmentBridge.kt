package com.angcyo.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.angcyo.base.havePermissions
import com.angcyo.base.instantiateFragment
import com.angcyo.base.requestPermission

/**
 * [Activity] 启动 [Activity] ,[onActivityResult] [onRequestPermissionsResult] 通信桥梁.
 *
 * [Activity] 内 [Fragment] 通信过渡.
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/12
 */
class FragmentBridge : Fragment() {

    companion object {
        private const val TAG: String = "com.angcyo.fragment.FragmentBridge"

        /**构建一个16位的请求码*/
        fun generateCode(): Int = SystemClock.uptimeMillis().toInt() and 0xFFFF

        /**安装*/
        fun install(fragmentManager: FragmentManager): FragmentBridge {
            val clsName = FragmentBridge::class.java.name
            var fragmentByTag: FragmentBridge? =
                fragmentManager.findFragmentByTag(TAG) as? FragmentBridge
            if (fragmentByTag == null) {
                fragmentByTag = instantiateFragment(
                    fragmentManager.javaClass.classLoader!!,
                    clsName
                ) as FragmentBridge
            }
            if (fragmentByTag.isAdded) {

            } else {
                fragmentManager
                    .beginTransaction()
                    .add(fragmentByTag, TAG)
                    .commitAllowingStateLoss()
                //androidx.lifecycle.ReportFragment.injectIfNeededIn
                // Hopefully, we are the first to make a transaction.
                fragmentManager.executePendingTransactions()
            }
            return fragmentByTag
        }
    }

    val _observer = SparseArrayCompat<IFragmentBridge>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    /**只有在[Fragment]中调用[startActivityForResult], 才能在当前的[Fragment]收到[onActivityResult]回调*/
    fun startActivityForResult(
        intent: Intent?,
        requestCode: Int = generateCode(),
        options: Bundle? = null,
        observer: IFragmentBridge
    ) {
        if (intent == null) {
            return
        }
        _observer.put(requestCode, observer)
        startActivityForResult(intent, requestCode, options)
    }

    fun startActivityForResult(
        intent: Intent?,
        requestCode: Int = generateCode(),
        options: Bundle? = null,
        observer: ActivityResult
    ) {
        if (intent == null) {
            return
        }
        startActivityForResult(intent, requestCode, options, object : IFragmentBridge {
            override fun onActivityResult(resultCode: Int, data: Intent?) {
                super.onActivityResult(resultCode, data)
                observer(resultCode, data)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        _observer.get(requestCode)?.apply {
            _observer.put(requestCode, null)
            onActivityResult(resultCode, data)
        }
    }

    /**同上*/
    fun startRequestPermissions(
        permissions: Array<out String>,
        requestCode: Int = generateCode(),
        observer: IFragmentBridge
    ) {
        if (permissions.isEmpty()) {
            return
        }
        _observer.put(requestCode, observer)
        requestPermissions(permissions, requestCode)
    }

    fun startRequestPermissions(
        permissions: Array<out String>,
        requestCode: Int = generateCode(),
        observer: (permissions: Array<out String>, grantResults: IntArray) -> Unit
    ) {
        if (permissions.isEmpty()) {
            return
        }
        startRequestPermissions(permissions, requestCode, object : IFragmentBridge {
            override fun onRequestPermissionsResult(
                permissions: Array<out String>,
                grantResults: IntArray
            ) {
                super.onRequestPermissionsResult(permissions, grantResults)
                observer(permissions, grantResults)
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        _observer.get(requestCode)?.apply {
            _observer.put(requestCode, null)
            onRequestPermissionsResult(permissions, grantResults)
        }
    }
}

typealias ActivityResult = (resultCode: Int, data: Intent?) -> Unit

/**启动一个Activity和Fragment通信桥梁, 直接回调.
 * [onActivityResult] [onRequestPermissionsResult]
 * */
fun dslBridge(fragmentManager: FragmentManager?, action: FragmentBridge.() -> Unit) {
    if (fragmentManager == null) {
    } else {
        FragmentBridge.install(fragmentManager).apply {
            action()
        }
    }
}

/**请求权限, 并返回*/
fun Context.requestPermissions(
    permissions: List<String>,
    result: (granted: Boolean) -> Unit
) {
    requestPermissions(permissions.toTypedArray(), result)
}

fun Context.requestPermissions(
    permissions: Array<out String>,
    result: (granted: Boolean) -> Unit
) {
    if (havePermissions(*permissions)) {
        result(true)
        return
    }

    if (this is FragmentActivity) {
        dslBridge(supportFragmentManager) {
            startRequestPermissions(permissions) { permissions, grantResults ->
                result(havePermissions(*permissions))
            }
        }
    } else {
        if (this is Activity) {
            this.requestPermission(permissions)
            Log.w(
                FragmentBridge::class.java.simpleName,
                "$this is not FragmentActivity, cannot callback result!"
            )
        } else {
            Log.w(FragmentBridge::class.java.simpleName, "$this is not Activity, cancel request!")
        }
    }
}