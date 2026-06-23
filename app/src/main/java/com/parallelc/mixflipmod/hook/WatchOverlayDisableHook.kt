package com.parallelc.mixflipmod.hook

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.view.WindowManager
import com.parallelc.mixflipmod.hook.util.after
import com.parallelc.mixflipmod.hook.util.callMethod
import com.parallelc.mixflipmod.hook.util.findClass
import com.parallelc.mixflipmod.hook.util.getField
import com.parallelc.mixflipmod.hook.util.hook
import com.parallelc.mixflipmod.hook.util.log
import com.parallelc.mixflipmod.hook.util.method
import com.parallelc.mixflipmod.hook.util.replaceResult
import com.parallelc.mixflipmod.hook.util.setField
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

object WatchOverlayDisableHook : BaseHook() {
    override val targetPackages = listOf("com.miui.fliphome")

    override fun setupHooks(prefKey: String, param: PackageReadyParam) {
        hookController(param.classLoader)
        hookView(param.classLoader)
        hookWindowManager()
    }

    private fun hookController(cl: ClassLoader) {
        val cls = cl.findClass("com.miui.fliphome.widget.WatchOverlayWindow\$CheckAppConfigRunnable")
        hook(cls.method("checkShouldHideWidget", PackageManager::class.java, ComponentName::class.java), after { chain, result ->
            runCatching {
                val outer = chain.thisObject.getField("this$0")
                outer.setField("mIsHideAppForeground", true)
                outer.callMethod("refreshWindow", 2, true)
            }.onFailure { log("WatchOverlay: checkShouldHideWidget after hook failed", it) }
            result
        })
        log("WatchOverlay: controller hook installed")
    }

    private fun hookView(cl: ClassLoader) {
        val viewClass = cl.findClass("com.miui.fliphome.widget.ui.WatchOverlayGroupView")

        val ctor = viewClass.getDeclaredConstructor(Context::class.java)
        ctor.isAccessible = true
        hook(ctor, after { chain, result ->
            runCatching {
                val view = chain.thisObject as? View
                if (view != null) {
                    val layoutParams = view.layoutParams as? WindowManager.LayoutParams
                    if (layoutParams != null) {
                        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        val context = view.context
                        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                        wm?.updateViewLayout(view, layoutParams)
                    }
                    view.visibility = View.GONE
                    runCatching {
                        val pagerView = view.getField("mPagerView") as? View
                        pagerView?.visibility = View.GONE
                    }
                }
            }.onFailure { log("WatchOverlay: constructor after hook failed", it) }
            result
        })

        hook(viewClass.method("dispatchTouchEvent", MotionEvent::class.java), replaceResult(false))

        hook(viewClass.method("setVisibility", Int::class.javaPrimitiveType!!)) { chain ->
            if (chain.args.getOrNull(0) as? Int == View.VISIBLE) {
                chain.args[0] = View.GONE
            }
            chain.proceed()
        }

        log("WatchOverlay: view hooks installed")
    }

    private fun hookWindowManager() {
        hook(ViewManager::class.java.method("addView", View::class.java, ViewGroup.LayoutParams::class.java)) { chain ->
            val view = chain.args.getOrNull(0) as? View
            if (view != null && view.javaClass.name.contains("WatchOverlayGroupView")) {
                log("WatchOverlay: blocked addView for ${view.javaClass.name}")
                null
            } else {
                chain.proceed()
            }
        }
        log("WatchOverlay: WindowManager hook installed")
    }
}
