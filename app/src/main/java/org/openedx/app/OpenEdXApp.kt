package org.openedx.app

import android.app.Application
import com.braze.Braze
import com.braze.configuration.BrazeConfig
import com.braze.ui.BrazeDeeplinkHandler
import com.google.firebase.FirebaseApp
import io.branch.referral.Branch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.openedx.app.deeplink.BranchBrazeDeeplinkHandler
import org.openedx.app.di.appModule
import org.openedx.app.di.networkingModule
import org.openedx.app.di.screenModule
import org.openedx.core.config.Config
import org.openedx.firebase.OEXFirebaseAnalytics

class OpenEdXApp : Application() {

    private val config by inject<Config>()
    private val pluginManager by inject<PluginManager>()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OpenEdXApp)
            modules(
                appModule,
                networkingModule,
                screenModule
            )
        }

        if (config.getShakeConfig().enabled) {
            try {
                Class.forName("com.shakebugs.shake.Shake")
                com.shakebugs.shake.Shake.start(this, config.getShakeConfig().token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (config.getBugseeConfig().enabled) {
            try {
                Class.forName("com.bugsee.library.Bugsee")
                com.bugsee.library.Bugsee.launch(this, config.getBugseeConfig().token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (config.getFirebaseConfig().enabled) {
            FirebaseApp.initializeApp(this)
        }

        if (config.getBranchConfig().enabled) {
            if (BuildConfig.DEBUG) {
                Branch.enableTestMode()
                Branch.enableLogging()
            }
            Branch.expectDelayedSessionInitialization(true)
            Branch.getAutoInstance(this)
        }

        if (config.getBrazeConfig().isEnabled && config.getFirebaseConfig().enabled) {
            val isCloudMessagingEnabled = config.getFirebaseConfig().isCloudMessagingEnabled &&
                    config.getBrazeConfig().isPushNotificationsEnabled

            val brazeConfig = BrazeConfig.Builder()
                .setIsFirebaseCloudMessagingRegistrationEnabled(isCloudMessagingEnabled)
                .setFirebaseCloudMessagingSenderIdKey(config.getFirebaseConfig().projectNumber)
                .setHandlePushDeepLinksAutomatically(true)
                .setIsFirebaseMessagingServiceOnNewTokenRegistrationEnabled(true)
                .build()
            Braze.configure(this, brazeConfig)

            if (config.getBranchConfig().enabled) {
                BrazeDeeplinkHandler.setBrazeDeeplinkHandler(BranchBrazeDeeplinkHandler())
            }
        }

        if (config.getFacebookConfig().isEnabled()) {
            try {
                val clazz = Class.forName("com.facebook.FacebookSdk")
                clazz.getMethod("setApplicationId", String::class.java)
                    .invoke(null, config.getFacebookConfig().appId)
                clazz.getMethod("setClientToken", String::class.java)
                    .invoke(null, config.getFacebookConfig().clientToken)
                clazz.getMethod("sdkInitialize", android.content.Context::class.java)
                    .invoke(null, this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        initPlugins()
    }

    private fun initPlugins() {
        if (config.getFirebaseConfig().enabled) {
            pluginManager.addPlugin(OEXFirebaseAnalytics(context = this))
        }
    }
}
