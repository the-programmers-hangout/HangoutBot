package me.markhc.tphbot.services

import com.beust.klaxon.Klaxon
import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.embed
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.jda.fullName
import java.awt.Color

@Service
class StartupService(configuration: Configuration,
                     discord: Discord
) {
    private data class Properties(val version: String, val kutils: String, val repository: String)
    private val propFile = Properties::class.java.getResource("/properties.json").readText()
    private val project = Klaxon().parse<Properties>(propFile) ?: throw Exception("Failed to parse properties");

    init {
        with(discord.configuration) {
            mentionEmbed = {
                embed {
                    val channel = it.channel
                    val self = channel.jda.selfUser

                    color = Color(0x00bfff)
                    thumbnail = self.effectiveAvatarUrl
                    addField(self.fullName(), "It's a bot!")
                    GuildConfiguration.findGuild(it.guild?.id) {
                        addInlineField(
                                "Prefix",
                                if(it != null)
                                    it[GuildConfiguration.prefix]
                                else "++")
                    }

                    with (project) {
                        val kotlinVersion = kotlin.KotlinVersion.CURRENT

                        addField("Build Info", "```" +
                                "Version: $version\n" +
                                "KUtils: $kutils\n" +
                                "Kotlin: $kotlinVersion" +
                                "```")

                        addField("Source", repository)
                    }
                }
            }
        }
    }
}