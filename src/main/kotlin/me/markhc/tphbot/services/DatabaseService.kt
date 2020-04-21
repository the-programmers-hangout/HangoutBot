package me.markhc.tphbot.services

import java.sql.*
import me.aberrantfox.kjdautils.api.annotation.Service
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Properties

fun createDatabaseSchema(configuration: Configuration) {
    with(configuration.mysqlConfig) {
        val dbParams = listOf(
                "useUnicode=true",
                "useJDBCCompliantTimezoneShift=true",
                "useLegacyDatetimeCode=true",
                "serverTimezone=UTC",
                "nullNamePatternMatchesAll=true",
                "useSSL=false"
        )
        println("1")
        Database.connect(
                url = "jdbc:mysql://$url/$dbname?${dbParams.joinToString("&")}",
                driver = "com.mysql.cj.jdbc.Driver",
                user = username,
                password = password)
        println("2")
        transaction {
            addLogger(StdOutSqlLogger)

            SchemaUtils.create(GuildConfiguration)
        }
        println("3")
    }
}

object GuildConfiguration : IntIdTable() {
    val guildId = varchar("guildId", 18).uniqueIndex()
    val prefix = varchar("prefix", 32)
    val reactToCommands = bool("react")
    val welcomeEmbeds = bool("welcomeEmbeds")
}

fun GuildConfiguration.findGuild(id: String?, result: (ResultRow?) -> Unit) {
    if(id == null) {
        result(null)
    } else {
        result(GuildConfiguration.select {GuildConfiguration.guildId eq id }.firstOrNull())
    }
}