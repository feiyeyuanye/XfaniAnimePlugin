package com.jhr.xfaniplugin.components

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import com.jhr.xfaniplugin.util.JsoupUtil
import com.su.mediabox.pluginapi.components.IMediaDetailPageDataComponent
import com.su.mediabox.pluginapi.action.ClassifyAction
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.action.PlayAction
import com.su.mediabox.pluginapi.data.*
import com.su.mediabox.pluginapi.util.TextUtil.urlEncode
import com.su.mediabox.pluginapi.util.UIUtil.dp
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class MediaDetailPageDataComponent : IMediaDetailPageDataComponent {

    override suspend fun getMediaDetailData(partUrl: String): Triple<String, String, List<BaseData>> {
        var cover = ""
        var title = ""
        var desc = ""
        // 导演
        var director = ""
        // 编剧
        var scriptwriter = ""
        // 主演
        var protagonist = ""
        // 更新时间
        var time = ""
        var upState = ""
        // 上映
        var show = ""
        val url = Const.host + partUrl
        val tags = mutableListOf<TagData>()
        val details = mutableListOf<BaseData>()

        val document = JsoupUtil.getDocument(url)

        // ------------- 番剧头部信息
        cover = document.select(".module-info-poster").select("img").attr("data-original")
        title = document.select(".module-info-heading").select("h1").text()
        // 更新状况
        val upStateItems = document.select(".module-info-items").select("div[class='module-info-item']")
        for (upStateEm in upStateItems){
            val t = upStateEm.text()
            when{
                t.contains("导演：") -> director = t
                t.contains("编剧：") -> scriptwriter = t
                t.contains("主演：") -> protagonist = t
                t.contains("备注：") -> upState = t
                t.contains("集数：") -> upState = t
                t.contains("上映：") -> show = t
                t.contains("更新：") -> time = t
            }
        }
        //类型
        val typeElements: Elements = document.select("div[class='module-info-tag']").select("a")
        for (l in typeElements.indices) {
            tags.add(TagData(typeElements[l].text())
//                .apply {
//                action = ClassifyAction.obtain(typeElements[l].attr("href"), "", typeElements[l].text())
//            }
            )
        }
        desc = document.select(".show-desc").text()

        // ---------------------------------- 播放列表+header
        val content = document.select("div[class='content']").first()?.also {
            val module = it.select("div[class='module']")[0]
            val playNameList = module.select("#y-playList").select(">div")
            val playEpisodeList = module.select("#panel1")
            for (index in 0..playNameList.size) {
                val playName = playNameList.getOrNull(index)
                val playEpisode = playEpisodeList.getOrNull(index)
                if (playName != null && playEpisode != null) {
                    val episodes = parseEpisodes(playEpisode)
                    if (episodes.isNullOrEmpty())
                        continue
                    details.add(
                        SimpleTextData(
                            playName.select("span").text() + "(${episodes.size}集)"
                        ).apply {
                            fontSize = 16F
                            fontColor = Color.WHITE
                        }
                    )
                    details.add(EpisodeListData(episodes))
                }
            }
            // ----------------------------------  系列动漫推荐
            val series = parseSeries(it.select("div[class='module']")[1])
            if (series.isNotEmpty()) {
                details.add(
                    SimpleTextData("其他系列作品").apply {
                        fontSize = 16F
                        fontColor = Color.WHITE
                    }
                )
                details.addAll(series)
            }
        }
        return Triple(cover, title, mutableListOf<BaseData>().apply {
            add(Cover1Data(cover).apply {
                layoutConfig =
                    BaseData.LayoutConfig(
                        itemSpacing = 12.dp,
                        listLeftEdge = 12.dp,
                        listRightEdge = 12.dp
                    )
            })
            if (title.isNotBlank()) add(
                SimpleTextData(title).apply {
                    fontColor = Color.WHITE
                    fontSize = 20F
                    gravity = Gravity.CENTER
                    fontStyle = 1
                }
            )
            if (tags.isNotEmpty()) add(TagFlowData(tags))
            add(
                LongTextData(desc).apply {
                    fontColor = Color.WHITE
                }
            )
            if (director.isNotBlank()) add(SimpleTextData("·$director").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            if (scriptwriter.isNotBlank()) add(SimpleTextData("·$scriptwriter").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            if (protagonist.isNotBlank()) add(SimpleTextData("·$protagonist").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            if (show.isNotBlank()) add(SimpleTextData("·$show").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            if (time.isNotBlank()) add(SimpleTextData("·$time").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            if (upState.isNotBlank()) add(SimpleTextData("·$upState").apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            if (title.isNotBlank()) add(LongTextData(douBanSearch(title)).apply {
                fontSize = 14F
                fontColor = Color.WHITE
                fontStyle = Typeface.BOLD
            })
            addAll(details)
        })
    }

    private fun parseEpisodes(element: Element): List<EpisodeData> {
        val episodeList = mutableListOf<EpisodeData>()
        val elements: Elements = element.select(".module-play-list").select("a")
        for (k in elements.indices) {
            val episodeUrl = elements[k].attr("href")
            episodeList.add(
                EpisodeData(elements[k].text(), episodeUrl).apply {
                    action = PlayAction.obtain(episodeUrl)
                }
            )
        }
        return episodeList
    }

    private fun parseSeries(element: Element): List<MediaInfo1Data> {
        val videoInfoItemDataList = mutableListOf<MediaInfo1Data>()
        val results = element.select(".module-items").select("a")
        for (i in results.indices) {
            val cover = results[i].select("img").attr("data-original")
            val title = results[i].select(".module-poster-item-title").text()
            val url = results[i].attr("href")
            val item = MediaInfo1Data(
                title, cover, Const.host + url,
                nameColor = Color.WHITE, coverHeight = 120.dp
            ).apply {
                action = DetailAction.obtain(url)
            }
            videoInfoItemDataList.add(item)
        }
        return videoInfoItemDataList
    }

    private fun douBanSearch(name: String) =
        "·豆瓣评分：https://m.douban.com/search/?query=${name.urlEncode()}"
}