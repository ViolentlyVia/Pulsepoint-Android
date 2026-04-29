package com.FMDAP.pulsepoint

import android.app.Application
import com.FMDAP.pulsepoint.data.prefs.AppPreferences
import com.FMDAP.pulsepoint.data.repository.PulsePointRepository

class PulsePointApp : Application() {
    val prefs by lazy { AppPreferences(this) }
    val repository by lazy { PulsePointRepository(prefs) }
}
