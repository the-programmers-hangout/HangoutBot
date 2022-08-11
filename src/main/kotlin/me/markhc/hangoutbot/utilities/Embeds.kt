package me.markhc.hangoutbot.utilities

import dev.kord.core.entity.*
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.rest.Image
import dev.kord.rest.request.RestRequestException
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import me.jakejmattson.discordkt.commands.CommandEvent
import me.jakejmattson.discordkt.commands.GuildCommandEvent
import me.jakejmattson.discordkt.extensions.pfpUrl
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

suspend fun CommandEvent<*>.buildGuildInfoEmbed(guild: Guild) = respond {
    title = guild.name
    color = discord.configuration.theme

    footer {
        text = "Guild creation date: ${DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDateTime.ofInstant(guild.id.timestamp.toJavaInstant(), ZoneOffset.UTC))}"
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
//    field {
//        name = "**Users**"
//        value = guild.withStrategy(EntitySupplyStrategy.rest).members.count().toString()
//        inline = true
//    }
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
    //TODO
//    field {
//        name = "**Region**"
//        value = guild.getRegion().name
//        inline = true
//    }
    field {
        name = "**Verification Level**"
        value = guild.verificationLevel.toString()
        inline = true
    }
    field {
        name = "**Emotes**"
        value = "${guild.emojis.count()}"
        inline = true
    }

    //TODO this wont be needed after 0.7.0 kord
    try {
        val invite = guild.getVanityUrl()

        field {
            name = "**Invite URL**"
            value = if (invite != null) "[Link]($invite)" else "Not set"
            inline = true
        }
    } catch (_: RestRequestException) {

    }

    //TODO field {
//        name = "**Boosts**"
//        value = "${guild.boostCount} (Tier: ${guild.boostTier.ordinal})"
//        inline = true
//    }
}

suspend fun GuildCommandEvent<*>.buildRoleInfoEmbed(role: Role) = respond {
    title = "Role information"
    color = role.color

    field {
        name = "**Name**"
        value = role.name
        inline = true
    }
    field {
        name = "**Id**"
        value = role.id.toString()
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
        value = "${guild.members.toList().filter { role in it.roles.toList() }.size} members"
    }
}

private fun formatOffsetTime(time: Instant): String {
    val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - time.epochSeconds)
    val dateTime = LocalDateTime.ofInstant(time.toJavaInstant(), ZoneOffset.UTC)
    return if (days > 4) {
        "$days days ago\n${DateTimeFormatter.ISO_LOCAL_DATE.format(dateTime)}"
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - time.epochSeconds)
        "$hours hours ago\n${DateTimeFormatter.ISO_LOCAL_DATE.format(dateTime)}"
    }
}

suspend fun CommandEvent<*>.buildUserInfoEmbed(user: User) = respond {
    title = "User information"
    color = discord.configuration.theme
    thumbnail {
        url = user.pfpUrl
    }

    field {
        name = "**Username**"
        value = user.tag
        inline = true
    }
    field {
        name = "**Avatar**"
        value = "[[Link]](${user.pfpUrl}?size=512)\n[[Search]](https://www.google.com/searchbyimage?&image_url=${user.pfpUrl})"
        inline = true
    }
    field {
        name = "**Id**"
        value = user.id.toString()
        inline = true
    }
    field {
        name = "**Created**"
        value = formatOffsetTime(user.id.timestamp)
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
        url = member.pfpUrl
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
        value = member.id.toString()
        inline = true
    }
    field {
        name = "**Avatar**"
        value = "[[Link]](${member.pfpUrl}?size=512)\n[[Search]](https://www.google.com/searchbyimage?&image_url=${member.pfpUrl})"
        inline = true
    }
    field {
        name = "**Created**"
        value = formatOffsetTime(member.id.timestamp)
        inline = true
    }
    field {
        name = "**Joined**"
        value = formatOffsetTime(member.joinedAt)
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
