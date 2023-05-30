package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink

class Movie123Provider : MainAPI() {
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
    )
    override var lang = "en"

    override var mainUrl = "https://ww1.new-movies123.co"
    override var name = "Movie123"

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val res = app.get(data, verify = false)
        val match = "var url = '(/user/servers/.*?\\?ep=.*?)';".toRegex().find(res.text)
        val serverUrl = match?.groupValues?.get(1) ?: return false
        val cookies = res.okhttpResponse.headers.getCookies()
        val doc = res.document
        val url = doc.select("meta[property=og:url]").attr("content")
        val headers = mapOf("X-Requested-With" to "XMLHttpRequest")
        val qualities = intArrayOf(2160, 1440, 1080, 720, 480, 360)
        app.get(
            "$mainUrl$serverUrl",
            cookies = cookies, referer = url, headers = headers
        ).document.select("ul li").amap { el ->
            val server = el.attr("data-value")
            val encryptedData = app.get(
                "$url?server=$server&_=${System.currentTimeMillis()}",
                cookies = cookies,
                referer = url,
                headers = headers
            ).text
            val json = base64Decode(encryptedData).xorDecrypt()
            val links = tryParseJson<List<VideoLink>>(json) ?: return@amap
            links.forEach { video ->
                qualities.filter { it <= video.max.toInt() }.forEach {
                    callback(
                        ExtractorLink(
                            name,
                            video.language,
                            video.src.split("360", limit = 3).joinToString(it.toString()),
                            "$mainUrl/",
                            it
                        )
                    )

                }
            }
        }
        return true
    }

    private fun String.xorDecrypt(key: String = "123"): String {
        val sb = StringBuilder()
        var i = 0
        while (i < this.length) {
            var j = 0
            while (j < key.length && i < this.length) {
                sb.append((this[i].code xor key[j].code).toChar())
                j++
                i++
            }
        }
        return sb.toString()
    }

    data class VideoLink(
        val src: String,
        val file: String,
        val label: Int,
        val type: String,
        val size: String,
        val max: String,
        val language: String
    )

    data class LinkData(
        val id: Int? = null,
        val imdbId: String? = null,
        val type: String? = null,
        val season: Int? = null,
        val episode: Int? = null,
        val aniId: String? = null,
        val animeId: String? = null,
        val title: String? = null,
        val year: Int? = null,
        val orgTitle: String? = null,
        val isAnime: Boolean = false,
        val airedYear: Int? = null,
        val lastSeason: Int? = null,
        val epsTitle: String? = null,
        val jpTitle: String? = null,
        val date: String? = null,
        val airedDate: String? = null,
    )
}
