package com.shverma.kinetic.data.auth

import android.app.Activity

/** Supplies the currently resumed host Activity to Android-only UI launchers. */
class AndroidActivityProvider {
    var currentActivity: Activity? = null
}
