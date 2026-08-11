package org.openedx.core

import java.util.Locale

object AppDataConstants {
    const val USER_MIN_YEAR = 13
    const val USER_MAX_YEAR = 77
    const val DEFAULT_MIME_TYPE = "image/jpeg"
    val defaultLocale: Locale = Locale.Builder().setLanguage("en").build()

    const val VIDEO_FORMAT_M3U8 = ".m3u8"
    const val VIDEO_FORMAT_MP4 = ".mp4"

    // Equal 1GB
    const val DOWNLOADS_CONFIRMATION_SIZE = 1024 * 1024 * 1024L

    const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    const val MOBILE_CHROME_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
}
