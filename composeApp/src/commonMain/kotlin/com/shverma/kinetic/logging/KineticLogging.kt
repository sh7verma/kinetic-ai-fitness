package com.shverma.kinetic.logging

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter

/** Configures Kermit to use the native log sink on every supported platform. */
fun configureKineticLogging() {
    Logger.setMinSeverity(Severity.Info)
    Logger.setLogWriters(platformLogWriter())
}
