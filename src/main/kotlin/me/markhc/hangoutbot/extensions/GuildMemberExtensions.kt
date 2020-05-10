package me.markhc.hangoutbot.extensions

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role

fun Member.addRole(roleId: Long) =
        this.guild.getRoleById(roleId)?.let { this.guild.addRoleToMember(this, it) }
fun Member.addRole(role: Role) =
        this.guild.addRoleToMember(this, role)
fun Member.removeRole(roleId: Long) =
        this.guild.getRoleById(roleId)?.let { this.guild.removeRoleFromMember(this, it) }
fun Member.removeRole(role: Role) =
        this.guild.removeRoleFromMember(this, role)