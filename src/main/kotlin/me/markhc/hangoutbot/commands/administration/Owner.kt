package me.markhc.hangoutbot.commands.administration

import me.jakejmattson.discordkt.api.GenericContainer
import me.jakejmattson.discordkt.api.arguments.*
import me.jakejmattson.discordkt.api.dsl.*
import me.markhc.hangoutbot.commands.administration.services.ScriptEngineService
import me.markhc.hangoutbot.services.*
import me.markhc.hangoutbot.utilities.executeLogged
import javax.script.*

fun ownerCommands(persistentData: PersistentData, scriptEngineService: ScriptEngineService) = commands("Owner Commands") {
    command("cooldown") {
        description = "Gets or sets the cooldown (in seconds) after a user executes a command before he is able to executeLogged another."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        executeLogged(IntegerArg.makeNullableOptional(null)) {
            val (cd) = args

            if (cd != null) {
                if (cd < 1) {
                    return@executeLogged respond("Cooldown cannot be less than 1 second!")
                }
                if (cd > 3600) {
                    return@executeLogged respond("Cooldown cannot be more than 1 hour!")
                }

                persistentData.getGuildProperty(guild!!) {
                    cooldown = cd.toInt()
                }

                respond("Command cooldown set to $cd seconds")
            } else {
                val value = persistentData.getGuildProperty(guild!!) {
                    cooldown
                }
                respond("Command cooldown is $value seconds")
            }
        }
    }

    command("setprefix") {
        description = "Sets the bot prefix."
        requiredPermissionLevel = PermissionLevel.GuildOwner
        requiresGuild = true
        executeLogged(AnyArg("Prefix")) {
            val newPrefix = args.first

            persistentData.setGuildProperty(guild!!) {
                prefix = newPrefix
            }

            respond("Bot prefix set to **$newPrefix**")
        }
    }

    command("eval") {
        description = "Evaluates a script"
        requiredPermissionLevel = PermissionLevel.BotOwner
        requiresGuild = true
        executeLogged(EveryArg) {
            evalCommand(scriptEngineService.engine, this)
        }
    }
}

suspend fun <T : GenericContainer> evalCommand(
    engine: ScriptEngine,
    commandEvent: CommandEvent<T>) {

    val input = commandEvent.message.content
        .removePrefix(commandEvent.prefix())
        .removePrefix(commandEvent.rawInputs.commandName)

    try {
        val bindings = engine.createBindings()
        bindings["discord"] = commandEvent.discord
        bindings["container"] = commandEvent.container
        bindings["event"] = commandEvent

        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
        engine.eval(
            """
                val discord = bindings["discord"] as me.jakejmattson.discordkt.api.Discord
                val container = bindings["container"] as me.jakejmattson.discordkt.api.dsl.command.CommandsContainer
                val event = bindings["event"] as me.jakejmattson.discordkt.api.dsl.CommandEvent<*>
                val jda = discord.jda
                
                fun evalScript() {
                    $input
                } 
                
                evalScript();
                """.trimIndent()
        )
    } catch (e: Exception) {
        System.err.print(e.message ?: "An exception occurred in the scripting engine.")
    }
}