package com.angcyo.fragment

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.angcyo.base.getAllResumedFragment

/**
 * Created by angcyo on 2018/12/03 23:17
 *
 *
 * 生命周期的封装, 只需要关注 [onFragmentShow] 和 [onFragmentHide]
 *
 * @author angcyo
 */
abstract class AbsLifecycleFragment : AbsFragment(), IFragment {

    /**
     * 触发 [.onFragmentShow] 的次数
     */
    var fragmentShowCount = 0

    /**[childFragmentManager]中最后一个可见性的[Fragment]*/
    val lastFragment: Fragment? get() = childFragmentManager.getAllResumedFragment().lastOrNull()

    //<editor-fold desc="属性动画的支持">

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    /**Fragment切换的时候, 尽量使用属性动画. 这样动画会在RenderThread中执行, 流畅度显著提升
     * https://developer.android.google.cn/guide/topics/graphics/prop-animation
     * https://developer.android.com/guide/topics/resources/drawable-resource
     * https://developer.android.com/guide/topics/resources/animation-resource
     * */
    override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
        return super.onCreateAnimator(transit, enter, nextAnim)
    }

    //</editor-fold desc="属性动画的支持">

    //<editor-fold desc="生命周期, 系统的方法">

    override fun onResume() {
        super.onResume()
        onFragmentShow(null)
    }

    override fun onPause() {
        super.onPause()
        onFragmentHide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentShowCount = 0
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        lastFragment?.onActivityResult(requestCode, resultCode, data)
    }

    //</editor-fold>

    //<editor-fold desc="扩展的方法">

    @CallSuper
    open fun onFragmentShow(bundle: Bundle?) {
        if (view != null) {
            if (fragmentShowCount++ == 0) {
                onFragmentFirstShow(bundle)
            } else {
                onFragmentNotFirstShow(bundle)
            }
        }
    }

    /**
     * 从 onFragmentShow 分出来的周期事件
     */
    open fun onFragmentFirstShow(bundle: Bundle?) {}

    open fun onFragmentNotFirstShow(bundle: Bundle?) {}

    open fun onFragmentHide() {
    }

    /**
     * 是否可以关闭当前[Fragment]界面.
     * 返回true, 表示可以关闭
     */
    override fun onBackPressed(): Boolean {
        return (lastFragment as? AbsLifecycleFragment)?.onBackPressed() ?: true
    }

    override fun canSwipeBack(): Boolean {
        return view != null
    }

    override fun canFlingBack(): Boolean {
        return canSwipeBack()
    }

    override fun hideSoftInputOnTouchDown(touchDownView: View?): Boolean {
        return false
    }

    override fun getFragmentTag(): String {
        return tag ?: this.javaClass.name
    }

    //</editor-fold>
}