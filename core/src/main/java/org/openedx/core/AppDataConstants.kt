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

    val ZOOM_URL_PATTERNS = listOf("zoom.us", "zoom.com", "/meeting/", "/join/", "zoom")

    const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    const val MOBILE_CHROME_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/UD1A.230805.019) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36"
}
