package com.jhr.xfaniplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import com.jhr.xfaniplugin.components.Const.host
import com.jhr.xfaniplugin.components.Const.layoutSpanCount
import com.jhr.xfaniplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.IHomePageDataComponent
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.UIUtil.dp

class HomePageDataComponent : IHomePageDataComponent {

    override suspend fun getData(page: Int): List<BaseData>? {
        if (page != 1)
            return null
        val url = host
        val doc = JsoupUtil.getDocument(url)
        val data = mutableListOf<BaseData>()

        //1.横幅
        doc.select(".container-slide").select(".swiper-wrapper").apply {
            val bannerItems = mutableListOf<BannerData.BannerItemData>()
            select(">div").forEach { bannerItem ->
                val nameEm = bannerItem.select(".v-title").text()
                var ext = ""
                bannerItem.select(".v-ins").select("p").forEach {
                    ext += it.text() + "\n"
                }
                val videoUrl = bannerItem.select("a").attr("href")
                val bannerImage = bannerItem.select("a").attr("style").substringAfter("(").substringBefore(")")
                if (bannerImage.isNotBlank()) {
//                    Log.e("TAG", "添加横幅项 封面：$bannerImage 链接：$videoUrl")
                    bannerItems.add(
                        BannerData.BannerItemData(bannerImage,nameEm, ext).apply {
                            if (!videoUrl.isNullOrBlank())
                                action = DetailAction.obtain(videoUrl)
                        }
                    )
                }
            }
            if (bannerItems.isNotEmpty())
                data.add(BannerData(bannerItems, 6.dp).apply {
                    layoutConfig = BaseData.LayoutConfig(layoutSpanCount, 14.dp)
                    spanSize = layoutSpanCount
                })
        }

        //3.各类推荐
        val module = doc.select("div[class='content']").first()?:return data
        val modules = module.select("div[class='module']")
        for ((i,em) in modules.withIndex()){
            if (i == 0) continue // 番剧周表
            val moduleHeading = em.select(".module-heading")
            val type = moduleHeading.select(".module-title")
            val typeUrl = moduleHeading.select(".module-title").select("a").attr("href")
            val typeName = type.select("a").first()?.ownText()
            if (!typeName.isNullOrBlank()) {
                data.add(SimpleTextData(typeName).apply {
                    fontSize = 15F
                    fontStyle = Typeface.BOLD
                    fontColor = Color.BLACK
                    spanSize = layoutSpanCount / 2
                })
                  data.add(SimpleTextData("查看更多 >").apply {
                        fontSize = 12F
                        gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                        fontColor = Const.INVALID_GREY
                        spanSize = layoutSpanCount / 2
                    }.apply {
                        action = ClassifyAction.obtain(typeUrl, typeName)
                    })
            }
            val li = em.select(".module-items").select("a")
            for ((index,video) in li.withIndex()){
                video.apply {
                    val name = select(".module-poster-item-title").text()
                    val videoUrl = attr("href")
                    val coverUrl = select("img").attr("data-original")
                    val episode = select(".module-item-note").text()
                    if (!name.isNullOrBlank() && !videoUrl.isNullOrBlank() && !coverUrl.isNullOrBlank()) {
                         data.add(
                            MediaInfo1Data(name, coverUrl, videoUrl, episode ?: "")
                                .apply {
                                    spanSize = layoutSpanCount / 3
                                    action = DetailAction.obtain(videoUrl)
                                })
//                        Log.e("TAG", "添加视频 ($name) ($videoUrl) ($coverUrl) ($episode)")
                    }
                }
                if (index == 11) break
            }
        }
        return data
    }
}