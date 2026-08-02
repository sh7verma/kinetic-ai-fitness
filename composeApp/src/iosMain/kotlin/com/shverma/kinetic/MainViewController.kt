package com.shverma.kinetic

import androidx.compose.ui.window.ComposeUIViewController
import com.shverma.kinetic.di.initializeKineticIosKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initializeKineticIosKoin()
    return ComposeUIViewController { IosApp() }
}
