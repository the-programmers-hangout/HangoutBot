package me.markhc.hangoutbot.commands.`fun`.services

import com.github.kittinunf.fuel.*
import com.github.kittinunf.fuel.gson.responseObject
import me.jakejmattson.discordkt.api.annotations.Service
import org.joda.time.*
import java.net.URLEncoder

private data class XKCDInfo(val num: Int = 0)

@Service
class XKCDService {
    companion object {
        private var xkcdLatest: Int = 1
        private var xkcdLatestCacheTime: DateTime = DateTime(0)
    }

    fun getLatest(): Int? {
        val timeDiff = Duration(xkcdLatestCacheTime, DateTime.now())

        return if (timeDiff.standardHours < 24) {
            xkcdLatest
        } else {
            fetchLatest()
        }
    }

    fun search(what: String): Int? {
        val comicNumberParseRegex = "(?:\\S+\\s+){2}(\\S+)".toRegex()

        val (_, _, result) = Fuel
            .get("https://relevantxkcd.appspot.com/process?action=xkcd&query=${URLEncoder.encode(what, "UTF-8")}")
            .responseString()

        return result.fold(
            success = { comicNumberParseRegex.find(it)!!.groups[1]?.value!!.toInt() },
            failure = { null })
    }

    fun getUrl(comicNumber: Int) =
        "http://xkcd.com/$comicNumber/"

    private fun fetchLatest(): Int? {
        val (_, _, result) = "https://xkcd.com/info.0.json".httpGet().responseObject<XKCDInfo>()

        return result.fold(
            success = {
                xkcdLatestCacheTime = DateTime.now()
                xkcdLatest = it.num
                xkcdLatest
            },
            failure = { null })
    }
}