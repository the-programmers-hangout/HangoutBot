package me.markhc.hangoutbot.preconditions

import dev.kord.common.entity.Permission
import me.jakejmattson.discordkt.dsl.precondition
import me.markhc.hangoutbot.dataclasses.Configuration
import kotlin.collections.set

val cooldownMap = mutableMapOf<Long, Long>()

fun cooldown(configuration: Configuration) = precondition {
    command ?: return@precondition

    val member = guild?.getMember(author.id)

    if (member != null && member.getPermissions().contains(Permission.ManageMessages))
        return@precondition

    val cd = guild?.let {
        configuration[it].cooldown
    } ?: 5

    if (cooldownMap[author.id.value.toLong()] != null) {
        val diff = System.currentTimeMillis() - cooldownMap[author.id.value.toLong()]!!

        if (diff < cd * 1000) {
            return@precondition fail("You're doing that too quickly. (${String.format("%.2f", (cd * 1000 - diff) / 1000.0f)} s)")
        }
    }

    cooldownMap[author.id.value.toLong()] = System.currentTimeMillis()

    return@precondition

}