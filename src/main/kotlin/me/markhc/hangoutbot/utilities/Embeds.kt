package me.markhc.hangoutbot.utilities

import me.jakejmattson.kutils.api.dsl.embed.embed
import me.jakejmattson.kutils.api.extensions.jda.fullName
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import org.joda.time.DateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun buildServerInfoEmbed(guild: Guild) = embed {
    title { text = guild.name }
    color = infoColor

    footer {
        text = "Guild creation date: ${guild.timeCreated.format(DateTimeFormatter.RFC_1123_DATE_TIME)}"
    }
    thumbnail = guild.iconUrl ?: ""

    if(guild.description != null) {
        field {
            name = "**Description**"
            value = guild.description
        }
    }

    field {
        name = "**Owner**"
        value = guild.owner?.user?.fullName() ?: "<None>"
        inline = true
    }
    field {
        name = "**Users**"
        value = "${guild.members.filter { it.onlineStatus != OnlineStatus.OFFLINE }.size}/${guild.members.size}"
        inline = true
    }
    field {
        name = "**Roles**"
        value = guild.roles.size.toString()
        inline = true
    }
    field {
        name = "**Text Channels**"
        value = guild.textChannelCache.size().toString()
        inline = true
    }
    field {
        name = "**Voice Channels**"
        value = guild.voiceChannels.size.toString()
        inline = true
    }
    field {
        name = "**Region**"
        value = guild.region.toString()
        inline = true
    }
    field {
        name = "**Verification Level**"
        value = guild.verificationLevel.toString()
        inline = true
    }
    field {
        name = "**Emotes**"
        value = "${guild.emotes.size}/${guild.maxEmotes*2}"
        inline = true
    }
    field {
        name = "**Invite URL**"
        value = if(guild.vanityUrl != null) "[Link](${guild.vanityUrl})" else "Not set"
        inline = true
    }
    field {
        name = "**Boosts**"
        value = "${guild.boostCount} (Tier: ${guild.boostTier.ordinal})"
        inline = true
    }
}

fun buildRoleInfoEmbed(role: Role) = embed {
    title { text = "Role information" }
    color = role.color

    field {
        name = "**Name**"
        value = role.name
        inline = true
    }
    field {
        name = "**Id**"
        value = role.id
        inline = true
    }
    field {
        name = "**Color**"
        value = if(role.color != null) "**rgb(${role.color!!.red}, ${role.color!!.green}, ${role.color!!.blue})**" else "None"
        inline = true
    }

    field {
        name = "**Is Hoisted**"
        value = role.isHoisted.toString()
        inline = true
    }
    field {
        name = "**Is Mentionable**"
        value = role.isMentionable.toString()
        inline = true
    }
    field {
        name = "**Is Managed**"
        value = role.isManaged.toString()
        inline = true
    }
    field {
        name = "**Members**"
        value = "${role.guild.getMembersWithRoles(role).size} members"
    }
}

private fun formatOffsetTime(time: OffsetDateTime): String {
    val days = TimeUnit.MILLISECONDS.toDays(DateTime.now().millis - time.toInstant().toEpochMilli())
    return if(days > 4) {
        "$days days ago\n${time.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
    } else {
        val hours = TimeUnit.MILLISECONDS.toHours(DateTime.now().millis - time.toInstant().toEpochMilli())
        "$hours hours ago\n${time.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
    }
}

fun buildUserInfoEmbed(user: User) = embed {
    title { text = "User information" }
    color = infoColor
    thumbnail = user.effectiveAvatarUrl

    field {
        name = "**Username**"
        value = user.fullName()
        inline = true
    }
    field {
        name = "**Avatar**"
        value = "[[Link]](${user.effectiveAvatarUrl}?size=512)\n[[Search]](https://www.google.com/searchbyimage?&image_url=${user.effectiveAvatarUrl})"
        inline = true
    }
    field {
        name = "**Id**"
        value = user.id
        inline = true
    }
    field {
        name = "**Created**"
        value = formatOffsetTime(user.timeCreated)
        inline = true
    }
}

fun getSafeNickname(member: Member): String {
    if(member.nickname == null) return "Not set"

    if(member.nickname!!.length <= 16) return member.nickname!!

    return "${member.nickname!!.take(12)}..."
}

fun buildMemberInfoEmbed(member: Member) = embed {
    title { text = "User information" }
    color = member.color
    thumbnail = member.user.effectiveAvatarUrl

    field {
        name = "**Username**"
        value = member.user.fullName()
        inline = true
    }
    field {
        name = "**Nickname**"
        value =  getSafeNickname(member)
        inline = true
    }
    field {
        name = "**Id**"
        value = member.id
        inline = true
    }
    field {
        name = "**Avatar**"
        value = "[[Link]](${member.user.effectiveAvatarUrl}?size=512)\n[[Search]](https://www.google.com/searchbyimage?&image_url=${member.user.effectiveAvatarUrl})"
        inline = true
    }
    field {
        name = "**Created**"
        value = formatOffsetTime(member.timeCreated)
        inline = true
    }
    field {
        name = "**Joined**"
        value = formatOffsetTime(member.timeJoined)
        inline = true
    }

    if(member.roles.isNotEmpty()) {
        field {
            name = "**Roles**"
            value = member.roles.joinToString(", ") { it.name }
            inline = true
        }
    }
}
