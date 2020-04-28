package com.angcyo

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import com.angcyo.DslTargetIntent.Companion.KEY_TARGET_INTENT
import com.angcyo.base.dslAHelper
import com.angcyo.base.fillFrom

/**
 * 目标[Intent]跳转处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class DslTargetIntent {

    companion object {
        /**需要跳转的目标[Intent]*/
        const val KEY_TARGET_INTENT = "key_target_intent"
    }

    /**处理了目标回调*/
    var targetIntentHandle: (targetIntent: Intent) -> Unit = {}

    /**处理参数中的目标[Intent]信息*/
    fun doIt(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.i(DslTargetIntent::class.java.simpleName, "nothing to do.")
            return
        }

        val targetIntent: Intent? = intent.getParcelableExtra(KEY_TARGET_INTENT)
        targetIntent?.apply {
            //handle

            //清空Extra数据, 防止在[fillFrom]时, 死循环.
            putExtra(KEY_TARGET_INTENT, null as Parcelable?)

            //只需要填充[mExtras]即可
            fillFrom(intent, 0)

            //跳转到真正目标, 目前只支持跳转[Activity]
            context.dslAHelper {
                start(targetIntent)
            }

            targetIntentHandle(this)
        }
    }
}

/**快速处理*/
fun Context.dslTargetIntentHandle(intent: Intent?, action: DslTargetIntent.() -> Unit = {}) {
    DslTargetIntent().apply {
        action()
        doIt(this@dslTargetIntentHandle, intent)
    }
}

/**设置目标[Intent]*/
fun Intent.setTargetIntent(intent: Intent?) {
    putExtra(KEY_TARGET_INTENT, intent)
}