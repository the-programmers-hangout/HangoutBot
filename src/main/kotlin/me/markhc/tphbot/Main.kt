package me.markhc.tphbot;

import me.aberrantfox.kjdautils.api.dsl.PrefixDeleteMode
import me.aberrantfox.kjdautils.api.startBot;
import me.markhc.tphbot.services.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        loadConfig {
            if (it == null)
                throw Exception("Failed to parse configuration");

            startBot(it.token) {
                registerInjectionObject(it);

                configure {
                    prefix = "++";
                    reactToCommands = true;
                    deleteMode = PrefixDeleteMode.None;
                }
            }
        }
    } catch (e: Exception) {
        exitProcess(-1);
    }
}