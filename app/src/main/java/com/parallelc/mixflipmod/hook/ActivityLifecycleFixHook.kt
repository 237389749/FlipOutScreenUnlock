package com.parallelc.mixflipmod.hook

import android.os.Bundle
import android.view.WindowManager
import com.parallelc.mixflipmod.Prefs
import com.parallelc.mixflipmod.hook.util.*
import com.parallelc.mixflipmod.module
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

object ActivityLifecycleFixHook {

    @Volatile
    private var enabled = false

    fun hook(param: SystemServerStartingParam) {
        safeHook("[global] activity cutout mode") {
            val prefs = module!!.getRemotePreferences(Prefs.NAME)
            enabled = prefs.getBoolean(Prefs.ACTIVITY_CUTOUT_MODE, false)
            prefs.registerOnSharedPreferenceChangeListener { _, key ->
                if (key == Prefs.ACTIVITY_CUTOUT_MODE) {
                    enabled = prefs.getBoolean(Prefs.ACTIVITY_CUTOUT_MODE, false)
                }
            }
            setupHook(param)
        }
    }

    private fun setupHook(param: SystemServerStartingParam) {
        val activityClass = param.classLoader.findClass("android.app.Activity")
        val onCreate = activityClass.method("onCreate", Bundle::class.java)
        hook(onCreate, after { chain, result ->
            if (!enabled) return@after result
            runCatching {
                val window = (chain.thisObject as? Any)?.callMethod("getWindow") ?: return@runCatching
                val attrs = window.callMethod("getAttributes") as? WindowManager.LayoutParams ?: return@runCatching
                attrs.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                window.callMethod("setAttributes", attrs)
            }
            result
        })
        log("ActivityLifecycleFix: onCreate hook installed")
    }

    private const val LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS = 3
}
