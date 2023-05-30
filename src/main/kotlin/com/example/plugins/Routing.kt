package com.example.plugins

import com.example.Movie123Provider
import com.example.utils.CineNexaConverter
import com.google.gson.Gson
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.utils.ExtractorLink
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Application.configureRouting() {
    routing {
        get("/") {
            val gson = Gson();
            val provider = Movie123Provider();

            val subtitles = ArrayList<SubtitleFile>();
            val links = ArrayList<ExtractorLink>();

            provider.loadLinks(
                gson.toJson(CineNexaConverter.convertIncomingRequestToLinkObject(call.request.queryParameters)),
                false,
                { subtitleFile ->
                    subtitles.add(subtitleFile);
                },
                { extractorLink ->
                    links.add(extractorLink);
                });

            val resp = suspendCoroutine { cont ->
                Timer().schedule(timerTask {
                    val response = ArrayList<CineNexaConverter.Stream>();
                    for (i in links) {
                        response.add(CineNexaConverter.generateResponse(i, subtitles));
                    }
                    cont.resume(response);
                }, 10000)
            };
            call.respond(HttpStatusCode.OK, resp); // Content negotiation automatically converts this to json
        }
    }
}
