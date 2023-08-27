package com.jhr.xfaniplugin.util

import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.jhr.xfaniplugin.actions.TodoAction
import com.jhr.xfaniplugin.components.Const.host
import com.jhr.xfaniplugin.components.Const.layoutSpanCount
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.AppUtil
import com.su.mediabox.pluginapi.util.UIUtil.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object ParseHtmlUtil {

    fun getCoverUrl(cover: String, imageReferer: String): String {
        return when {
            cover.startsWith("//") -> {
                try {
                    "${URL(imageReferer).protocol}:$cover"
                } catch (e: Exception) {
                    e.printStackTrace()
                    cover
                }
            }
            cover.startsWith("/") -> {
                //url不全的情况
                host + cover
            }
            else -> cover
        }
    }

    /**
     * 解析搜索的元素
     * @param element ul的父元素
     */
    suspend fun parseSearchEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()

        val content = element.select("div[class='main']").select(".module").select("div[class='module-main module-page']")
        val results: Elements = content.select("div[class='module-card-item module-item']")
        for (i in results.indices) {
            var cover = results[i].select("img").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val title = results[i].select(".module-card-item-title").text()
            val url = results[i].select("a").first()?.attr("href") ?:""
            val cardClass = results[i].select(".module-card-item-class").text() // 剧场版/BD动画/...
            val episode = results[i].select(".module-item-note").text() + " [$cardClass][稀饭动漫]"
            val tags = mutableListOf<TagData>()
            val tag = results[i].select(".module-info-item-content").first()?.ownText()?.also {
                it.split(" ").forEach { split ->
                    split.split(",").forEach { split1 ->
                        tags.add(TagData(split1))
                    }
                }
            }
            val describe = results[i].select(".module-info-item-content").last()?.text()?:""
            val item = MediaInfo2Data(
                title, cover, url, episode, describe, tags
            ).apply {
                    if (cardClass == "贤者专区"){
                        action = TodoAction
                    }else{
                        action = DetailAction.obtain(url)
                    }
                }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }
    /**
     * 解析分类下的元素
     * @param element ul的父元素
     */
    fun parseClassifyEm(
        element: Element,
        imageReferer: String
    ): List<BaseData> {
        val videoInfoItemDataList = mutableListOf<BaseData>()
        val results: Elements = element.select("div[class='main']").select("div[class='module']").select(">a")
        for (i in results.indices) {
            val title = results[i].select(".module-poster-item-title").text()
            var cover = results[i].select("img").attr("data-original")
            if (imageReferer.isNotBlank())
                cover = getCoverUrl(cover, imageReferer)
            val url = results[i].attr("href")
            val episode = results[i].select(".module-item-note").text()
            val item = MediaInfo1Data(title, cover, host + url, episode ?: "")
                .apply {
                    spanSize = layoutSpanCount / 3
                    action = DetailAction.obtain(url)
                }
            videoInfoItemDataList.add(item)
        }
        videoInfoItemDataList[0].layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
        return videoInfoItemDataList
    }
    /**
     * 解析分类元素
     */
    fun parseClassifyEm(element: Element): List<ClassifyItemData> {
        val classifyItemDataList = mutableListOf<ClassifyItemData>()
        val classifyCategory = element.select(".module-item-title").text()
        val li = element.select(".module-item-box").select("a")
        for (em in li){
            classifyItemDataList.add(ClassifyItemData().apply {
                    action = ClassifyAction.obtain(
                        em.attr("href").apply {
//                            Log.d("分类链接", this)
                        },
                        classifyCategory,
                        em.text()
                    )
                })
            }
        return classifyItemDataList
    }
}