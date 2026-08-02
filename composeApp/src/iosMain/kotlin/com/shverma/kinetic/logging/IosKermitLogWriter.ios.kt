package com.shverma.kinetic.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import platform.Foundation.NSLog

/** Sends Kermit records through Apple's native console sink for Xcode visibility. */
internal class IosKermitLogWriter : LogWriter() {
    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        val throwableMessage = throwable?.message?.let { " ($it)" }.orEmpty()
        NSLog("Kinetic: [$severity][$tag] $message$throwableMessage")
    }
}
