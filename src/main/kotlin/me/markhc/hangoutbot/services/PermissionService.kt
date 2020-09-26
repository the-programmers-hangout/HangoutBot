package me.markhc.hangoutbot.services

import com.gitlab.kordlib.core.entity.*
import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.*
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.locale.Messages

enum class PermissionLevel {
    Everyone,
    Staff,
    Administrator,
    GuildOwner,
    BotOwner
}

val DEFAULT_REQUIRED_PERMISSION = PermissionLevel.Everyone

@Service
class PermissionsService(private val persistentData: PersistentData, private val botConfig: BotConfiguration) {
    suspend fun getCommandPermissionLevel(guild: Guild, command: Command): PermissionLevel {
        return persistentData.getGuildProperty(guild) {
                commandPermission[command.names.first()]
            } ?: command.requiredPermissionLevel
    }

    suspend fun trySetCommandPermission(guild: Guild, invokingUser: User, command: Command, level: PermissionLevel): Boolean {
        val member = invokingUser.asMember(guild.id)
        val cmdPerms = getCommandPermissionLevel(guild, command)
        val authorPerms = getPermissionLevel(member)

        return if (cmdPerms > authorPerms) {
            false
        } else {
            persistentData.setGuildProperty(guild) {
                commandPermission[command.names.first()] = level
            }

            true
        }
    }

    suspend fun hasClearance(guild: Guild?, user: User, requiredPermissionLevel: PermissionLevel): Boolean {
        val permissionLevel = guild?.getMember(user.id)?.let { getPermissionLevel(it) }

        return if (permissionLevel == null) {
            requiredPermissionLevel == PermissionLevel.Everyone || user.id.value == botConfig.ownerId
        } else {
            permissionLevel >= requiredPermissionLevel
        }
    }

    suspend fun isCommandVisible(guild: Guild, user: User, command: Command) =
        hasClearance(guild, user, getCommandPermissionLevel(guild, command))

    suspend fun hasPermission(member: Member, level: PermissionLevel) = getPermissionLevel(member) >= level

    suspend fun getPermissionLevel(member: Member) =
        when {
            member.isBotOwner() -> PermissionLevel.BotOwner
            member.isGuildOwner() -> PermissionLevel.GuildOwner
            member.isAdministrator() -> PermissionLevel.Administrator
            member.isStaff() -> PermissionLevel.Staff
            else -> PermissionLevel.Everyone
        }

    private fun Member.isBotOwner() = id.value == botConfig.ownerId
    private suspend fun Member.isGuildOwner() = isOwner()
    private suspend fun Member.isAdministrator(): Boolean {
        val roles = persistentData.getGuildProperty(guild.asGuild()) { rolePermissions }
        val adminRoles = roles
            .filter { it.value == PermissionLevel.Administrator }
            .map { it.key }
        val userRoles = this.roles.map { it.id }

        return adminRoles.intersect(userRoles).isNotEmpty()
    }

    private fun Member.isStaff(): Boolean {
        val roles = persistentData.getGuildProperty(guild) { rolePermissions }
        val staffRoles = roles
            .filter { it.value == PermissionLevel.Staff }
            .map { it.key }
        val userRoles = this.roles.map { it.id }

        return staffRoles.intersect(userRoles).isNotEmpty()
    }
}

val commandPermissions: MutableMap<Command, PermissionLevel> = mutableMapOf()

var Command.requiredPermissionLevel: PermissionLevel
    get() = commandPermissions[this] ?: DEFAULT_REQUIRED_PERMISSION
    set(value) {
        commandPermissions[this] = value
    }

fun CommandEvent<*>.requiresPermission(level: PermissionLevel, action: suspend CommandEvent<*>.() -> Unit) {
    val svc = this.discord.getInjectionObjects(PermissionsService::class)

    if (svc.hasClearance(this.guild, this.author, level)) {
        action(this)
    } else {
        respond(Messages.INSUFFICIENT_PERMS)
    }
}