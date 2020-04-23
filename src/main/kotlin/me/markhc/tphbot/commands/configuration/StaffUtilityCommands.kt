package me.markhc.tphbot.commands.configuration

import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.annotation.CommandSet
import me.aberrantfox.kjdautils.api.dsl.command.commands
import me.aberrantfox.kjdautils.extensions.jda.getRoleByName
import me.aberrantfox.kjdautils.extensions.stdlib.sanitiseMentions
import me.aberrantfox.kjdautils.internal.arguments.SentenceArg
import me.markhc.tphbot.arguments.MyTextChannelArg
import me.markhc.tphbot.extensions.requiredPermissionLevel
import me.markhc.tphbot.services.GuildConfiguration
import me.markhc.tphbot.services.Permission
import me.markhc.tphbot.services.findOrCreate
import net.dv8tion.jda.api.entities.TextChannel
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.StringReader

@CommandSet("StaffUtility")
fun staffUtilityCommands() = commands {
    requiredPermissionLevel = Permission.Staff

    command("Echo") {
        description = "Echo a message to a channel."
        execute(MyTextChannelArg.makeOptional { it.channel as TextChannel }, SentenceArg) {
            val (target, message) = it.args

            target.sendMessage(message.sanitiseMentions()).queue()
        }
    }
}