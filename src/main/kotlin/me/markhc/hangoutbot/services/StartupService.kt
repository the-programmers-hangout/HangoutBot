package me.markhc.hangoutbot.services

import me.jakejmattson.kutils.api.Discord
import me.jakejmattson.kutils.api.annotations.Service
import me.jakejmattson.kutils.api.extensions.jda.fullName
import me.jakejmattson.kutils.api.services.ScriptEngineService
import me.markhc.hangoutbot.dataclasses.BotConfiguration
import me.markhc.hangoutbot.dataclasses.Properties
import me.markhc.hangoutbot.modules.utilities.services.MuteService
import me.markhc.hangoutbot.modules.utilities.services.ReminderService
import kotlin.concurrent.thread

@Service
class StartupService(private val properties: Properties,
                     private val config: BotConfiguration,
                     private val botStats: BotStatsService,
                     private val discord: Discord,
                     private val permissionsService: PermissionsService,
                     private val persistentData: PersistentData,
                     private val muteService: MuteService,
                     private val reminderService: ReminderService,
                     private val scriptingEngine: ScriptEngineService) {
    init {
        muteService.launchTimers()
        reminderService.launchTimers()

        thread(start = true, isDaemon = true) {
            while(true) {
                var input = readLine()
                while(input?.endsWith("$$") == false) {
                    input +=  '\n' + (readLine() ?: "")
                }
                input = input?.dropLast(2)

                try {
                    scriptingEngine.engine.eval(
                        """
                        val commands = bindings["commandContainer"]
                        val discord = bindings["discord"] as me.jakejmattson.kutils.api.Discord
                        val jda = discord.jda
                        
                        fun evalScript() {
                            $input
                        } 
                        
                        evalScript();
                        """.trimIndent()
                    )
                } catch(e: Exception) {
                    System.err.print(e.message ?: "An exception occurred in the scripting engine.")
                }
            }
        }

        with(discord.configuration) {
            prefix {
                if(it.guild == null)
                    config.prefix
                else
                    persistentData.getGuildProperty(it.guild!!) { prefix }
            }
            mentionEmbed {
                val channel = it.channel
                val self = channel.jda.selfUser

                color = infoColor
                thumbnail = self.effectiveAvatarUrl

                field {
                    name = self.fullName()
                    value = "A bot to manage utility commands and functionality that does not warrant its own bot"
                }
                field {
                    name = "Prefix"
                    value = if(it.guild == null)
                        config.prefix
                    else
                        persistentData.getGuildProperty(it.guild!!) { prefix }
                    inline = true
                }
                field {
                    name = "Contributors"
                    value = "markhc#8366"
                    inline = true
                }

                with (properties) {
                    val kotlinVersion = KotlinVersion.CURRENT

                    field {
                        name = "Build Info"
                        value = "```"+
                                "Version: $version\n" +
                                "KUtils:  $kutils\n" +
                                "Kotlin:  $kotlinVersion" +
                                "```"
                    }

                    field {
                        name = "Uptime"
                        value = botStats.uptime
                    }

                    field {
                        name = "Source"
                        value = "[[GitHub]](${repository})"
                    }
                }
            }

            visibilityPredicate predicate@{
                return@predicate if(it.guild == null && it.command.requiresGuild ?: discord.configuration.requiresGuild) {
                    false
                } else {
                    permissionsService.isCommandVisible(it.guild, it.user, it.command)
                }
            }
        }
    }
}