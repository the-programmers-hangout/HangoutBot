package me.markhc.tphbot.services

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun createDatabaseSchema(configuration: Configuration) {
    val url    = System.getenv("DB_URL");
    val dbname = System.getenv("DB_NAME");
    val user   = System.getenv("DB_USER");
    val pass   = System.getenv("DB_PASS");

    Database.connect(
            url = "jdbc:mysql://$url/$dbname?autoReconnect=true&useSSL=false",
            driver = "com.mysql.cj.jdbc.Driver",
            user = user,
            password = pass)
    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.createMissingTablesAndColumns(GuildConfigurationTable)
    }
}

object GuildConfigurationTable : IntIdTable() {
    val guildId = varchar("guildId", 18).uniqueIndex()
    val prefix = varchar("prefix", 32).default("+")
    val reactToCommands = bool("react").default(false)
    val welcomeEmbeds = bool("welcomeEmbeds").default(false)
    val welcomeChannel = varchar("welcomeChannel", 18).nullable()
    val staffRoleName = varchar("staffRoleName", 32).nullable()
}

class GuildConfiguration(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GuildConfiguration>(GuildConfigurationTable)
    var guildId by GuildConfigurationTable.guildId
    var prefix by GuildConfigurationTable.prefix
    var reactToCommands by GuildConfigurationTable.reactToCommands
    var welcomeEmbeds by GuildConfigurationTable.welcomeEmbeds
    var welcomeChannel by GuildConfigurationTable.welcomeChannel
    var staffRoleName by GuildConfigurationTable.staffRoleName
}

fun GuildConfiguration.Companion.findOrCreate(id: String): GuildConfiguration {
    val guild = GuildConfiguration.find {GuildConfigurationTable.guildId eq id }.firstOrNull()

    if(guild != null) return guild

    return GuildConfiguration.new {
        guildId = id
        prefix = "++"
        reactToCommands = true
        welcomeEmbeds = false
    }
}