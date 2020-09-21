package me.markhc.hangoutbot.services

import me.jakejmattson.discordkt.api.annotations.Service
import me.jakejmattson.discordkt.api.dsl.command.Command
import me.jakejmattson.discordkt.api.dsl.command.CommandEvent
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.locale.Messages
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

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
    fun getCommandPermissionLevel(guild: Guild?, command: Command): PermissionLevel {
        return guild?.let {
            persistentData.getGuildProperty(it) {
                commandPermission[command.names.first()]
            }
        } ?: command.requiredPermissionLevel
    }

    fun trySetCommandPermission(guild: Guild, invokingUser: User, command: Command, level: PermissionLevel): Boolean {
        val member = guild.getMember(invokingUser)!!

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

    fun hasClearance(guild: Guild?, user: User, requiredPermissionLevel: PermissionLevel): Boolean {
        val permissionLevel = guild?.getMember(user)?.let { getPermissionLevel(it) }

        return if(permissionLevel == null) {
            requiredPermissionLevel == PermissionLevel.Everyone || user.id == botConfig.ownerId
        } else {
            permissionLevel >= requiredPermissionLevel
        }
    }

    fun isCommandVisible(guild: Guild?, user: User, command: Command) =
            hasClearance(guild, user, getCommandPermissionLevel(guild, command))

    fun hasPermission(member: Member, level: PermissionLevel)
            = getPermissionLevel(member) >= level

    fun getPermissionLevel(member: Member) =
            when {
                member.isBotOwner() -> PermissionLevel.BotOwner
                member.isGuildOwner() -> PermissionLevel.GuildOwner
                member.isAdministrator() -> PermissionLevel.Administrator
                member.isStaff() -> PermissionLevel.Staff
                else -> PermissionLevel.Everyone
            }

    private fun Member.isBotOwner() = user.id == botConfig.ownerId
    private fun Member.isGuildOwner() = isOwner
    private fun Member.isAdministrator() : Boolean {
        val roles = persistentData.getGuildProperty(guild) { rolePermissions }
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

fun CommandEvent<*>.requiresPermission(level: PermissionLevel, action: CommandEvent<*>.() -> Unit) {
    val svc = this.discord.getInjectionObjects(PermissionsService::class)

    if(svc.hasClearance(this.guild, this.author, level)) {
        action(this)
    } else {
        respond(Messages.INSUFFICIENT_PERMS)
    }
}