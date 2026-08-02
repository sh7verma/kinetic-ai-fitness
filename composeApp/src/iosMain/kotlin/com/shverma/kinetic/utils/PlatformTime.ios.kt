package com.shverma.kinetic.utils

import platform.CoreFoundation.CFAbsoluteTimeGetCurrent

actual fun currentTimeMillis(): Long =
    ((CFAbsoluteTimeGetCurrent() + 978307200.0) * 1000.0).toLong()
