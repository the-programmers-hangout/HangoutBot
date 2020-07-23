package me.markhc.hangoutbot

import com.google.gson.Gson
import me.jakejmattson.kutils.api.dsl.bot
import me.markhc.hangoutbot.commands.administration.services.ScriptEngineService
import me.markhc.hangoutbot.dataclasses.Properties
import me.markhc.hangoutbot.dataclasses.loadConfig
import me.markhc.hangoutbot.utilities.toLongDurationString
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import java.awt.Color
import java.util.*

fun main() {
    println(3600.toLong().toLongDurationString())
    loadConfig {
        val configuration = it ?: throw Exception("Failed to parse configuration")
        val propFile = Properties::class.java.getResource("/hangoutbot_properties.json").readText()
        val properties = Gson().fromJson(propFile, Properties::class.java)
                ?: throw Exception("Failed to parse properties")

        bot(configuration.token) {
            client { token ->
                JDABuilder.createDefault(token)
                        .setChunkingFilter(ChunkingFilter.ALL)
                        .enableIntents(EnumSet.allOf(GatewayIntent::class.java))
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
            }

            injection {
                inject(configuration)
                inject(configuration)
                inject(properties)
            }

            configure {
                colors {
                    infoColor = Color.CYAN
                    failureColor = Color.RED
                    successColor = Color.GREEN
                }
                commandReaction = null
                allowMentionPrefix = true
                requiresGuild = false
            }

            logging {
                generateCommandDocs = false
            }
        }
    }
}