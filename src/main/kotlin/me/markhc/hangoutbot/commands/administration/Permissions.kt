package me.markhc.hangoutbot.commands.administration

import me.jakejmattson.discordkt.arguments.*
import me.jakejmattson.discordkt.commands.GuildCommandEvent
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.dsl.*
import me.markhc.hangoutbot.services.*

fun producePermissionCommands(persistentData: PersistentData,
                              permissionsService: PermissionsService) = commands("Permissions") {
    suspend fun listPermissions(event: GuildCommandEvent<*>) {
        val commands = event.discord.commands
            .sortedBy { it.names.joinToString() }
            .groupBy { it.category }
            .toList()
            .sortedByDescending { it.second.size }

        event.respond {
            title = "Required permissions"
            description = "```css\n" +
                "[B] → Bot Owner\n" +
                "[G] → Guild Owner\n" +
                "[A] → Administrator\n" +
                "[S] → Staff\n" +
                "[E] → Everyone```"
            commands.forEach {
                field {
                    name = it.first
                    value = "```css\n${
                        it.second.map {
                            "[${permissionsService.getCommandPermissionLevel(event.guild, it).toString().first()}]\u202F${it.names.first()}"
                        }.joinToString("\n")
                    }\n```"
                    inline = true
                }
            }
        }
    }
}