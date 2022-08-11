package me.markhc.hangoutbot.conversations

import dev.kord.core.entity.Guild
import me.jakejmattson.discordkt.arguments.ChannelArg
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.RoleArg
import me.jakejmattson.discordkt.conversations.conversation
import me.markhc.hangoutbot.services.PersistentData

class ConfigurationConversation(private val persistentData: PersistentData) {
    fun createConfigurationConversation(guild: Guild) = conversation {
        val prefix = prompt(EveryArg, "Bot prefix:")
        val welcomeChannel = prompt(ChannelArg, "Welcome channel:")
        val loggingChannel = prompt(ChannelArg, "Logging channel:")
        val muteRole = prompt(RoleArg, "Mute role:")
        val softMuteRole = prompt(RoleArg, "Soft mute role:")

        persistentData.setup(guild, prefix, welcomeChannel, loggingChannel, muteRole, softMuteRole)
    }
}