package me.markhc.hangoutbot.modules.`fun`.services

import com.google.gson.annotations.SerializedName
import me.jakejmattson.kutils.api.dsl.embed.embed
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

data class MediaTitle(val romaji: String? = "",
                      val english: String? = "",
                      val native: String? = "")

enum class MediaType {
    ANIME,
    MANGA
}

@Suppress("unused")
enum class MediaFormat {
    TV,
    TV_SHORT,
    MOVIE,
    SPECIAL,
    OVA,
    ONA,
    MUSIC,
    MANGA,
    NOVEL,
    ONE_SHOT
}

@Suppress("unused")
enum class MediaStatus {
    FINISHED,
    RELEASING,
    NOT_YET_RELEASED,
    CANCELLED
}

@Suppress("unused")
enum class MediaSeason {
    WINTER,
    SPRING,
    SUMMER,
    FALL
}

@Suppress("unused")
enum class MediaSource {
    ORIGINAL,
    MANGA,
    LIGHT_NOVEL,
    VISUAL_NOVEL,
    VIDEO_GAME,
    OTHER,
    NOVEL,
    DOUJINSHI,
    ANIME
}

data class MediaCoverImage(val large: String = "",
                           val medium: String = "",
                           val color: String = "")

data class Studio(val id: Int,
                  val name: String)

data class StudioEdge(val id: Int,
                      val node: Studio,
                      val isMain: Boolean)

data class StudioConnection(val edges: List<StudioEdge> = listOf())

@Suppress("unused")
enum class MediaRankType {
    RATED,
    POPULAR
}

data class MediaRank(val id: Int,
                     val rank: Int,
                     val type: MediaRankType,
                     val year: Int? =  null,
                     val allTime: Boolean,
                     val season: MediaSeason? = null)

data class FuzzyDate(val year: Int?, val month: Int?, val day: Int?)

data class Media(val id: Int,
                 @SerializedName("title")
                 val mediaTitle: MediaTitle,
                 val type: MediaType,
                 val format: MediaFormat,
                 val status: MediaStatus,
                 val startDate: FuzzyDate? = null,
                 @SerializedName("description")
                 val mediaDescription: String,
                 val meanScore: Int = 0,
                 val season: MediaSeason? =  null,
                 val seasonYear: Int? =  null,
                 val episodes: Int? =  null,
                 val chapters: Int? =  null,
                 val volumes: Int? =  null,
                 val source: MediaSource? =  null,
                 val coverImage: MediaCoverImage? = null,
                 val genres: List<String>,
                 val favourites: Int,
                 val studios: StudioConnection? =  null,
                 val isAdult: Boolean = false,
                 val rankings: List<MediaRank>,
                 val siteUrl: String) {
    fun buildEmbed() = embed {
        title {
            text = getNiceTitle()
            url = siteUrl
        }

        description = getNiceDescription()
        thumbnail = coverImage?.large ?: coverImage?.medium
        color = if(coverImage?.medium != null) averageImageColor(coverImage.medium) else infoColor

        addInlineField("Status", status.toString().toLowerCase().replace('_', ' ').capitalize())

        if(season != null) {
            addInlineField("Season", season.toString().toLowerCase().capitalize() + (startDate?.year?.let { " $it" } ?: ""))
        } else {
            addInlineField("Start date", startDate?.let { formatFuzzyDate(it) } ?: "Unknown")
        }

        field {
            inline = true
            if(type == MediaType.ANIME) {
                name = "Episodes"
                value = episodes?.toString() ?: "Unknown"
            } else {
                name = "Chapters"
                value = chapters?.toString() ?: "Unknown"
            }
        }

        if(source != null) {
            addInlineField("Source", source.toString().toLowerCase().replace('_', ' ').capitalize())
        }

        if(studios != null) {
            val mainStudio = studios.edges.filter { it.isMain }
            if(mainStudio.isNotEmpty()) {
                addInlineField("Studio", mainStudio.joinToString { it.node.name })
            }
        }

        if(meanScore != 0) {
            addInlineField("Score", (meanScore / 10.0).toString())
        }

        if(genres.isNotEmpty()) {
            addInlineField("Genres", genres.joinToString())
        }

        val allTime = rankings.filter { it.allTime }
        if(allTime.isNotEmpty()) {
            footer {
                text = allTime.joinToString(", ") {
                    if(it.type == MediaRankType.POPULAR) {
                        "#${it.rank} Most Popular All Time"
                    } else {
                        "#${it.rank} Highest Rated All Time"
                    }
                }

            }
        }

    }

    private fun getNiceTitle(): String {
        return mediaTitle.romaji ?: mediaTitle.english ?: mediaTitle.native ?: "No title"
    }

    private fun getNiceDescription(): String {
        // Replace some simple html tags
        val str = mediaDescription
                .replace("<i>", "*").replace("</i>", "*")
                .replace("<b>", "*").replace("</b>", "*")
                .replace("<br>", "\n")
                // replace multiple new lines with a single one
                .replace("\n{2,}".toRegex(), "\n")

        return if(str.length < 256) str else "${str.take(256)}... [Read more](${siteUrl})"
    }
}

private fun averageColor(bi: BufferedImage, w: Int,
                         h: Int): Color? {
    var sumr: Long = 0
    var sumg: Long = 0
    var sumb: Long = 0
    for (x in 0 until w) {
        for (y in 0 until h) {
            val pixel = Color(bi.getRGB(x, y))
            sumr += pixel.red
            sumg += pixel.green
            sumb += pixel.blue
        }
    }
    val num = w * h
    return Color(sumr.toInt() / num, sumg.toInt() / num, sumb.toInt() / num)
}
private fun averageImageColor(imageUrl: String): Color? {
    val url = URL(imageUrl)
    val img = ImageIO.read(url)
    return averageColor(img, img.width, img.height)
}

private fun formatFuzzyDate(date: FuzzyDate): String {
    val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    return when {
        date.day != null -> {
            "${months[date.month!!]} ${date.day}, ${date.year!!}"
        }
        date.month != null -> {
            "${months[date.month]} ${date.year!!}"
        }
        date.year != null -> {
            "${date.year}"
        }
        else -> {
            ""
        }
    }
}