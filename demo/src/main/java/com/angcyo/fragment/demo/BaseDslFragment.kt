package com.angcyo.fragment.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.base.back
import com.angcyo.base.dslFHelper
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.initDslAdapter
import com.angcyo.dsladapter.updateNow
import com.angcyo.dsladapter.updateOrInsertItem
import com.angcyo.fragment.AbsLifecycleFragment
import com.angcyo.item.DslButtonItem
import com.angcyo.item.DslTextItem

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseDslFragment : AbsLifecycleFragment() {

    lateinit var dslAdapter: DslAdapter

    init {
        fragmentLayoutId = R.layout.fragment_dsl
    }

    override fun initBaseView(rootView: View, savedInstanceState: Bundle?) {
        super.initBaseView(rootView, savedInstanceState)
        rootView.findViewById<Toolbar>(R.id.toolbar)?.title = this::class.java.simpleName
        rootView.findViewById<RecyclerView>(R.id.recycler_view)?.initDslAdapter {
            dslAdapter = this
            renderDslAdapter()
        }
    }

    override fun onFragmentShow(bundle: Bundle?) {
        super.onFragmentShow(bundle)
        i(this::class.java.simpleName + " onFragmentShow:" + fragmentShowCount)

        dslAdapter.updateOrInsertItem<DslTextItem>("show", 4) {
            it.apply {
                itemText = this::class.java.simpleName + " onFragmentShow:" + fragmentShowCount
            }
        }
    }

    override fun onFragmentHide() {
        super.onFragmentHide()
        i(this::class.java.simpleName + " onFragmentHide")

        dslAdapter.updateOrInsertItem<DslTextItem>("hide", 5) {
            it.apply {
                itemText =
                    this::class.java.simpleName + " onFragmentHide:" + System.currentTimeMillis()
            }
        }
    }

    open fun DslAdapter.renderDslAdapter() {
        DslTextItem()() {
            itemText =
                this@BaseDslFragment::class.java.simpleName + " " + System.currentTimeMillis()
        }

        DslButtonItem()() {
            itemButtonText = "点击启动 TestFragment1"
            itemClick = {
                dslFHelper {
                    show(TestFragment1::class.java)
                }
            }
        }

        DslButtonItem()() {
            itemButtonText = "点击启动 TestFragment2"
            itemClick = {
                dslFHelper {
                    show(TestFragment2::class.java)
                }
            }
        }

        DslButtonItem()() {
            itemButtonText = "点击启动 TestFragment3"
            itemClick = {
                dslFHelper {
                    show(TestFragment3::class.java)
                }
            }
        }

        if (parentFragment == null) {
            DslButtonItem()() {
                itemButtonText = "back 回退Fragment\n需要(BaseAppCompatActivity)支持"
                itemClick = {
                    back()
                }
            }

            DslButtonItem()() {
                itemButtonText = "remove 关闭当前Fragment"
                itemClick = {
                    dslFHelper {
                        remove(this@BaseDslFragment)
                    }
                }
            }

            DslButtonItem()() {
                itemButtonText = "remove所有, 保持MainFragment"
                itemClick = {
                    dslFHelper {
                        keep(MainFragment::class.java)
                    }
                }
            }

            DslButtonItem()() {
                itemButtonText = "remove所有"
                itemClick = {
                    dslFHelper {
                        finishActivityOnLastFragmentRemove = false
                        removeAll()
                    }
                }
            }
        }

        updateNow()
    }
}