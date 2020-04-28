package com.angcyo.fragment

import android.content.Intent

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/12
 */

interface IFragmentBridge {

    /**[android.app.Activity.onActivityResult]*/
    fun onActivityResult(resultCode: Int, data: Intent?) {

    }

    /**[android.app.Activity.onRequestPermissionsResult]*/
    fun onRequestPermissionsResult(permissions: Array<out String>, grantResults: IntArray) {

    }
}
