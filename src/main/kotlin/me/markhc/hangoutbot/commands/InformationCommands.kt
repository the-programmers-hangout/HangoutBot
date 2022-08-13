package me.markhc.hangoutbot.commands

import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.rest.Image
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.toJavaInstant
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.arguments.UserArg
import me.jakejmattson.discordkt.commands.subcommand
import me.jakejmattson.discordkt.extensions.*

fun produceInformationCommands() = subcommand("Info") {
    sub("server") {
        description = "Display a message giving basic server information."
        execute {
            respondPublic {
                title = guild.name
                description = guild.description
                color = discord.configuration.theme
                timestamp = guild.id.timestamp

                author(guild.getOwner())
                footer("Guild creation")
                thumbnail(guild.getIconUrl(Image.Format.PNG) ?: "")

                field {
                    name = "Count"
                    value = """
                        ```
                        Users: ${guild.members.count()}
                        Roles: ${guild.roles.count()}
                        Boost: ${guild.premiumTier.value}
                        Emoji: ${guild.emojis.count()}
                        Voice: ${guild.channels.filterIsInstance<VoiceChannel>().count()}
                        Text : ${guild.channels.filterIsInstance<TextChannel>().count()}```
                    """.trimIndent()
                }

                addInlineField("Vanity URL", guild.vanityUrl ?: "None")
            }
        }
    }

    sub("user") {
        description = "Displays information about the given user."
        execute(UserArg("User", "The user to see more information about").optional { it.author }) {
            val (user) = args
            val member = guild.getMemberOrNull(user.id)

            respondPublic {
                title = "User information"
                color = discord.configuration.theme
                thumbnail(user.pfpUrl)
                addInlineField("Username", user.tag)
                addInlineField("ID", user.id.toString())
                addInlineField("Created", TimeStamp.at(user.id.timestamp.toJavaInstant(), TimeStyle.RELATIVE))

                if (member != null) {
                    addInlineField("Nickname", member.displayName)
                    addInlineField("Avatar", "[Link](${member.pfpUrl}?size=512)")
                    addInlineField("Joined", TimeStamp.at(member.joinedAt.toJavaInstant(), TimeStyle.RELATIVE))

                    addField("Roles", member.roles.toList().joinToString { it.name })
                }
            }
        }
    }

    sub("role") {
        description = "Displays information about the given role."
        execute(RoleArg("Role", "The role to see information about")) {
            val role = args.first

            respond {
                title = "Role information"
                color = role.color

                addInlineField("Name", role.name)
                addInlineField("ID", role.id.toString())
                addInlineField("Color", stringify(role.color))
                addInlineField("Hoisted", role.hoisted.toString())
                addInlineField("Managed", role.managed.toString())
                addInlineField("Mentionable", role.mentionable.toString())
                addField("Members", "${guild.members.toList().filter { role in it.roles.toList() }.size} members")
            }
        }
    }

    sub("avatar") {
        description = "Gets the avatar from the given user"
        execute(UserArg("User", "The user to see the avatar of")) {
            val user = args.first
            respond("${user.pfpUrl}?size=512")
        }
    }
}
