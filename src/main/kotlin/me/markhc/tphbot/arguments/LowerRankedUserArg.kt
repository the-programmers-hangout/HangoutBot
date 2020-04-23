package me.markhc.tphbot.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.extensions.jda.getHighestRole
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.tryRetrieveSnowflake
import me.markhc.tphbot.services.GuildConfiguration
import me.markhc.tphbot.services.Permission
import me.markhc.tphbot.services.findOrCreate
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.transaction

open class LowerRankedUserArg(override val name : String = "Lower Ranked member") : ArgumentType<User>() {
    companion object : LowerRankedUserArg()

    override val consumptionType = ConsumptionType.Single
    override val examples: ArrayList<String>
        get() = arrayListOf("@Bob", "197780697866305536", "302134543639511050")

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<User> {
        val retrieved = tryRetrieveSnowflake(event.discord.jda) {
            event.guild?.getMemberById(arg.trimToID())
        } as Member? ?: return ArgumentResult.Error("Couldn't retrieve member: $arg")

        return when {
            compareMembers(retrieved, event.guild!!.getMember(event.author)!!) >= 0
                -> ArgumentResult.Error("You don't have the permission to use this command on the target user.")
            else -> ArgumentResult.Success(retrieved.user)
        }
    }
}

private fun compareMembers(a: Member, b: Member) = getPermissionLevel(a).compareTo(getPermissionLevel(b))
private fun getPermissionLevel(member: Member): Permission {
    if(member.roles.isEmpty()) return Permission.Everyone
    val guildConfig = transaction {
        GuildConfiguration.findOrCreate(member.guild.id)
    }

    if(member.isOwner) return Permission.GuildOwner
    if(member.roles.any { guildConfig.adminRoleName == it.name }) return Permission.Administrator
    if(member.roles.any { guildConfig.staffRoleName == it.name }) return Permission.Staff
    return Permission.Everyone
}
