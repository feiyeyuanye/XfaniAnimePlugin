package com.jhr.xfaniplugin.components

import android.util.Log
import com.jhr.xfaniplugin.components.Const.host
import com.jhr.xfaniplugin.util.JsoupUtil
import com.jhr.xfaniplugin.util.ParseHtmlUtil
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData

class MediaSearchPageDataComponent : IMediaSearchPageDataComponent {

    override suspend fun getSearchData(keyWord: String, page: Int): List<BaseData> {
        val searchResultList = mutableListOf<BaseData>()
        // https://dick.xfani.com/search/wd/%E9%BE%99/page/2.html
        val url = "${host}/search/wd/${keyWord}/page/${page}.html"
        Log.e("TAG", url)

        val document = JsoupUtil.getDocument(url)
        searchResultList.addAll(ParseHtmlUtil.parseSearchEm(document, url))
        return searchResultList
    }

}