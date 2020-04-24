package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import mu.KLogger
import net.dv8tion.jda.api.entities.Member
import org.jetbrains.exposed.sql.transactions.transaction

enum class Permission {
    BotOwner,
    GuildOwner,
    Administrator,
    Staff,
    Everyone
}

val DEFAULT_REQUIRED_PERMISSION = Permission.GuildOwner

@Service
class PermissionsService(private val logger: KLogger, private val configuration: Configuration) {
    fun hasClearance(member: Member, requiredPermissionLevel: Permission): Boolean {

        logger.debug { "Required Permission Level: $requiredPermissionLevel, Member Permission Level ${member.getPermissionLevel()}" }

        return member.getPermissionLevel().ordinal <= requiredPermissionLevel.ordinal
    }
    fun getPermissionLevel(member: Member) = member.getPermissionLevel().ordinal

    private fun Member.getPermissionLevel() =
            when {
                isBotOwner() -> Permission.BotOwner
                isGuildOwner() -> Permission.GuildOwner
                isAdministrator() -> Permission.Administrator
                isStaff() -> Permission.Staff
                else -> Permission.Everyone
            }

    private fun Member.isBotOwner() = user.id == configuration.ownerId
    private fun Member.isGuildOwner() = isOwner
    private fun Member.isAdministrator() : Boolean {
        val guildConfig = transaction {
            GuildConfiguration.findOrCreate(guild.id)
        }

        val requiredRole = guildConfig.adminRoleName?.let {
            guild.getRolesByName(it, true).firstOrNull()
        } ?: return false

        return requiredRole in roles
    }
    private fun Member.isStaff(): Boolean {
        val guildConfig = transaction {
            GuildConfiguration.findOrCreate(guild.id)
        }

        val requiredRole = guildConfig.staffRoleName?.let {
            guild.getRolesByName(it, true).firstOrNull()
        } ?: return false

        return requiredRole in roles
    }
}
