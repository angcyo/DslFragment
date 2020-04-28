package com.angcyo.fragment.demo

import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.base.dslFHelper

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class MainActivity : BaseAppCompatActivity() {

    override fun onCreateAfter(savedInstanceState: Bundle?) {
        super.onCreateAfter(savedInstanceState)
        dslFHelper {
            restore(MainFragment::class.java)
        }
    }
}
