package com.parallelc.mixflipmod.hook

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import android.view.View
import com.parallelc.mixflipmod.Prefs
import com.parallelc.mixflipmod.hook.util.findClass
import com.parallelc.mixflipmod.hook.util.getField
import com.parallelc.mixflipmod.hook.util.hook
import com.parallelc.mixflipmod.hook.util.method
import com.parallelc.mixflipmod.hook.util.prefInt
import com.parallelc.mixflipmod.hook.util.replaceResult
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.lang.reflect.Method
import kotlin.math.abs

object FlipHomeHook : BaseHook() {
    override val targetPackages = listOf("com.miui.fliphome")

    override fun setupHooks(prefKey: String, param: PackageReadyParam) {
        when (prefKey) {
            Prefs.FLIPHOME_NO_START_PAGE -> hookNoStartPage(param)
            Prefs.FLIPHOME_RECENTS_STYLE -> hookRecentsStyle(param)
        }
    }

    private fun hookNoStartPage(param: PackageReadyParam) {
        val cls = param.classLoader.findClass("com.miui.fliphome.utils.PerformLaunchAction")
        hook(
            cls.method("onStartIntercept", UserHandle::class.java, Intent::class.java, Bundle::class.java, View::class.java),
            replaceResult(false)
        )
    }

    private fun hookRecentsStyle(param: PackageReadyParam) {
        val layoutStyleClass = param.classLoader.findClass("com.miui.fliphome.recents.TaskStackViewLayoutStyle")
        val horizontalClass = param.classLoader.findClass("com.miui.fliphome.recents.TaskStackViewLayoutStyleHorizontal")
        val horizontalConstructor = horizontalClass.getDeclaredConstructor(Context::class.java).also {
            it.isAccessible = true
        }
        val verticalClass = param.classLoader.findClass("com.miui.fliphome.recents.TaskStackViewLayoutStyleVertical")
        val verticalConstructor = verticalClass.getDeclaredConstructor(Context::class.java).also {
            it.isAccessible = true
        }
        hook(layoutStyleClass.method("create", Int::class.java, Context::class.java), Hooker { chain ->
            val context = chain.args[1] as? Context ?: return@Hooker chain.proceed()
            when (prefInt(Prefs.FLIPHOME_RECENTS_STYLE, Prefs.RecentsLayoutStyle.DEFAULT.prefValue)) {
                Prefs.RecentsLayoutStyle.HORIZONTAL.prefValue -> horizontalConstructor.newInstance(context)
                Prefs.RecentsLayoutStyle.VERTICAL.prefValue -> verticalConstructor.newInstance(context)
                else -> chain.proceed()
            }
        })

        setupRecentsLayoutDetection(param.classLoader)
        hookRecentsHorizontalTargetTaskIndex(param)
        hookRecentsHorizontalSwipeDismissDirection(param)
    }

    private fun hookRecentsHorizontalTargetTaskIndex(param: PackageReadyParam) {
        val algorithmClass = param.classLoader.findClass("com.miui.fliphome.recents.views.TaskStackViewsAlgorithmHorizontal")
        hook(
            algorithmClass.method(
                "getTargetTaskViewIndex",
                Boolean::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!,
            ),
            Hooker { chain ->
                if (recentsLayoutStyle() != Prefs.RecentsLayoutStyle.HORIZONTAL) return@Hooker chain.proceed()
                val quickSwitch = chain.args.getOrNull(0) as? Boolean
                val target = chain.args.getOrNull(1) as? Int
                val running = chain.args.getOrNull(2) as? Int
                if (quickSwitch == false && target == -1 && running != null && running >= 0) {
                    running
                } else {
                    chain.proceed()
                }
            },
        )
    }

    private fun hookRecentsHorizontalSwipeDismissDirection(param: PackageReadyParam) {
        val verticalSwipeClass = param.classLoader.findClass("com.miui.fliphome.recents.views.VerticalSwipe")
        hook(verticalSwipeClass.method("getCurTransY"), Hooker { chain ->
            val result = chain.proceed()
            if (recentsLayoutStyle() != Prefs.RecentsLayoutStyle.HORIZONTAL) return@Hooker result
            val transY = result as? Float ?: return@Hooker result
            val scale = chain.thisObject.getField("mCurScale") as? Float ?: return@Hooker result
            if (scale < 0.999f) {
                -abs(transY)
            } else {
                result
            }
        })
    }

    private var getFlipAppInstance: Method? = null
    private var getBaseGesture: Method? = null
    private var getLayoutStyle: Method? = null
    private var horizontalStyleClass: Class<*>? = null
    private var verticalStyleClass: Class<*>? = null
    private var recentsLayoutDetectionReady = false

    private fun setupRecentsLayoutDetection(cl: ClassLoader) {
        if (recentsLayoutDetectionReady) return

        val appClass = cl.loadClass("com.miui.fliphome.FlipApplication")
        getFlipAppInstance = appClass.getMethod("getInstance")
        getBaseGesture = appClass.getMethod("getBaseGestureImpl")
        getLayoutStyle = cl.loadClass("com.miui.fliphome.gesture.BaseGestureImpl")
            .getMethod("getTaskStackViewLayoutStyle")
        horizontalStyleClass = cl.loadClass("com.miui.fliphome.recents.TaskStackViewLayoutStyleHorizontal")
        verticalStyleClass = cl.loadClass("com.miui.fliphome.recents.TaskStackViewLayoutStyleVertical")
        recentsLayoutDetectionReady = true
    }

    private fun recentsLayoutStyle(): Prefs.RecentsLayoutStyle {
        if (!recentsLayoutDetectionReady) return Prefs.RecentsLayoutStyle.DEFAULT
        return runCatching {
            val app = getFlipAppInstance?.invoke(null) ?: return Prefs.RecentsLayoutStyle.DEFAULT
            val gesture = getBaseGesture?.invoke(app) ?: return Prefs.RecentsLayoutStyle.DEFAULT
            val style = getLayoutStyle?.invoke(gesture)
            if (horizontalStyleClass?.isInstance(style) == true) Prefs.RecentsLayoutStyle.HORIZONTAL
            else if (verticalStyleClass?.isInstance(style) == true) Prefs.RecentsLayoutStyle.VERTICAL
            else Prefs.RecentsLayoutStyle.DEFAULT
        }.getOrDefault(Prefs.RecentsLayoutStyle.DEFAULT)
    }
}
