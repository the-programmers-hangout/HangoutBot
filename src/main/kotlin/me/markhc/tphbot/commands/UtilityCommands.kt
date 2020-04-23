package me.markhc.tphbot.commands

import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.command.SingleArg
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.jda.getRoleByName
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
import me.aberrantfox.kjdautils.internal.arguments.WordArg
import me.markhc.tphbot.arguments.MyTextChannelArg
import me.markhc.tphbot.extensions.requiredPermissionLevel
import me.markhc.tphbot.services.GuildConfiguration
import me.markhc.tphbot.services.Permission
import me.markhc.tphbot.services.findOrCreate
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.StringReader
import java.time.format.DateTimeFormatter

@CommandSet("Utility")
fun utilityCommands() = commands {
    requiredPermissionLevel = Permission.Everyone

    command("ServerInfo") {
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
