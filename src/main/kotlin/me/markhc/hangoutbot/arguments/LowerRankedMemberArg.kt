package me.markhc.hangoutbot.arguments

import me.jakejmattson.kutils.api.dsl.arguments.ArgumentResult
import me.jakejmattson.kutils.api.dsl.arguments.ArgumentType
import me.jakejmattson.kutils.api.dsl.command.CommandEvent
import me.jakejmattson.kutils.api.extensions.jda.tryRetrieveSnowflake
import me.jakejmattson.kutils.api.extensions.stdlib.trimToID
import me.markhc.hangoutbot.services.PermissionsService
import net.dv8tion.jda.api.entities.Member

open class LowerRankedMemberArg(override val name : String = "Lower Ranked member") : ArgumentType<Member>() {
    companion object : LowerRankedMemberArg()

    override fun generateExamples(event: CommandEvent<*>)
            = mutableListOf("@Bob", "197780697866305536", "302134543639511050")

    override fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Member> {
        val permissions = event.discord.getInjectionObjects(PermissionsService::class)
        val retrieved = event.discord.jda.tryRetrieveSnowflake {
            event.guild?.getMemberById(arg.trimToID())
        } as Member? ?: return ArgumentResult.Error("Couldn't retrieve member: $arg")

        val author = event.guild!!.getMember(event.author)!!

        return when {
            author.isHigherRankedThan(permissions, retrieved)
                -> ArgumentResult.Error("You don't have the permission to use this command on the target user.")
            else -> ArgumentResult.Success(retrieved, 1)
        }
    }
}

private fun Member.isHigherRankedThan(permissions: PermissionsService, b: Member) =
        permissions.getPermissionLevel(this) > permissions.getPermissionLevel(b)