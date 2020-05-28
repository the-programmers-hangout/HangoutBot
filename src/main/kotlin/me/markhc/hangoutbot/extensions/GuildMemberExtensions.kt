package me.markhc.hangoutbot.extensions

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role

fun Member.addRole(role: Role) =
        this.guild.addRoleToMember(this, role)
fun Member.removeRole(role: Role) =
        this.guild.removeRoleFromMember(this, role)