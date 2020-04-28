package com.angcyo.fragment

import android.app.Activity
import android.view.View

/**
 * Created by angcyo on 2018/12/04 23:32
 *
 * @author angcyo
 */
interface IFragment {

    /**
     * [Activity] 的 [Activity.onBackPressed] 回调.
     *
     * @return true 允许关闭当前的Fragment
     */
    fun onBackPressed(): Boolean

    /**
     * 是否可以滑动返回
     *
     * @return true 允许
     */
    fun canSwipeBack(): Boolean

    /**是否可以Fling返回*/
    fun canFlingBack(): Boolean

    /**
     * 当手指在 touchDownView 上点击时, 是否调用隐藏键盘的方法
     */
    fun hideSoftInputOnTouchDown(touchDownView: View?): Boolean

    /**
     * @see [androidx.fragment.app.FragmentTransaction.add(androidx.fragment.app.Fragment, java.lang.String)]
     * */
    fun getFragmentTag(): String
}