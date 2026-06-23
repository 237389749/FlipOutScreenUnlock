package com.parallelc.mixflipmod.hook

import android.graphics.Insets
import android.graphics.Path
import android.graphics.Rect
import com.parallelc.mixflipmod.Prefs
import com.parallelc.mixflipmod.hook.util.*
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

object CutoutFixHook : BaseHook() {
    override val targetPackages = listOf("android", "com.android.systemui", "com.miui.aod", "com.android.camera")

    override fun hook(param: PackageReadyParam) {
        if (!prefEnabled(Prefs.SYSTEM_CUTOUT_FIX)) return
        safeHook("[${param.packageName}] ${Prefs.SYSTEM_CUTOUT_FIX}") {
            setupHooks(param)
        }
    }

    private fun setupHooks(param: PackageReadyParam) {
        hookCutoutParser(param.classLoader)
        hookDisplayGetCutout(param.classLoader)
        if (param.packageName == "com.miui.aod") {
            hookDisplayUtilsGetCutoutPosition(param.classLoader)
        }
    }

    private fun hookCutoutParser(classLoader: ClassLoader) {
        runCatching {
            val parserClass = classLoader.findClass("android.view.CutoutSpecification\$Parser")
            val parseMethod = parserClass.method("parse", String::class.java)
            hook(parseMethod, after { chain, result ->
                val spec = result ?: return@after result
                val originalSpec = chain.args[0] as? String
                if (originalSpec != null && (originalSpec.contains("M 604,664") || originalSpec.contains("@bind_right_cutout"))) {
                    spec.setField("mLeftBound", Rect(0, 0, 0, 0))
                    spec.setField("mTopBound", Rect(0, 0, 0, 0))
                    spec.setField("mRightBound", Rect(0, 0, 0, 0))
                    spec.setField("mBottomBound", Rect(0, 0, 0, 0))
                    spec.setField("mInsets", Insets.of(0, 0, 0, 0))
                    spec.setField("mPath", Path())
                }
                result
            })
            log("hookCutoutParser hooked")
        }.onFailure { log("hookCutoutParser failed", it) }
    }

    private fun hookDisplayGetCutout(classLoader: ClassLoader) {
        runCatching {
            val displayClass = classLoader.findClass("android.view.Display")
            val getCutoutMethod = displayClass.method("getCutout")
            val displayCutoutClass = classLoader.findClass("android.view.DisplayCutout")

            val noCutout = runCatching {
                displayCutoutClass.getDeclaredField("NO_CUTOUT").also { it.isAccessible = true }.get(null)
            }.getOrNull()

            if (noCutout != null) {
                hook(getCutoutMethod, replaceResult(noCutout))
                log("hookDisplayGetCutout hooked (NO_CUTOUT)")
            } else {
                val zeroInsets = Insets.of(0, 0, 0, 0)
                hook(getCutoutMethod) { chain ->
                    runCatching {
                        val constructor = displayCutoutClass.getDeclaredConstructor(
                            Insets::class.java, Rect::class.java, Rect::class.java,
                            Rect::class.java, Rect::class.java
                        ).also { it.isAccessible = true }
                        constructor.newInstance(zeroInsets, null, null, null, null)
                    }.getOrElse { chain.proceed() }
                }
                log("hookDisplayGetCutout hooked (reflection fallback)")
            }
        }.onFailure { log("hookDisplayGetCutout failed", it) }
    }

    private fun hookDisplayUtilsGetCutoutPosition(classLoader: ClassLoader) {
        runCatching {
            val displayUtilsClass = classLoader.findClass("com.miui.aod.utils.DisplayUtils")
            val getCutoutPositionMethod = displayUtilsClass.method(
                "getCutoutPosition", android.content.Context::class.java
            )
            val directionClass = runCatching {
                classLoader.findClass("com.miui.aod.utils.DisplayUtils\$Direction")
            }.getOrElse {
                classLoader.findClass("com.miui.aod.utils.Direction")
            }
            val noneValue = directionClass.getDeclaredField("CAMERA_CUTOUT_ON_NONE")
                .also { it.isAccessible = true }
                .get(null)

            hook(getCutoutPositionMethod, replaceResult(noneValue))
            log("hookDisplayUtilsGetCutoutPosition hooked")
        }.onFailure { log("hookDisplayUtilsGetCutoutPosition failed", it) }
    }
}
