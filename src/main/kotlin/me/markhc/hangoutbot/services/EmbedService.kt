package me.markhc.hangoutbot.services

import me.jakejmattson.discordkt.api.dsl.embed.embed
import me.jakejmattson.discordkt.api.extensions.jda.fullName
import me.markhc.hangoutbot.dataclasses.Configuration
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import org.joda.time.DateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class EmbedService(private val botStats: BotStatsService, private val config: Configuration) {
    fun botStats() = embed {
        simpleTitle = "Stats"
        color = infoColor

        field {
            name = "Commands"
            value = """
                        ```
                        Commands executed:      ${String.format("%6d", config.totalCommandsExecuted)}
                        Commands since restart: ${String.format("%6d", botStats.totalCommands)}
                        Average execution time: ${String.format("%6.1f", botStats.avgResponseTime)} ms
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

    fun debugStats() = embed {
        simpleTitle = "Debug"
        color = infoColor

        field {
            name = "Command times"
            value = "```\n" + botStats.avgCommandTimes.toList()
                    .sortedBy { it.second }
                    .joinToString("\n") {
                        "${it.first.padEnd(15)} ${it.second.toString().padStart(8)} ms"
                    } + "\n```";
        }
    }

    fun guildInfo(guild: Guild) = embed {
        simpleTitle = guild.name
        color = infoColor

        footer {
            text = "Guild creation date: ${guild.timeCreated.format(DateTimeFormatter.RFC_1123_DATE_TIME)}"
        }
        thumbnail = guild.iconUrl ?: ""

        if (guild.description != null) {
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
            value = "${guild.emotes.size}/${guild.maxEmotes * 2}"
            inline = true
        }
        field {
            name = "**Invite URL**"
            value = if (guild.vanityUrl != null) "[Link](${guild.vanityUrl})" else "Not set"
            inline = true
        }
        field {
            name = "**Boosts**"
            value = "${guild.boostCount} (Tier: ${guild.boostTier.ordinal})"
            inline = true
        }
    }

    fun roleInfo(role: Role) = embed {
        simpleTitle = "Role information"
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
            value = if (role.color != null) "**rgb(${role.color!!.red}, ${role.color!!.green}, ${role.color!!.blue})**" else "None"
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

    fun userInfo(user: User) = embed {
        simpleTitle = "User information"
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

    fun memberInfo(member: Member) = embed {
        simpleTitle = "User information"
        color = member.color
        thumbnail = member.user.effectiveAvatarUrl

        field {
            name = "**Username**"
            value = member.user.fullName()
            inline = true
        }
        field {
            name = "**Nickname**"
            value = member.nickname?.take(25) ?: "--"
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

        if (member.roles.isNotEmpty()) {
            field {
                name = "**Roles**"
                value = member.roles.joinToString(", ") { it.name }
                inline = true
            }
        }
    }

    private fun formatOffsetTime(time: OffsetDateTime): String {
        val days = TimeUnit.MILLISECONDS.toDays(DateTime.now().millis - time.toInstant().toEpochMilli())
        return if (days > 4) {
            "$days days ago\n${time.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        } else {
            val hours = TimeUnit.MILLISECONDS.toHours(DateTime.now().millis - time.toInstant().toEpochMilli())
            "$hours hours ago\n${time.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        }
    }
}