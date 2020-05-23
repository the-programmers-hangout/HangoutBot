package me.markhc.hangoutbot.commands.utilities

import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.internal.arguments.*
import me.markhc.hangoutbot.commands.utilities.services.ColorService
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.PermissionLevel
import me.markhc.hangoutbot.services.PersistentData
import net.dv8tion.jda.api.entities.TextChannel
import java.awt.Color

@CommandSet("Colors")
fun colorCommands(persistentData: PersistentData,
                                colorService: ColorService) = commands {

    command("setcolor") {
        description = "Creates a role with the given name and color and assigns it to the user."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute(HexColorArg("HexColor").makeNullableOptional(), EveryArg("RoleName")) { event ->
            val (color, roleName) = event.args

            val guild = event.guild!!
            val member = guild.getMember(event.author)!!

            val message = event.channel.sendMessage("Working...").complete()

            runCatching {
                colorService.setMemberColor(member, roleName, color)
            }.onSuccess {
                message.editMessage("Successfully assigned color $roleName").queue()
            }.onFailure {
                message.editMessage(it.message!!).queue()
            }
        }
    }

    command("clearcolor") {
        description = "Clears the current color role."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute { event ->
            val member = event.guild!!.getMember(event.author)!!

            colorService.clearMemberColor(member)

            event.respond("Cleared user color")
        }
    }

    command("listcolors") {
        description = "Creates a role with the given name and color and assigns it to the user."
        requiredPermissionLevel = PermissionLevel.Staff
        requiresGuild = true
        execute { event ->
            val (colorRoles, prefix) = persistentData.getGuildProperty(event.guild!!) { assignedColorRoles to prefix }

            if (colorRoles.isEmpty()) {
                return@execute event.respond("No colors set. For more information, see `${prefix}help setcolor`")
            }

            val colorInfo = colorRoles.map {
                event.guild!!.getRoleById(it.key)
            }.filterNotNull().sortedBy {
                val rgb = it.color!!

                val hsv = Color.RGBtoHSB(rgb.red, rgb.green, rgb.blue, null)

                hsv[0]
            }.joinToString("\n") { it.asMention }


            event.respond(embed {
                title = "Currently used colors"
                description = "Run `setcolor <name>` to use one of the colors here.\n" +
                        "Run `setcolor <hexcolor> <name>` to create a new color."
                color = infoColor

                field {
                    name = "Colors"
                    value = colorInfo
                }
            })
        }
    }
}