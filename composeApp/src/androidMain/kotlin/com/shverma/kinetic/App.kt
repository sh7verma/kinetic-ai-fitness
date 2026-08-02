package com.shverma.kinetic

import android.app.Application
import com.shverma.kinetic.BuildConfig
import co.touchlab.kermit.Logger
import com.shverma.kinetic.di.kineticAndroidModule
import com.shverma.kinetic.logging.configureKineticLogging
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class KineticApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        configureKineticLogging()
        startKoin {
            androidContext(this@KineticApplication)
            modules(kineticAndroidModule)
        }
        if (BuildConfig.DEBUG) {
            Logger.withTag("Kinetic").i { "Application initialized" }
        }
    }
}
