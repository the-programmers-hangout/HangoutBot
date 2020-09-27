package me.markhc.hangoutbot.utilities

import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.*
import com.gitlab.kordlib.rest.Image
import kotlinx.coroutines.flow.*
import me.jakejmattson.discordkt.api.dsl.CommandEvent
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.services.BotStatsService
import org.joda.time.DateTime
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

suspend fun CommandEvent<*>.createBotStatsEmbed(botStats: BotStatsService, config: Configuration) = respond {
    title = "Stats"
    color = discord.configuration.theme

    field {
        name = "Commands"
        value = """
            ```
            Commands executed:      ${String.format("%6d", config.totalCommandsExecuted)}
            Commands since restart: ${String.format("%6d", botStats.totalCommands)}
            ```
        """.trimIndent()
    }

    val runtime = Runtime.getRuntime()
    val usedMemory = runtime.totalMemory() - runtime.freeMemory()

    field {
        name = "Memory"
        value = "${usedMemory / 1000000}/${runtime.totalMemory() / 1000000} MiB"
        inline = true
    }

    field {
        name = "Ping"
        value = botStats.ping
        inline = true
    }

    field {
        name = "Uptime"
        value = botStats.uptime
    }
}

suspend fun CommandEvent<*>.buildGuildInfoEmbed(guild: Guild) = respond {
    title = guild.name
    color = discord.configuration.theme

    footer {
        text = "Guild creation date: ${DateTimeFormatter.RFC_1123_DATE_TIME.format(guild.id.timeStamp)}"
    }
    thumbnail {
        url = guild.getIconUrl(Image.Format.PNG) ?: ""
    }

    if (guild.description != null) {
        field {
            name = "**Description**"
            value = guild.description!!
        }
    }

    field {
        name = "**Owner**"
        value = guild.getOwner().tag
        inline = true
    }
    field {
        name = "**Users**"
        //TODO value = "${guild.members.toList().filter { it.onlineStatus != OnlineStatus.OFFLINE }.size}/${guild.memberCount}"
        inline = true
    }
    field {
        name = "**Roles**"
        value = guild.roles.count().toString()
        inline = true
    }
    field {
        name = "**Text Channels**"
        value = guild.channels.filterIsInstance<TextChannel>().count().toString()
        inline = true
    }
    field {
        name = "**Voice Channels**"
        value = guild.channels.filterIsInstance<VoiceChannel>().count().toString()
        inline = true
    }
    field {
        name = "**Region**"
        value = guild.getRegion().name
        inline = true
    }
    field {
        name = "**Verification Level**"
        value = guild.verificationLevel.toString()
        inline = true
    }
    field {
        name = "**Emotes**"
        //TODO value = "${guild.emojis.size}/${guild * 2}"
        inline = true
    }
    field {
        name = "**Invite URL**"
        value = if (guild.getVanityUrl() != null) "[Link](${guild.getVanityUrl()})" else "Not set"
        inline = true
    }
    field {
        name = "**Boosts**"
        //TODO value = "${guild.boostCount} (Tier: ${guild.boostTier.ordinal})"
        inline = true
    }
}

suspend fun CommandEvent<*>.buildRoleInfoEmbed(role: Role) = respond {
    title = "Role information"
    color = role.color

    field {
        name = "**Name**"
        value = role.name
        inline = true
    }
    field {
        name = "**Id**"
        value = role.id.value
        inline = true
    }
    field {
        name = "**Color**"
        value = "**rgb(${role.color.red}, ${role.color.green}, ${role.color.blue})**"
        inline = true
    }

    field {
        name = "**Is Hoisted**"
        value = role.hoisted.toString()
        inline = true
    }
    field {
        name = "**Is Mentionable**"
        value = role.mentionable.toString()
        inline = true
    }
    field {
        name = "**Is Managed**"
        value = role.managed.toString()
        inline = true
    }
    field {
        name = "**Members**"
        value = "${guild!!.members.toList().filter { role in it.roles.toList() }.size} members"
    }
}

private fun formatOffsetTime(time: Instant): String {
    val days = TimeUnit.MILLISECONDS.toDays(DateTime.now().millis - time.toEpochMilli())
    return if (days > 4) {
        "$days days ago\n${DateTimeFormatter.ISO_LOCAL_DATE.format(time)}"
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(DateTime.now().millis - time.toEpochMilli())
        "$hours hours ago\n${DateTimeFormatter.ISO_LOCAL_DATE.format(time)}"
    }
}

suspend fun CommandEvent<*>.buildUserInfoEmbed(user: User) = respond {
    title = "User information"
    color = discord.configuration.theme
    thumbnail {
        url = user.avatar.url
    }

    field {
        name = "**Username**"
        value = user.tag
        inline = true
    }
    field {
        name = "**Avatar**"
        value = "[[Link]](${user.avatar.url}?size=512)\n[[Search]](https://www.google.com/searchbyimage?&image_url=${user.avatar.url})"
        inline = true
    }
    field {
        name = "**Id**"
        value = user.id.value
        inline = true
    }
    field {
        name = "**Created**"
        value = formatOffsetTime(user.id.timeStamp)
        inline = true
    }
}

fun getSafeNickname(member: Member): String {
    if (member.nickname == null) return "Not set"

    if (member.nickname!!.length <= 16) return member.nickname!!

    return "${member.nickname!!.take(12)}..."
}

suspend fun CommandEvent<*>.buildMemberInfoEmbed(member: Member) = respond {
    title = "User information"
    color = member.roles.toList().maxByOrNull { it.rawPosition }?.color
    thumbnail {
        url = member.avatar.url
    }

    field {
        name = "**Username**"
        value = member.tag
        inline = true
    }
    field {
        name = "**Nickname**"
        value = getSafeNickname(member)
        inline = true
    }
    field {
        name = "**Id**"
        value = member.id.value
        inline = true
    }
    field {
        name = "**Avatar**"
        value = "[[Link]](${member.avatar.url}?size=512)\n[[Search]](https://www.google.com/searchbyimage?&image_url=${member.avatar.url})"
        inline = true
    }
    field {
        name = "**Created**"
        value = formatOffsetTime(member.id.timeStamp)
        inline = true
    }
    field {
        name = "**Joined**"
        value = formatOffsetTime(member.id.timeStamp)
        inline = true
    }

    if (member.roles.toList().isNotEmpty()) {
        field {
            name = "**Roles**"
            value = member.roles.toList().joinToString { it.name }
            inline = true
        }
    }
}
