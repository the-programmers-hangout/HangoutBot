package me.markhc.hangoutbot.modules.`fun`.services

import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.JsonObject
import me.jakejmattson.kutils.api.annotations.Service

@Service
class AnilistService() {
    companion object {
        val query_string = AnilistService::class.java.getResource("/anilist_query.graphql")!!.readText().replace("\r\n", "\n")
    }
    data class Query(val query: String, val variables: Map<String, Any>)
    data class QueryErrors(val message: String, val status: Int)
    data class QueryResult(val data: JsonObject?, val errors: List<QueryErrors>?)

    fun searchMedia(type: MediaType, search: String): Media {
        val query = Query(query_string, mapOf("type" to type, "search" to search))

        val (_, _, result) = Fuel
                .post("https://graphql.anilist.co")
                .set("User-Agent", "HangoutBot (https://github.com/the-programmers-hangout/HangoutBot/)")
                .set("Accept", "application/json")
                .set("Content-type", "application/json")
                .body(Gson().toJson(query))
                .responseString()

        return result.fold(
                success = {
                    val obj = Gson().fromJson(it, QueryResult::class.java)
                    when {
                        obj.errors != null -> {
                            throw UnknownError(obj.errors.first().message)
                        }
                        obj.data == null -> {
                            throw UnknownError("Anilist request was successfull, but no data was returned.")
                        }
                        else -> {
                            return@fold Gson().fromJson(obj.data["Media"], Media::class.java)
                        }
                    }
                },
                failure = {
                    if(it.response.statusCode == 404) {
                        throw UnknownError("No results.")
                    }
                    throw it.exception
                })
    }
}