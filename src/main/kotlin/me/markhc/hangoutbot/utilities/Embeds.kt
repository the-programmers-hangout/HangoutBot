package me.markhc.hangoutbot.utilities

import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.api.dsl.command.CommandsContainer
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import java.awt.Color
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

fun buildServerInfoEmbed(guild: Guild) =
        embed {
            title = guild.name
            color = Color.MAGENTA
            footer {
                text = "Guild creation date: ${guild.timeCreated.format(DateTimeFormatter.RFC_1123_DATE_TIME)}"
                iconUrl = "http://i.imgur.com/iwwEprG.png"
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
                value = guild.owner?.fullName() ?: "<None>"
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
                value = "${guild.emotes.size}/${guild.maxEmotes}"
                inline = true
            }
            field {
                name = "**Invite URL**"
                value = guild.vanityCode ?: "Not set"
                inline = true
            }
            field {
                name = "**Boosts**"
                value = guild.boostCount.toString()
                inline = true
            }
        }

fun buildSelfMuteEmbed(member: Member, duration: Long) = embed {
    title = "You have been muted"
    description = "You have been muted as a result of invoking the selfmute command. " +
            "This mute will be automatically removed when the time expires."
    field {
        inline = true
        name = "Duration"
        value = toTimeString(duration)
    }

    field {
        inline = true
        name = "You will be unmuted on"
        value = DateTime.now(DateTimeZone.UTC).plus(duration).toString(DateTimeFormat.fullDateTime())
    }

    thumbnail = member.user.avatarUrl
}

fun buildRoleInfoEmbed(role: Role) = embed {
    title = "Role information"
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
        value = if(role.color != null) "**rgba(${role.color!!.red}, ${role.color!!.green}, ${role.color!!.blue}, ${role.color!!.alpha})**" else "None"
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
}

fun buildUserInfoEmbed(user: User) = embed {
    title = "User information"

    val createdTime = DateTime(user.timeCreated.toInstant().toEpochMilli(), DateTimeZone.UTC)

    field {
        name = "**Username**"
        value = user.fullName()
        inline = true
    }
    field {
        name = "**Avatar**"
        value = "[Link](${user.effectiveAvatarUrl}?size=512)"
        inline = true
    }
    field {
        name = "**Id**"
        value = user.id
        inline = true
    }
    field {
        name = "**Creation Time**"
        value = createdTime.toString(DateTimeFormat.shortDateTime())
        inline = true
    }
}

fun buildMemberInfoEmbed(member: Member) = embed {
    title = "User information"

    val createdTime = DateTime(member.timeCreated.toInstant().toEpochMilli(), DateTimeZone.UTC)
    val joinedTime = DateTime(member.timeJoined.toInstant().toEpochMilli(), DateTimeZone.UTC)

    field {
        name = "**Username**"
        value = member.fullName()
        inline = true
    }
    field {
        name = "**Nickname**"
        value = member.nickname ?: "Not set"
        inline = true
    }
    field {
        name = "**Avatar**"
        value = "[Link](${member.user.effectiveAvatarUrl}?size=512)"
        inline = true
    }
    field {
        name = "**Id**"
        value = member.id
        inline = true
    }
    field {
        name = "**Creation Time**"
        value = createdTime.toString(DateTimeFormat.shortDateTime())
        inline = true
    }
    field {
        name = "**Join Time**"
        value = joinedTime.toString(DateTimeFormat.shortDateTime())
        inline = true
    }
    field {
        name = "**Roles**"
        value = member.roles.joinToString("\n") { it.name }
        inline = true
    }
}

fun buildHelpEmbed(prefix: String, container: CommandsContainer) = embed {

    title = "Help information"
    description = "Use `${prefix}help <command>` for more information"
    color = Color.green

    fun joinNames(value: List<Command>) =
            value.sortedBy { it.names.joinToString() }.joinToString("\n") { it.names.joinToString() }

    container.commands
            .groupBy { it.category }
            .map {(category, commands) ->
                when {
                    commands.size >= 6 -> { // Split into 3 columns
                        val n = ceil(commands.size / 3.0).toInt()
                        field {
                            name = "**$category**"
                            value = joinNames(commands.subList(0, n))
                            inline = true
                        }
                        field {
                            value = joinNames(commands.subList(n, n * 2))
                            inline = true
                        }
                        field {
                            value = joinNames(commands.subList(n * 2, commands.size))
                            inline = true
                        }
                    }
                    else -> {
                        field {
                            name = "**$category**"
                            value = joinNames(commands)
                            inline = true
                        }
                        field { inline = true }
                        field { inline = true }
                    }
                }
            }
}

private fun generateStructure(command: Command) =
        command.expectedArgs.arguments.joinToString(" ") {
            val type = it.name
            if (it.isOptional) "($type)" else "[$type]"
        }

private fun generateExample(command: Command) =
        command.expectedArgs.arguments.joinToString(" ") {
            it.examples.random()
        }

fun buildHelpEmbedForCommand(prefix: String, command: Command) = embed {
    title = command.names.joinToString()
    description = command.description
    color = Color.green

    val commandInvocation = "${prefix}${command.names.first()}"

    field {
        name = "What is the structure of the command?"
        value = "$commandInvocation ${generateStructure(command)}"
    }

    field {
        name = "Show me an example of someone using the command."
        value = "$commandInvocation ${generateExample(command)}"
    }

}