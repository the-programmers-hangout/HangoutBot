package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

enum class Permission {
    BotOwner,
    GuildOwner,
    Administrator,
    Staff,
    Everyone
}

val DEFAULT_REQUIRED_PERMISSION = Permission.Everyone

@Service
class PermissionsService(private val botConfig: BotConfiguration, private val config: Configuration) {
    fun hasClearance(member: Member, requiredPermissionLevel: Permission) =
            member.getPermissionLevel().ordinal <= requiredPermissionLevel.ordinal
    fun getPermissionLevel(member: Member) =
            member.getPermissionLevel().ordinal

    fun isCommandVisible(guild: Guild?, user: User, command: Command): Boolean {
        guild ?: return false

        val member = user.toMember(guild)!!
        val permission = command.requiredPermissionLevel

        return hasClearance(member, permission)
    }

    private fun Member.getPermissionLevel() =
            when {
                isBotOwner() -> Permission.BotOwner
                isGuildOwner() -> Permission.GuildOwner
                isAdministrator() -> Permission.Administrator
                isStaff() -> Permission.Staff
                else -> Permission.Everyone
            }

    private fun Member.isBotOwner() = user.id == botConfig.ownerId
    private fun Member.isGuildOwner() = isOwner
    private fun Member.isAdministrator() : Boolean {
        val guildConfig = config.getGuildConfig(this.guild.id)

        if(guildConfig.adminRole.isEmpty()) return false

        val requiredRole = guild.getRoleById(guildConfig.adminRole)
                ?: return false

        return requiredRole in roles
    }
    private fun Member.isStaff(): Boolean {
        val guildConfig = config.getGuildConfig(this.guild.id)

        if(guildConfig.staffRole.isEmpty()) return false

        val requiredRole = guild.getRoleById(guildConfig.staffRole)
                ?: return false

        return requiredRole in roles
    }
}
