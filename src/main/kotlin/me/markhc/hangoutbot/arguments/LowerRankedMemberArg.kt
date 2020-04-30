package me.markhc.hangoutbot.arguments

import me.aberrantfox.kjdautils.api.dsl.command.CommandEvent
import me.aberrantfox.kjdautils.api.getInjectionObject
import me.aberrantfox.kjdautils.extensions.stdlib.trimToID
import me.aberrantfox.kjdautils.internal.command.ArgumentResult
import me.aberrantfox.kjdautils.internal.command.ArgumentType
import me.aberrantfox.kjdautils.internal.command.ConsumptionType
import me.aberrantfox.kjdautils.internal.command.tryRetrieveSnowflake
import me.markhc.hangoutbot.services.PermissionsService
import net.dv8tion.jda.api.entities.Member

open class LowerRankedMemberArg(override val name : String = "Lower Ranked member") : ArgumentType<Member>() {
    companion object : LowerRankedMemberArg()

    override val consumptionType = ConsumptionType.Single
    override fun generateExamples(event: CommandEvent<*>)
            = mutableListOf("@Bob", "197780697866305536", "302134543639511050")

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val permissions = event.discord.getInjectionObject<PermissionsService>()!!
        val retrieved = tryRetrieveSnowflake(event.discord.jda) {
            event.guild?.getMemberById(arg.trimToID())
        } as Member? ?: return ArgumentResult.Error("Couldn't retrieve member: $arg")

        val author = event.guild!!.getMember(event.author)!!

        return when {
            author.isHigherRankedThan(permissions, retrieved)
                -> ArgumentResult.Error("You don't have the permission to use this command on the target user.")
            else -> ArgumentResult.Success(retrieved)
        }
    }
}

private fun Member.isHigherRankedThan(permissions: PermissionsService, b: Member) =
        permissions.getPermissionLevel(this) > permissions.getPermissionLevel(b)