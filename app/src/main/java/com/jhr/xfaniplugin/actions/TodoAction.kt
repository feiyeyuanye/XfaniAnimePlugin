package com.jhr.xfaniplugin.actions

import android.content.Context
import android.widget.Toast
import com.su.mediabox.pluginapi.action.Action
import com.su.mediabox.pluginapi.util.AppUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 注意不能使用匿名类自定义action
 */
object TodoAction : Action() {

    override fun go(context: Context) {
        Toast.makeText(context, "无权限访问此数据", Toast.LENGTH_LONG).show()
    }
}