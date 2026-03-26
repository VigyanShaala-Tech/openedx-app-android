package org.openedx.discovery.presentation

import androidx.fragment.app.Fragment
//import org.openedx.auth.presentation.logistration.LogistrationFragment

class DiscoveryNavigator(
    private val isDiscoveryTypeWebView: Boolean,
) {
    fun getDiscoveryFragment(): Fragment {
        return if (isDiscoveryTypeWebView) {
            WebViewDiscoveryFragment.newInstance()
        } else {
//            LogistrationFragment.newInstance(null, "RECOMMENDED")
            NativeDiscoveryFragment.newInstance()
        }
    }
}
