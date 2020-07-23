package me.markhc.hangoutbot.utilities

import java.util.concurrent.TimeUnit

fun Long.toShortDurationString()  =
        String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(this),
                TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(this)),
                TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this)))

fun Long.toLongDurationString(): String {
    val days = TimeUnit.MILLISECONDS.toDays(this)
    val hours = TimeUnit.MILLISECONDS.toHours(this) - TimeUnit.DAYS.toHours(days)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(this))
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
    return if(hours < 48) {
        String.format(
                "%d hour${if(hours > 1 || hours == 0.toLong()) "s" else ""}, " +
                "%d minute${if(minutes > 1 || minutes == 0.toLong()) "s" else ""}, " +
                "%d second${if(seconds > 1 || seconds == 0.toLong()) "s" else ""}", hours, minutes, seconds)
    } else {
        String.format(
                "%d days, " +
                "%d hour${if(hours > 1 || hours == 0.toLong()) "s" else ""}, " +
                "%d minute${if(minutes > 1 || minutes == 0.toLong()) "s" else ""}, " +
                "%d second${if(seconds > 1 || seconds == 0.toLong()) "s" else ""}", days, hours, minutes, seconds)
    }

}
