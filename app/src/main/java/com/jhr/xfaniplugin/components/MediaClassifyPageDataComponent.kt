package com.jhr.xfaniplugin.components

import android.util.Log
import com.jhr.xfaniplugin.util.JsoupUtil
import com.jhr.xfaniplugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.components.IMediaClassifyPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.ClassifyItemData
import com.su.mediabox.pluginapi.util.PluginPreferenceIns
import com.su.mediabox.pluginapi.util.TextUtil.urlDecode
import com.su.mediabox.pluginapi.util.WebUtil
import com.su.mediabox.pluginapi.util.WebUtilIns
import org.jsoup.Jsoup

class MediaClassifyPageDataComponent : IMediaClassifyPageDataComponent {
    var classify : String = Const.host +"/show/1.html"

    override suspend fun getClassifyItemData(): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        //示例：使用WebUtil解析动态生成的分类项
        // TV动画
        // /show/1.html
        // BD动画
        // /show/2.html
        // 剧场版
        // /show/3.html
        // 美漫
        // /show/21.html
        val cookies = mapOf("cookie" to PluginPreferenceIns.get(JsoupUtil.cfClearanceKey, ""))
        Log.e("TAG","classify $classify")
        val document = Jsoup.parse(
            WebUtilIns.getRenderedHtmlCode(
                 classify, loadPolicy = object :
                    WebUtil.LoadPolicy by WebUtil.DefaultLoadPolicy {
                    override val headers = cookies
                    override val userAgentString = Const.ua
                    override val isClearEnv = false
                }
            )
        )
        document.select("div[class='module-main module-class']")
            .select("div[class='module-class-items scroll-box']").forEach {
            classifyItemDataList.addAll(ParseHtmlUtil.parseClassifyEm(it))
        }
        return classifyItemDataList
    }

    override suspend fun getClassifyData(
        classifyAction: ClassifyAction,
        page: Int
    ): List<BaseData> {
        val classifyList = mutableListOf<BaseData>()
        Log.e("TAG", "获取分类数据 ${classifyAction.url}")
        var str = classifyAction.url?.urlDecode() ?:""
        str = when(str){
            "/type/1.html" -> {
                // 修改分类项
                classify = Const.host +"/show/1.html"
                classify
            }
            "/type/2.html" -> {
                classify = Const.host +"/show/2.html"
                classify
            }
            "/type/3.html" -> {
                classify = Const.host +"/show/3.html"
                classify
            }
            "/type/21.html" -> {
                classify = Const.host +"/show/21.html"
                classify
            }
            else -> { str }
        }

        // 指定要插入的字符 charToInsert
        val charToInsert = "/page/${page}"
        val indexToInsert = str.length - 5

        // 使用 StringBuilder 创建一个可变的字符串，调用 insert() 方法将字符插入到指定位置，最后将结果转换回不可变字符串。
        var url = StringBuilder(str).insert(indexToInsert, charToInsert).toString()
        if (!url.startsWith(Const.host)){
            url = Const.host + url
        }
        Log.e("TAG", "获取分类数据 $url")
        JsoupUtil.getDocument(url).also {
            classifyList.addAll(ParseHtmlUtil.parseClassifyEm(it, url))
        }
        return classifyList
    }
}