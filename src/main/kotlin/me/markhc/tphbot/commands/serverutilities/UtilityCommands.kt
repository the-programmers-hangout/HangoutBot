package me.markhc.tphbot.commands.serverutilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.markhc.tphbot.extensions.requiredPermissionLevel
import me.markhc.tphbot.services.Permission
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Guild
import java.awt.Color
import java.time.format.DateTimeFormatter

@CommandSet("Utility")
fun utilityCommands() = commands {
    requiredPermissionLevel = Permission.Everyone

    command("ping") {
        description = "Display a message giving basic server information"
        execute {
            it.respond("<:ping_pang:702977946050101278>")
        }
    }

    command("serverinfo") {
        description = "Display a message giving basic server information"
        execute {event ->
            event.guild
                    ?.let { produceServerInfoEmbed(it) }
                    ?.let { event.respond(it) }
        }
    }
}

private fun produceServerInfoEmbed(guild: Guild) =
        embed {
            title = guild.name
            color = Color.MAGENTA
            description = "The programmer's hangout is a programming server, made for persons of all skill levels, be you someone who has wrote 10 lines of code, or someone with 10 years of experience."
            footer {
                text = "Guild creation date: ${guild.timeCreated.format(DateTimeFormatter.RFC_1123_DATE_TIME)}"
                iconUrl = "http://i.imgur.com/iwwEprG.png"
            }
            thumbnail = "http://i.imgur.com/DFoaG7k.png"

            addField(name = "Users", value = "${guild.members.filter { it.onlineStatus != OnlineStatus.OFFLINE }.size}/${guild.members.size}")

            addInlineField(name = "Total Roles", value = guild.roles.size.toString())
            addInlineField(name = "Owner", value = guild.owner?.fullName() ?: "<None>")
            addInlineField(name = "Region", value = guild.region.toString())
            addInlineField(name = "Text Channels", value = guild.textChannelCache.size().toString())
            addInlineField(name = "Voice Channels", value = guild.voiceChannels.size.toString())
        }
