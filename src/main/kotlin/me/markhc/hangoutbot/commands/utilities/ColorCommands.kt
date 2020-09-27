package me.markhc.hangoutbot.commands.utilities

import com.gitlab.kordlib.core.behavior.edit
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.commands
import me.jakejmattson.discordkt.api.extensions.*
import me.markhc.hangoutbot.commands.utilities.services.ColorService
import me.markhc.hangoutbot.services.*

import java.awt.Color

fun colorCommands(persistentData: PersistentData, colorService: ColorService) = commands("Colors") {
    guildCommand("setcolor") {
        description = "Creates a role with the given name and color and assigns it to the user."
        requiredPermissionLevel = PermissionLevel.Staff
        execute(HexColorArg("HexColor").makeNullableOptional(), EveryArg("RoleName")) {
            val (color, roleName) = args

            val guild = guild!!
            val member = author.asMember(guild.id)
            val message = channel.createMessage("Working...")

            runCatching {
                colorService.setMemberColor(member, roleName, color)
            }.onSuccess {
                message.edit {
                    content = "Successfully assigned color $roleName"
                }
            }.onFailure {
                message.edit {
                    content = it.message!!
                }
            }
        }
    }

    guildCommand("clearcolor") {
        description = "Clears the current color role."
        requiredPermissionLevel = PermissionLevel.Staff
        execute {
            val member = author.asMember(guild.id)
            colorService.clearMemberColor(member)
            respond("Cleared user color")
        }
    }

    guildCommand("listcolors") {
        description = "Creates a role with the given name and color and assigns it to the user."
        requiredPermissionLevel = PermissionLevel.Staff
        execute {
            val (colorRoles, prefix) = persistentData.getGuildProperty(guild) { assignedColorRoles to prefix }

            if (colorRoles.isEmpty()) {
                respond("No colors set. For more information, see `${prefix}help setcolor`")
                return@execute
            }

            val colorInfo = colorRoles.map {
                it.key.toSnowflakeOrNull()?.let { guild.getRole(it) }
            }.filterNotNull().sortedBy {
                val rgb = it.color
                val hsv = Color.RGBtoHSB(rgb.red, rgb.green, rgb.blue, null)

                hsv[0]
            }.joinToString("\n") { it.mention }

            respond {
                title = "Currently used colors"
                description = "Run `setcolor <name>` to use one of the colors here.\n" +
                    "Run `setcolor <hexcolor> <name>` to create a new color."
                color = discord.configuration.theme

                field {
                    name = "Colors"
                    value = colorInfo
                }
            }
        }
    }
}