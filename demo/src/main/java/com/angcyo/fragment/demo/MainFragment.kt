package com.angcyo.fragment.demo

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.angcyo.fragment.AbsLifecycleFragment
import com.angcyo.tablayout.delegate.ViewPager1Delegate

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class MainFragment : AbsLifecycleFragment() {
    init {
        fragmentLayoutId = R.layout.fragment_main
    }

    override fun initBaseView(rootView: View, savedInstanceState: Bundle?) {
        super.initBaseView(rootView, savedInstanceState)
        rootView.findViewById<ViewPager>(R.id.view_pager)?.apply {
            adapter =
                RFragmentAdapter(
                    childFragmentManager,
                    listOf(TestFragment1(), TestFragment2(), TestFragment3())
                )

            ViewPager1Delegate.install(this, rootView.findViewById(R.id.tab_layout))
        }
    }
}