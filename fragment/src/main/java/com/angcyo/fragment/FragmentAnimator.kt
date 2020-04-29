package com.angcyo.fragment

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import com.angcyo.base.animatorOf
import com.angcyo.base.getScreenWidth

/**
 * [Fragment]切换动画约束, 属性动画拥有比补间动画更高效的执行性能.
 *
 * 但是属性动画又无法配置100%这样的参数.
 *
 * 所以...需要手动处理.
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object FragmentAnimator {
    var ANIM_DURATION = 300L

    var DEFAULT_SHOW_ENTER_ANIMATOR = R.anim.lib_x_show_enter_holder
    var DEFAULT_SHOW_EXIT_ANIMATOR = R.anim.lib_x_show_exit_holder

    var DEFAULT_REMOVE_ENTER_ANIMATOR = R.anim.lib_x_remove_enter_holder
    var DEFAULT_REMOVE_EXIT_ANIMATOR = R.anim.lib_x_remove_exit_holder

    fun loadAnimator(context: Context, anim: Int): Animator? {
        val sw = context.getScreenWidth().toFloat()
        val duration = ANIM_DURATION

        val objectAnimator = ObjectAnimator()
        objectAnimator.duration = duration
        objectAnimator.interpolator = AccelerateDecelerateInterpolator()

        return when (anim) {
            R.anim.lib_x_show_enter_holder -> {
                objectAnimator.setPropertyName("translationX")
                objectAnimator.setFloatValues(sw, 0f)
                objectAnimator
            }
            R.anim.lib_x_show_exit_holder -> {
                objectAnimator.setPropertyName("translationX")
                objectAnimator.setFloatValues(0f, -sw * 0.8f)
                objectAnimator.interpolator = AccelerateInterpolator()
                objectAnimator
            }
            R.anim.lib_x_remove_enter_holder -> {
                objectAnimator.setPropertyName("translationX")
                objectAnimator.setFloatValues(-sw, 0f)
                objectAnimator
            }
            R.anim.lib_x_remove_exit_holder -> {
                objectAnimator.setPropertyName("translationX")
                objectAnimator.setFloatValues(0f, sw)
                objectAnimator
            }
            else -> animatorOf(context, anim)
        }
    }
}