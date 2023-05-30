package com.example.utils

import com.example.Movie123Provider
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.utils.ExtractorLink
import io.ktor.http.*

class CineNexaConverter {
    data class VideoResponse(
        val streams: List<Stream>
    )

    data class Stream(
        val url: String,
        val name: String,
        val quality: Int,
        val size: Int? = null,
        val subbed: Boolean? = null,
        val streamGroup: String,
        val subtitles: List<Subtitle>
    )

    data class Subtitle(
        val title: String,
        val url: String
    )


    companion object {
        fun convertIncomingRequestToLinkObject(parameters: Parameters): Movie123Provider.LinkData {
            return Movie123Provider.LinkData(
                parameters["tmdbId"]?.toInt(),
                parameters["imdbId"],
                parameters["type"],
                parameters["season"]?.toInt(),
                parameters["episode"]?.toInt(),
                null,
                null,
                parameters["title"],
                parameters["releaseYear"]?.toInt(),
                parameters["title"],
                false,
                parameters["releaseYear"]?.toInt(),
            );
        }

        fun generateResponse(videoLink: ExtractorLink, subtitles: List<SubtitleFile>): Stream {
            return Stream(
                url = videoLink.url,
                name = videoLink.name,
                quality = videoLink.quality,
                //size = no option for size
                //subbed = no option for subbed
                streamGroup = "SAMPLE_EXT_UNIQUE_STRING",
                subtitles = subtitles.map { Subtitle(it.lang, it.url) }
            )
        }
    }
}

