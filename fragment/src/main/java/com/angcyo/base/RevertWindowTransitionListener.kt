package com.angcyo.base

import android.graphics.drawable.Drawable
import android.os.Build
import android.transition.Transition
import android.view.Window
import androidx.annotation.RequiresApi

/**
 * 恢复window背景的listener
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/19
 */

@RequiresApi(Build.VERSION_CODES.KITKAT)
class RevertWindowTransitionListener(val window: Window, val bgDrawable: Drawable?) :
    Transition.TransitionListener {
    override fun onTransitionEnd(transition: Transition?) {
        window.setBackgroundDrawable(bgDrawable)
    }

    override fun onTransitionResume(transition: Transition?) {
    }

    override fun onTransitionPause(transition: Transition?) {
    }

    override fun onTransitionCancel(transition: Transition?) {
        onTransitionEnd(transition)
    }

    override fun onTransitionStart(transition: Transition?) {
    }

}