package org.openedx.core.system

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.RequestBody
import org.openedx.core.config.Config
import org.openedx.core.data.api.CookiesApi
import retrofit2.Response
import java.util.concurrent.TimeUnit

class AppCookieManager(private val config: Config, private val api: CookiesApi) {

    companion object {
        private val FRESHNESS_INTERVAL = TimeUnit.HOURS.toMillis(1)
    }

    private var authSessionCookieExpiration: Long = -1
    private var response: Response<RequestBody>? = null

    suspend fun tryToRefreshSessionCookie(targetUrl: String? = null) {
        try {
            response = api.userCookies()
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            val parsedCookies = Cookie.parseAll(response!!.raw().request.url, response!!.headers())

            val hostsToSet = mutableSetOf<String>()
            val apiHost = config.getApiHostURL().removeSuffix("/")
            hostsToSet.add(apiHost)

            if (!apiHost.contains("://apps.")) {
                hostsToSet.add(apiHost.replace("://", "://apps."))
            }

            targetUrl?.let { url ->
                try {
                    val uri = java.net.URI(url)
                    if (uri.scheme != null && uri.host != null) {
                        hostsToSet.add("${uri.scheme}://${uri.host}")
                    }
                } catch (_: Exception) {}
            }

            for (host in hostsToSet) {
                for (cookie in parsedCookies) {
                    cookieManager.setCookie(host, cookie.toString())
                }
            }
            cookieManager.flush()
            authSessionCookieExpiration = System.currentTimeMillis() + FRESHNESS_INTERVAL
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearWebViewCookie() {
        CookieManager.getInstance().removeAllCookies(null)
        authSessionCookieExpiration = -1
    }

    fun isSessionCookieMissingOrExpired(): Boolean {
        return authSessionCookieExpiration < System.currentTimeMillis()
    }
}
