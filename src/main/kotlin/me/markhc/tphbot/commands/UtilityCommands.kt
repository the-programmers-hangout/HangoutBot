package me.markhc.tphbot.commands

import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.dsl.command.SingleArg
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.extensions.jda.fullName
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.arguments.RoleArg
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.aberrantfox.kjdautils.internal.arguments.TextChannelArg
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
import java.time.format.DateTimeFormatter

@CommandSet("Utility")
fun utilityCommands() = commands {
    requiredPermissionLevel = Permission.Everyone

    command("serverinfo") {
        description = "Display a message giving basic server information"
        execute {event ->
            event.guild
                    ?.let { produceServerInfoEmbed(it) }
                    ?.let { event.respond(it) }
        }
    }
}

@CommandSet("StaffUtility")
fun staffUtilityCommands() = commands {
    requiredPermissionLevel = Permission.Staff

    command("echo") {
        description = "Echo a message to a channel"
        execute(TextChannelArg("Channel").makeOptional { it.channel as TextChannel },
                SentenceArg("Message")) {

            val (target, message) = it.args

            target.sendMessage(message.sanitiseMentions()).queue()
        }
    }

    command("setcolor") {
        description = "Give yourself one of the color roles"
        execute(RoleArg("ColorRole")) {event ->
            val (role) = event.args

            event.guild?.id
                    ?.let { transaction { GuildConfiguration.findOrCreate(it) }.colorRoles }
                    ?.let { Klaxon().parse<Array<String>>(it) }
                    ?.let { roles: Array<String> -> if(role in roles) role else null  }
                    ?.apply {
                        grantRole(event, this)
                    }

        }
    }
}

private fun produceServerInfoEmbed(guild: Guild) =
        embed {
            title = guild.name
            color = Color.MAGENTA
            description = """
                The programmer's hangout is a programming server, made for persons of all skill levels, be you someone who has wrote 10 lines of code, or someone with 10 years of experience.
            """.trimIndent()
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

private fun grantRole(event: CommandEvent<SingleArg<Role>>, role: Role) {
    val guild = event.guild!!
    val member = guild.getMember(event.author)!!
    val roleName = role.name

    guild.addRoleToMember(member, role)
    event.respond("$roleName assigned to ${event.author.fullName()}")
}
