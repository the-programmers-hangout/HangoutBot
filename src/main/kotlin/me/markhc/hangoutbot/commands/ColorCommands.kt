package me.markhc.hangoutbot.commands

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.subcommand
import me.jakejmattson.discordkt.extensions.stringify
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.services.ColorService
import java.awt.Color

fun colorCommands(configuration: Configuration, colorService: ColorService) = subcommand("colors", Permissions(Permission.ManageMessages)) {
    sub("create", "Create a color role.") {
        execute(AnyArg("Name"), HexColorArg) {
            val (name, color) = args
            val member = getMember()!!
            val existingRole = colorService.findRole(name, color, guild)

            if (existingRole != null) {
                respond("Similar role already exists: ${existingRole.name} - ${stringify(existingRole.color)}")
            } else {
                val newRole = colorService.createRole(name, color, guild)
                colorService.setMemberColor(member, newRole)
                respondPublic("Role created: ${newRole.mention}")
            }
        }
    }

    sub("apply", "Apply a color role.") {
        execute(RoleArg) {
            val role = args.first
            val member = author.asMember(guild.id)
            val wasApplied = colorService.setMemberColor(member, role)

            if (wasApplied)
                respondPublic("Color applied: ${role.name}")
            else
                respond("Not a valid color role.")
        }
    }

    sub("clear", "Clears your color role.") {
        execute {
            val member = author.asMember(guild.id)
            colorService.removeColorRole(member)
            respond("Cleared user color")
        }
    }

    sub("list", "List all color roles.") {
        execute {
            val colorRoles = configuration[guild].assignedColorRoles.keys

            if (colorRoles.isEmpty()) {
                respond("No color roles")
                return@execute
            }

            val colorInfo = colorRoles.map {
                it.let { guild.getRole(it) }
            }.sortedBy {
                val rgb = it.color
                val hsv = Color.RGBtoHSB(rgb.red, rgb.green, rgb.blue, null)
                hsv[0]
            }.joinToString("\n") { it.mention }

            respond {
                title = "Currently used colors"
                description = "Run `/create color <name> <color>` to create some"
                color = discord.configuration.theme

                field {
                    name = "Colors"
                    value = colorInfo
                }
            }
        }
    }
}