package com.angcyo

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import com.angcyo.base.*
import com.angcyo.fragment.*


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/22
 */
class DslFHelper(
    val fm: FragmentManager,
    val context: Context? = null,
    val debug: Boolean = BuildConfig.DEBUG
) {

    companion object {
        var fragmentManagerLog: String = ""
    }

    //<editor-fold desc="属性">

    /**这个列表中的[Fragment]将会被执行[add]操作*/
    val showFragmentList = mutableListOf<Fragment>()

    /**这个列表中的[Fragment]将会被执行[remove]操作*/
    val removeFragmentList = mutableListOf<Fragment>()

    /**[Fragment]默认的容器id*/
    @IdRes
    var containerViewId: Int = fm.getLastFragmentContainerId(R.id.fragment_container)

    /**
     * 隐藏[hide], 操作之后最后一个[Fragment]前面第几个开始的所有[Fragment]
     * >=1 才有效.
     * */
    var hideBeforeIndex = 2

    /**当显示一个已经存在的[Fragment]时, 是否要移除覆盖在此[Fragment]上的所有[Fragment]*/
    var removeOverlayFragmentOnShow = true

    /**最后一个[Fragment]在执行的[back]时, 是否需要[remove]*/
    var removeLastFragmentOnBack: Boolean = false
        set(value) {
            field = value
            if (field) {
                finishActivityOnLastFragmentRemove = false
            }
        }

    /**最后一个[Fragment]在执行的[remove]时, 是否需要关闭[activity]*/
    var finishActivityOnLastFragmentRemove: Boolean = true

    var finishToActivity: Class<out Activity>? = DslAHelper.mainActivityClass

    @AnimatorRes
    @AnimRes
    var showEnterAnimRes: Int = FragmentAnimator.DEFAULT_SHOW_ENTER_ANIMATOR

    @AnimatorRes
    @AnimRes
    var showExitAnimRes: Int = FragmentAnimator.DEFAULT_SHOW_EXIT_ANIMATOR

    @AnimatorRes
    @AnimRes
    var removeEnterAnimRes: Int = FragmentAnimator.DEFAULT_REMOVE_ENTER_ANIMATOR

    @AnimatorRes
    @AnimRes
    var removeExitAnimRes: Int = FragmentAnimator.DEFAULT_REMOVE_EXIT_ANIMATOR

    /**执行操作之前, 需要保证的权限. 无权限, 则取消操作*/
    val permissions = mutableListOf<String>()

    //</editor-fold desc="属性">

    init {
        //FragmentManager.enableDebugLogging(debug)
        if (debug) {
            //fm.registerFragmentLifecycleCallbacks(LogFragmentLifecycleCallbacks(), true)
        }
    }

    /**给Fragment配置参数*/
    fun configFragment(action: Fragment.() -> Unit) {
        showFragmentList.forEach {
            it.action()
        }
    }

    //<editor-fold desc="add 或者 show操作">

    inline fun <reified F : Fragment> show(
        fClass: Class<out Fragment>,
        crossinline action: F.() -> Unit
    ) {
        instantiateFragment(fClass.classLoader!!, fClass.name)?.run {
            (this as F).action()
            show(this)
        }
    }

    fun show(vararg fClass: Class<out Fragment>, action: Fragment.() -> Unit = {}) {
        val list = mutableListOf<Fragment>()
        for (cls in fClass) {
            instantiateFragment(cls.classLoader!!, cls.name)?.run {
                action()
                list.add(this)
            }

//            fm.fragmentFactory.instantiate(
//                cls.classLoader!!,
//                cls.name
//            )
        }
        show(list)
    }

    fun show(vararg fragment: Fragment, action: Fragment.() -> Unit = {}) {
        show(fragment.toList(), action)
    }

    /**
     * 如果显示的[Fragment]已经[add], 那么此[Fragment]上面的其他[Fragment]都将被[remove]
     * 这一点, 有点类似[Activity]的[SINGLE_TASK]启动模式
     * */
    fun show(fragmentList: List<Fragment>, action: Fragment.() -> Unit = {}) {
        fragmentList.forEach {
            it.action()
            if (!showFragmentList.contains(it)) {
                showFragmentList.add(it)
                if (it.isAdded && removeOverlayFragmentOnShow) {
                    remove(fm.getOverlayFragment(it))
                }
            }
        }
    }

    /**优先使用已经存在的[Fragment]*/
    fun restore(vararg fClass: Class<out Fragment>, action: Fragment.() -> Unit = {}) {
        show(fm.restore(*fClass), action)
    }

    fun restore(vararg tag: String?, action: Fragment.() -> Unit = {}) {
        show(fm.restore(*tag), action)
    }

    fun restores(vararg fragment: Fragment, action: Fragment.() -> Unit = {}) {
        show(fm.restore(*fragment), action)
    }

    fun <T : Fragment> restore(fragment: T, action: T.() -> Unit = {}) {
        show(fm.restore(fragment).apply {
            (this.first() as T).action()
        })
    }

    //</editor-fold desc="add 或者 show操作">

    //<editor-fold desc="remove操作">

    fun remove(vararg fClass: Class<out Fragment>) {
        for (cls in fClass) {
            remove(cls.name)
        }
    }

    fun remove(vararg tag: String?) {
        for (t in tag) {
            fm.findFragmentByTag(t)?.let { remove(it) }
        }
    }

    fun remove(fragment: Fragment?) {
        if (fragment == null) {
            return
        }
        if (!removeFragmentList.contains(fragment)) {
            removeFragmentList.add(fragment)
        }
    }

    fun removes(vararg fragment: Fragment) {
        remove(fragment.toList())
    }

    fun remove(fragmentList: List<Fragment>) {
        for (f in fragmentList) {
            remove(f)
        }
    }

    /**移除最后的指定个数的[Fragment]*/
    fun removeLast(count: Int = 1) {
        val allValidityFragment = fm.getAllValidityFragment()
        val size = allValidityFragment.size
        for (i in size - count until size) {
            remove(allValidityFragment[i])
        }
    }

    /**移除所有[getView]不为空的[Fragment]*/
    fun removeAll() {
        remove(fm.getAllValidityFragment())
    }

    /**保留前几个, 移除后面全部*/
    fun keep(before: Int) {
        val allValidityFragment = fm.getAllValidityFragment()
        for (i in before until allValidityFragment.size) {
            remove(allValidityFragment[i])
        }
    }

    /**只保留需要keep的Fragment, 其余remove*/
    fun keep(vararg tags: String?) {
        val allValidityFragment = fm.getAllValidityFragment()
        val keepList = mutableListOf<Fragment>()
        for (tag in tags) {
            fm.findFragmentByTag(tag)?.let {
                keepList.add(it)
            }
        }
        for (f in allValidityFragment) {
            if (!keepList.contains(f)) {
                remove(f)
            }
        }
    }

    /**只保留需要keep的Fragment, 其余remove*/
    fun keep(vararg fClass: Class<out Fragment>) {
        val tags = mutableListOf<String?>()
        for (cls in fClass) {
            tags.add(cls.name)
        }
        keep(*tags.toTypedArray())
    }

    //</editor-fold desc="remove操作">

    //<editor-fold desc="拦截操作">

    /**自定义的配置操作, 请勿在此执行[commit]操作*/
    var onConfigTransaction: (FragmentTransaction) -> Unit = {

    }

    /**提交操作*/
    var onCommit: (FragmentTransaction, List<Fragment>) -> Unit = { ft, fl ->
        if (fm.isStateSaved) {
            if (fl.isEmpty()) {
                ft.commitNowAllowingStateLoss()
            } else {
                ft.commitAllowingStateLoss()
            }
        } else {
            if (fl.isEmpty()) {
                ft.commitNow()
            } else {
                ft.commit()
            }
        }
    }

    //</editor-fold desc="拦截操作">

    //<editor-fold desc="其他操作">

    /**去掉默认的动画*/
    fun noAnim() {
        showEnterAnimRes = 0
        showExitAnimRes = 0
        removeEnterAnimRes = 0
        removeExitAnimRes = 0
    }

    fun anim(@AnimatorRes @AnimRes enter: Int, @AnimatorRes @AnimRes exit: Int) {
        showEnterAnimRes = enter
        removeEnterAnimRes = enter

        showExitAnimRes = exit
        removeExitAnimRes = exit
    }

    /**
     * 回退操作, 请使用此方法.
     * 会进行回退检查
     *
     * @return 返回true, 表示当前Activity可以被关闭
     * */
    fun back(): Boolean {
        var result = true

        val allValidityFragment = fm.getAllValidityFragment()
        val lastFragment = allValidityFragment.lastOrNull()

        if (lastFragment != null) {
            if (lastFragment is IFragment) {
                if (!lastFragment.onBackPressed()) {
                    result = false
                }
            }

            if (result) {
                //可以remove
                if (showFragmentList.isEmpty() && allValidityFragment.size == 1) {
                    if (removeLastFragmentOnBack) {
                        result = false
                        //只有一个Fragment
                        remove(lastFragment)
                        doIt(true)
                    }
                } else {
                    result = false
                    remove(lastFragment)
                    doIt(true)
                }
            }
        }

        return result
    }

    val _handle: Handler by lazy { Handler(Looper.getMainLooper()) }

    var _logRunnable: Runnable? = null

    /**检查权限*/
    fun _checkPermissions(onPermissionsGranted: () -> Unit) {
        if (permissions.isEmpty() || context?.havePermission(permissions) == true) {
            onPermissionsGranted()
        } else {
            dslBridge(fm) {
                startRequestPermissions(permissions.toTypedArray()) { _, _ ->
                    if (context?.havePermission(permissions) == true) {
                        onPermissionsGranted()
                    }
                }
            }
        }
    }

    /**执行操作,
     * [fromBack] 是否是用户back操作触发的
     * */
    fun doIt(fromBack: Boolean = false) {
        _checkPermissions {
            _doIt(fromBack)
        }
    }

    private fun _doIt(fromBack: Boolean = false) {
        fm.beginTransaction().apply {
            if (fm.isDestroyed) {
                //no op
                Log.w(DslFHelper::class.java.simpleName, "fm is destroyed.")
                return
            }

            if (removeFragmentList.isEmpty() && showFragmentList.isEmpty()) {
                Log.w(DslFHelper::class.java.simpleName, "no op do it.")
                return
            }

            //如果需要显示的Fragment在移除列表中
            removeFragmentList.removeAll(showFragmentList)

            //一顿操作之后, 最终fm中, 应该有的Fragment列表
            val fmFragmentList = mutableListOf<Fragment>()

            val allValidityFragment = mutableListOf<Fragment>()
            val allNoViewFragment = mutableListOf<Fragment>()
            fm.fragments.forEach {
                when {
                    showFragmentList.contains(it) -> {
                        //需要显示的Fragment, 已经存在于结构中
                    }
                    it.view == null -> allNoViewFragment.add(it)
                    else -> allValidityFragment.add(it)
                }
            }

            fmFragmentList.addAll(allValidityFragment)
            fmFragmentList.addAll(showFragmentList)
            fmFragmentList.removeAll(removeFragmentList)

            //...end

            //anim 动画需要在op之前设置, 否则不会有效果
            //需要执行显示F的动画
            val showFAnim = showFragmentList.isNotEmpty() && allValidityFragment.isNotEmpty()
            //需要执行移除F的动画
            val removeFAnim = !showFAnim && removeFragmentList.isNotEmpty()
            if (showFAnim) {
                //显示F,并且非第一个Fragment
                if (showEnterAnimRes != 0) {
                    setCustomAnimations(
                        showEnterAnimRes,
                        showExitAnimRes,
                        showEnterAnimRes,
                        showExitAnimRes
                    )
                }
            } else if (removeFAnim) {
                //移除F
                if (removeExitAnimRes != 0) {
                    setCustomAnimations(
                        removeEnterAnimRes,
                        removeExitAnimRes,
                        removeEnterAnimRes,
                        removeExitAnimRes
                    )
                }
            }

            //op remove
            removeFragmentList.forEach {
                remove(it)
            }

            //op hide
            fmFragmentList.forEachIndexed { index, fragment ->
                if (fragment.isAdded) {
                    if (index < fmFragmentList.lastIndex) {
                        //除了顶上一个Fragment, 其他Fragment都要执行不可见生命周期回调
                        setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                    }

                    if (index < fmFragmentList.size - hideBeforeIndex) {
                        hide(fragment)
                    } else {
                        show(fragment)
                    }
                }
            }

            //no view ,no op
//            allNoViewFragment.forEach {
//                if (it.retainInstance) {
//                    setMaxLifecycle(it, Lifecycle.State.STARTED)
//                }
//            }

            //op show
            val lastFragment = fmFragmentList.lastOrNull()
            showFragmentList.forEach { fragment ->
                when {
                    fragment.isDetached -> attach(fragment)
                    fragment.isAdded -> {
                    }
                    else -> add(
                        containerViewId,
                        fragment,
                        fragment.getFragmentTag()
                    )
                }
                if (fragment != lastFragment) {
                    setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                }
            }

            //op last
            lastFragment?.let {
                setPrimaryNavigationFragment(it)
                setMaxLifecycle(it, Lifecycle.State.RESUMED)
            }

            //由于setCustomAnimations动画只会在add和remove F的时候才会触发.
            (when {
                showFAnim -> fmFragmentList.getOrNull(fmFragmentList.lastIndex - 1)
                removeFAnim -> lastFragment
                else -> null
            })?.run {
                //removeEnterAnimRes showExitAnimRes 动画触发
                val animRes = if (showFAnim) showExitAnimRes else removeEnterAnimRes
                if (isAdded && view != null && animRes != 0) {
                    if (view.isVisible()) {
                        view?.apply {
                            FragmentAnimator.loadAnimator(context, animRes)?.apply {
                                setTarget(view)
                                start()
                            }.elseNull {
                                animationOf(context, animRes)?.apply {
                                    startAnimation(this)
                                }
                            }
                        }
                    }
                }
            }

            onConfigTransaction(this)
            fragmentManagerLog = fm.log(false)

            if (fmFragmentList.isEmpty() &&
                finishActivityOnLastFragmentRemove &&
                context is Activity
            ) {
                val activity = context
                activity.dslAHelper {
                    finishToActivity = this@DslFHelper.finishToActivity
                    finish()
                }
            } else {
                onCommit(this, fmFragmentList)
            }

            if (debug) {
                _logRunnable?.run { _handle.removeCallbacks(this) }
                _logRunnable = object : Runnable {
                    override fun run() {
                        fm.log()
                        _handle.removeCallbacks(this)
                        _logRunnable = null
                    }
                }
                _handle.postDelayed(_logRunnable!!, 16)
            }
        }
    }

    //</editor-fold desc="其他操作">
}