package me.markhc.hangoutbot.utilities

import java.util.concurrent.TimeUnit

class TimeFormatter {
    companion object {
        fun toShortDurationString(ms: Long)  =
                String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(ms),
                        TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
                        TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)))

        fun toLongDurationString(ms: Long): String {
            val units = decompose(ms)

            return units.filter { it.first != 0L }.joinToString {
                "${it.first} ${it.second}"
            }.ifBlank { "0 seconds" }
        }

        private fun decompose(ms: Long): List<Pair<Long, String>> {
            val days    = TimeUnit.MILLISECONDS.toDays(ms)
            val hours   = TimeUnit.MILLISECONDS.toHours(ms) - days * 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) - hours * 60 - days * 1440
            val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) - minutes * 60 - hours * 3600 - days * 86400

            return listOf(
                    days to "day${if(days != 1L) "s" else ""}",
                    hours to "hour${if(hours != 1L) "s" else ""}",
                    minutes to "minute${if(minutes != 1L) "s" else ""}",
                    seconds to "second${if(seconds != 1L) "s" else ""}")
        }
    }
}