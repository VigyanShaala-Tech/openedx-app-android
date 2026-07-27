package org.openedx.core.extension

import android.net.Uri
import java.net.URL

fun String?.equalsHost(host: String?): Boolean {
    return try {
        host?.startsWith(URL(this).host, ignoreCase = true) == true
    } catch (_: Exception) {
        false
    }
}

fun String.addMobileQueryParam(): String {
    if (this.isBlank() || this.startsWith("javascript:", ignoreCase = true)) {
        return this
    }
    return try {
        val uri = Uri.parse(this)
        if (uri.scheme?.startsWith("http") != true) {
            return this
        }
        if (uri.getQueryParameter("mobile") == "true") {
            return this
        }
        uri.buildUpon()
            .appendQueryParameter("mobile", "true")
            .build()
            .toString()
    } catch (e: Exception) {
        this
    }
}
