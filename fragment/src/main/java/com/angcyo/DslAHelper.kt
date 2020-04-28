package com.angcyo

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.transition.*
import android.util.Log
import android.view.View
import android.view.Window
import androidx.annotation.AnimRes
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.angcyo.DslAHelper.Companion.DEFAULT_REMOVE_ENTER_ANIM
import com.angcyo.DslAHelper.Companion.DEFAULT_REMOVE_EXIT_ANIM
import com.angcyo.DslAHelper.Companion.DEFAULT_SHOW_ENTER_ANIM
import com.angcyo.DslAHelper.Companion.DEFAULT_SHOW_EXIT_ANIM
import com.angcyo.activity.FragmentWrapActivity
import com.angcyo.activity.JumpActivity
import com.angcyo.base.*
import com.angcyo.fragment.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslAHelper(val context: Context) {

    companion object {
        /**程序主界面, 用于当非主界面支持back时, 返回到主界面. 且当前的任务根[Activity]不是[mainActivityClass]*/
        var mainActivityClass: Class<out Activity>? = null

        /**当前对象是否是设置的[mainActivityClass]类*/
        fun isMainActivity(any: Any?): Boolean {
            if (mainActivityClass == null || any == null) {
                return false
            }
            if (any is CharSequence) {
                if (any.isEmpty()) {
                    return false
                }
                val mainClassName: String? = mainActivityClass?.name
                return any.toString() == mainClassName
            }
            return any.javaClass == mainActivityClass
        }

        /**[androidx.fragment.app.FragmentTransaction.setCustomAnimations(int, int, int, int)]*/

        @AnimRes
        var DEFAULT_SHOW_ENTER_ANIM = R.anim.lib_translate_x_show_enter

        @AnimRes
        var DEFAULT_SHOW_EXIT_ANIM = R.anim.lib_translate_x_show_exit

        @AnimRes
        var DEFAULT_REMOVE_ENTER_ANIM = R.anim.lib_translate_x_remove_enter

        @AnimRes
        var DEFAULT_REMOVE_EXIT_ANIM = R.anim.lib_translate_x_remove_exit
    }

    /**需要启动的[Intent]*/
    val startIntentConfig = mutableListOf<IntentConfig>()

    /**关闭自身*/
    var finishSelf: Boolean = false

    /**是否使用[Finish]方法关闭[Activity], 默认使用[onBackPressed]*/
    var finishWithFinish: Boolean = false

    /**当前[Activity]finish时, 启动此[Activity]*/
    var finishToActivity: Class<out Activity>? = mainActivityClass

    /**执行操作之前, 需要保证的权限. 无权限, 则取消操作*/
    val permissions = mutableListOf<String>()

    //<editor-fold desc="start操作">

    fun start(intent: Intent, action: IntentConfig.() -> Unit = {}) {
        val config = IntentConfig(intent)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        config.action()
        if (context is Activity) {
            config.configWindow(context.window)
        }

        //跳板Activity
        if (config.useJumpActivity) {
            val jumpIntent = Intent(config.intent)
            jumpIntent.component = ComponentName(context, JumpActivity::class.java)
            jumpIntent.setTargetIntent(intent)
            jumpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            jumpIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            config.intent = jumpIntent
        }

        if (config.forceSingleTask) {
            config.intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            config.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        startIntentConfig.add(config)
    }

    fun start(aClass: Class<out Activity>, action: IntentConfig.() -> Unit = {}) {
        val intent = Intent(context, aClass)
        start(intent, action)
    }

    /**启动一个包, 通常用来启动第三方APP
     * [className] 可以指定启动的组件名. 不指定, 则启动main*/
    fun start(
        packageName: String?,
        className: String? = null,
        action: IntentConfig.() -> Unit = {}
    ): Intent? {
        if (packageName.isNullOrBlank()) {
            Log.w(DslAHelper::class.java.simpleName, "packageName is null!")
            return null
        }
        val intent = if (className.isNullOrEmpty()) {
            context.packageManager.getLaunchIntentForPackage(packageName)
        } else {
            Intent().run {
                setClassName(packageName, className)
                if (queryActivities(context).isEmpty()) {
                    null
                } else {
                    this
                }
            }
        }

//        Intent(Intent.ACTION_MAIN)
//        intent.addCategory(Intent.CATEGORY_LAUNCHER)
//        intent.setPackage(packageName)

        if (intent == null) {
            Log.w(DslAHelper::class.java.simpleName, "packageName launch intent is null!")
            return intent
        }
        start(intent, action)
        return intent
    }

    /**使用[FragmentWrapActivity]包裹启动[Fragment]*/
    fun start(
        fragment: Class<out Fragment>,
        singTask: Boolean = false,
        wrapActivity: Class<out Activity> = FragmentWrapActivity::class.java,
        action: IntentConfig .() -> Unit = {}
    ) {
        start(FragmentWrapActivity.getIntent(context, fragment, singTask, wrapActivity), action)
    }

    //</editor-fold desc="start操作">

    /**检查权限*/
    fun _checkPermissions(onPermissionsGranted: () -> Unit) {
        if (permissions.isEmpty() || context.havePermission(permissions)) {
            onPermissionsGranted()
        } else {
            if (context is FragmentActivity) {
                dslBridge(context.supportFragmentManager) {
                    startRequestPermissions(permissions.toTypedArray()) { _, _ ->
                        if (context?.havePermission(permissions) == true) {
                            onPermissionsGranted()
                        }
                    }
                }
            } else {
                context.requestPermissions(permissions) {
                    //no op
                }
            }
        }
    }

    fun doIt() {
        _checkPermissions {
            _doIt()
        }
    }

    private fun _doIt() {
        startIntentConfig.forEach {
            try {

                //op
                if (context !is Activity) {
                    it.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                //共享元素配置
                var transitionOptions: Bundle? = null
                if (enableWindowTransition || it.sharedElementList.isNotEmpty()) {
                    _supportTransition {
                        transitionOptions = if (it.sharedElementList.isNotEmpty()) {
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (context as Activity),
                                *it.sharedElementList.toTypedArray()
                            ).toBundle()
                        } else {
                            ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity)
                                .toBundle()
                        }

                        //https://developer.android.com/training/transitions/start-activity#custom-trans
                        context.window.apply {
                            //requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                            //requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                        }
                    }
                }

                var noResult = true
                if (context is Activity) {
                    //ForResult
                    if (it.onActivityResult != null && context is FragmentActivity) {
                        //FragmentBridge
                        dslBridge(context.supportFragmentManager) {
                            it.requestCode = if (it.requestCode == -1) {
                                FragmentBridge.generateCode()
                            } else it.requestCode

                            startActivityForResult(
                                it.intent,
                                it.requestCode,
                                transitionOptions,
                                it.onActivityResult!!
                            )
                        }
                        noResult = false
                    } else if (it.requestCode != -1) {
                        //default result
                        ActivityCompat.startActivityForResult(
                            context,
                            it.intent,
                            it.requestCode,
                            transitionOptions
                        )
                        noResult = false
                    }
                }

                if (noResult) {
                    //取消ForResult
                    ActivityCompat.startActivity(
                        context,
                        it.intent,
                        transitionOptions
                    )
                }

                //anim
                if (context is Activity) {
                    if (it.enterAnim != -1 || it.exitAnim != -1) {
                        context.overridePendingTransition(it.enterAnim, it.exitAnim)
                    }
                }
            } catch (e: Exception) {
                Log.e(DslAHelper::class.java.simpleName, "启动Activity失败:$e")
            }
        }

        if (finishSelf && context is Activity) {
            if (finishWithFinish) {
                context.finish()
            } else {
                context.onBackPressed()
            }
        }

    }

    //<editor-fold desc="finish 操作">

    /**关闭当前[context]*/
    fun finish(withBackPress: Boolean = false, action: IntentConfig.() -> Unit = {}) {
        if (context is Activity) {
            val activity = context
            val config = IntentConfig(Intent())
            //config.exitAnim()
            config.action()

            activity.setResult(config.resultCode, config.resultData)
            config.configWindow(activity.window)

            if (withBackPress) {
                activity.onBackPressed()
            } else {
                activity.finish()
            }

            onFinish()

            if (config.enterAnim != -1 || config.exitAnim != -1) {
                activity.overridePendingTransition(config.enterAnim, config.exitAnim)
            }
        } else {
            Log.e(DslAHelper::class.java.simpleName, "context 必须是 Activity, 才能执行 finish()")
        }
    }

    /**界面关闭后, 检查是否需要启动主界面*/
    fun onFinish() {
        if (context is Activity) {
            val activity = context
            finishToActivity?.also { cls ->
                if (activity::class.java != cls) {
                    if (activity.baseActivityIsMainActivity()) {
                        Log.w(
                            DslAHelper::class.java.simpleName,
                            "baseActivity is ${mainActivityClass?.name}, pass to start [finishToActivity]!"
                        )
                        return
                    }

                    //启动主界面
                    activity.dslAHelper {
                        start(cls)
                    }
                }
            }
        }
    }

    //</editor-fold desc="finish 操作">

    //<editor-fold desc="转场动画配置">

    //https://developer.android.com/training/transitions/start-activity#custom-trans

    /**
     * 转场动画支持.
     * 步骤1: 获取共享元素属性值
     * 步骤2: 传递属性
     * 步骤3: 播放动画
     */

    //是否支持转场动画
    fun _supportTransition(action: () -> Unit) {
        if (context is Activity &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        ) {
            action()
        }
    }

    /**默认的共享元素转场动画*/
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun _defaultElementTransition(): TransitionSet {
        val transitionSet = TransitionSet()
        transitionSet.addTransition(ChangeBounds())
        _supportTransition {
            transitionSet.addTransition(ChangeTransform())
            transitionSet.addTransition(ChangeClipBounds())
            transitionSet.addTransition(ChangeImageTransform())
            //transitionSet.addTransition(ChangeScroll()) //图片过渡效果, 请勿设置此项
        }
        return transitionSet
    }

    /**排除目标*/
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun _excludeTarget(transition: Transition, config: IntentConfig, exclude: Boolean = true) {
        config.sharedElementList.forEach {
            transition.excludeTarget(it.first, exclude)
        }
    }

    fun _excludeDecor(transition: Transition, exclude: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transition.excludeTarget(android.R.id.navigationBarBackground, exclude)
            transition.excludeTarget(android.R.id.statusBarBackground, exclude)
        }
    }

    /**添加目标*/
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun _addTarget(transition: Transition, config: IntentConfig) {
        config.sharedElementList.forEach {
            transition.addTarget(it.first)
        }
    }

    //</editor-fold desc="转场动画配置">

    //<editor-fold desc="启动转场动画配置">

    /**开启转场动画*/
    var enableWindowTransition: Boolean = false

    //可以单独设置窗口的过渡动画(非共享元素的动画), 而无需设置共享元素
    var windowExitTransition: Transition? = null
        set(value) {
            field = value
            enableWindowTransition = true
        }
    var windowEnterTransition: Transition? = null
        set(value) {
            field = value
            enableWindowTransition = true
        }

    var elementEnterTransition: Transition? = null
        set(value) {
            field = value
            enableWindowTransition = true
        }
    var elementExitTransition: Transition? = null
        set(value) {
            field = value
            enableWindowTransition = true
        }

    /**启动[Activity]后, 调用此方法开始转场动画, 必须在[Activity]的[onCreate]中调用*/
    fun transition(action: IntentConfig.() -> Unit = {}) {

        _supportTransition {
            if (context is Activity) {
                context.window.apply {
                    //requestFeature() must be called before adding content
                    //requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                    //requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

                    //https://www.jianshu.com/p/4d23b8a37a5d
                    //setExitTransition()     //当A start B时，使A中的View退出场景的transition
                    //setEnterTransition()   //当A start B时，使B中的View进入场景的transition
                    //setReturnTransition()  //当B 返回 A时，使B中的View退出场景的transition
                    //setReenterTransition() //当B 返回 A时，使A中的View进入场景的transition

                    val revertWindowTransitionListener =
                        RevertWindowTransitionListener(this, this.decorView.background)

                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    windowEnterTransition?.run {
                        enterTransition = this
                        reenterTransition = this
                        //addListener(revertWindowTransitionListener)
                    }

                    windowExitTransition?.run {
                        exitTransition = this
                        returnTransition = this
                        //addListener(revertWindowTransitionListener)
                    }

                    elementEnterTransition?.run {
                        sharedElementEnterTransition = this
                        sharedElementReenterTransition = this
                        addListener(revertWindowTransitionListener)
                    }

                    elementExitTransition?.run {
                        sharedElementExitTransition = this
                        sharedElementReturnTransition = this
                        addListener(revertWindowTransitionListener)
                    }

                    sharedElementsUseOverlay = true

                    //allowEnterTransitionOverlap = true
                    //allowReturnTransitionOverlap = true
                }
            }
        }

        val config = IntentConfig(Intent())
        config.action()

        if (context is Activity) {
            config.configWindow(context.window)

            _supportTransition {
                if (config.excludeDecor) {
                    windowEnterTransition?.run { _excludeDecor(this) }
                    windowExitTransition?.run { _excludeDecor(this) }
                }

//                if (config.excludeTarget) {
//                    elementEnterTransition?.run { _addTarget(this, config) }
//                    elementExitTransition?.run { _addTarget(this, config) }
//                }
            }
        }

        for (pair in config.sharedElementList) {
            pair.first?.run { ViewCompat.setTransitionName(this, pair.second) }
        }
    }

    //</editor-fold desc="启动转场动画配置">
}

data class IntentConfig(
    var intent: Intent,

    //-1 默认动画, 0无动画
    var enterAnim: Int = -1,
    var exitAnim: Int = -1,

    //ForResult
    var requestCode: Int = -1,

    //Result 只在 finish 操作有效
    var resultCode: Int = Activity.RESULT_CANCELED,
    var resultData: Intent? = null,

    //转场动画

    //window过得动画, 不需要包括状态栏和导航栏
    var excludeDecor: Boolean = true,

    //共享元素
    val sharedElementList: MutableList<Pair<View, String>> = mutableListOf(),

    //Window配置
    var configWindow: (Window) -> Unit = {},

    //强制使用Single_Task启动
    var forceSingleTask: Boolean = false,

    /**是否使用跳板[JumpActivity]*/
    var useJumpActivity: Boolean = false,

    //指定使用[FragmentBridge]启动[Activity], 不受[requestCode]的影响
    var onActivityResult: (ActivityResult)? = null
)

/**去掉系统默认的动画*/
fun IntentConfig.noAnim() {
    enterAnim = 0
    exitAnim = 0
}

/**重置为系统默认的动画*/
fun IntentConfig.resetAnim() {
    enterAnim = -1
    exitAnim = -1
}

fun IntentConfig.enterAnim(
    enter: Int = DEFAULT_SHOW_ENTER_ANIM,
    exit: Int = DEFAULT_SHOW_EXIT_ANIM
) {
    enterAnim = enter
    exitAnim = exit
}

fun IntentConfig.exitAnim(
    exit: Int = DEFAULT_REMOVE_EXIT_ANIM,
    enter: Int = DEFAULT_REMOVE_ENTER_ANIM
) {
    enterAnim = enter
    exitAnim = exit
}

/**设置共享元素[View], 和对应的[Key]*/
fun IntentConfig.transition(sharedElement: View?, sharedElementName: String? = null) {
    sharedElement?.run {
        sharedElementList.add(
            Pair(
                this,
                sharedElementName
                    ?: ViewCompat.getTransitionName(this)
                    ?: this.javaClass.name
            )
        )
    }
}

/**是否在主界面所在的task中*/
fun Context?.baseActivityIsMainActivity(): Boolean = runningTasks(1).firstOrNull()?.run {
    DslAHelper.isMainActivity(baseActivity?.className)
} ?: false
