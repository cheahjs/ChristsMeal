package me.jscheah.christsmeal

val Long.formatTimeAgo: String
    get() {
        val diffSeconds = this / 1000 % 60
        if (diffSeconds < 60)
            return "$diffSeconds second${if (diffSeconds == 1L) "s" else ""} ago"
        val diffMinutes = this / (60 * 1000) % 60
        if (diffMinutes < 60)
            return "$diffMinutes minute${if (diffMinutes == 1L) "s" else ""} ago"
        val diffHours = this / (60 * 60 * 1000) % 24
        if (diffHours < 24)
            return "$diffHours hour${if (diffHours == 1L) "s" else ""} ago"
        val diffDays = this / (24 * 60 * 60 * 1000)
        return "$diffHours day${if (diffDays == 1L) "s" else ""} ago"
    }