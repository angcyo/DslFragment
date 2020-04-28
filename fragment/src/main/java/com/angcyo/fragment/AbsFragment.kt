package com.angcyo.fragment

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * Created by angcyo on 2018/12/03 23:17
 *
 * 一些生命周期日志的输出,和创建跟视图
 *
 * https://developer.android.com/reference/kotlin/androidx/fragment/app/Fragment.html?hl=zh_cn
 *
 * https://developer.android.com/guide/components/fragments?hl=zh_cn
 *
 * @author angcyo
 */
abstract class AbsFragment : Fragment() {

    //<editor-fold desc="对象变量">

    lateinit var attachContext: Context

    //</editor-fold">

    //<editor-fold desc="生命周期, 系统的方法">

    /**
     * 此方法, 通常在 hide show fragment的时候调用
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
    }

    /**
     * 此方法, 通常在 FragmentStatePagerAdapter 中调用
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
    }

    override fun getContext(): Context {
        return super.getContext() ?: attachContext
    }

    fun fContext(): Context {
        return context
    }

    override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
        return super.onGetLayoutInflater(savedInstanceState)
    }

    /**当Fragment在xml中定义时触发.*/
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
    }

    /**
     * OnAttach -> OnCreate -> onInflate -> onGetLayoutInflater ->
     * OnCreateView (initBaseView) -> onViewCreated -> OnActivityCreated ->
     * OnViewStateRestored -> OnStart -> OnResume
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = onCreateRootView(inflater, container, savedInstanceState)
        initBaseView(rootView, savedInstanceState)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * 状态恢复, 回调顺序 最优先
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    /**
     * View需要恢复状态
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }

    /**
     * OnPause -> OnStop -> OnDestroyView -> OnDestroy -> OnDetach
     */
    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onOrientationChanged(newConfig.orientation)
    }

    //</editor-fold>

    //<editor-fold desc="自定义的方法">

    open fun onOrientationChanged(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) { //切换到横屏
            onOrientationToLandscape()
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) { //切换到竖屏
            onOrientationToPortrait()
        }
    }

    open fun onOrientationToLandscape() {}

    open fun onOrientationToPortrait() {}

    /**根布局*/
    @LayoutRes
    var fragmentLayoutId = -1

    /**
     * 不指定布局Id的时候, 可以用代码创建跟视图
     */
    open fun onCreateRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layoutId = fragmentLayoutId
        val rootView: View
        rootView = if (layoutId != -1) {
            inflater.inflate(layoutId, container, false)
        } else {
            TextView(context).apply {
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                text = "重写[onCreateRootView]实现自定义布局!"
            }
        }
        return rootView
    }

    /**初始化布局, 此时的[View]还没有[attach]*/
    open fun initBaseView(rootView: View, savedInstanceState: Bundle?) {

    }

    //</editor-fold>
}