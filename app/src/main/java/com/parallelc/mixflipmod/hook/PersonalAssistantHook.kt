package com.parallelc.mixflipmod.hook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.parallelc.mixflipmod.Prefs
import com.parallelc.mixflipmod.hook.util.callMethod
import com.parallelc.mixflipmod.hook.util.findClass
import com.parallelc.mixflipmod.hook.util.getField
import com.parallelc.mixflipmod.hook.util.hook
import com.parallelc.mixflipmod.hook.util.log
import com.parallelc.mixflipmod.hook.util.method
import io.github.libxposed.api.XposedInterface.Hooker
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.io.File
import java.lang.reflect.Method
import java.util.zip.ZipInputStream

object PersonalAssistantHook : BaseHook() {
    override val targetPackages = listOf("com.miui.personalassistant")

    private const val WIDGET_ID_PREFIX = "mixflipmod_"
    private const val FLIPHOME_PACKAGE = "com.miui.fliphome"
    private const val PA_FILE_PROVIDER = "com.miui.personalassistant.fileprovider"
    private const val METHOD_IMPORT = "mixflipmod_import"
    private const val METHOD_PING = "mixflipmod_ping"
    private const val EXTRA_TARGET = "mixflipmod_target"
    private const val TARGET_FLIPHOME = "fliphome"

    private val MTZ_SIZE_REGEX = Regex("""widget_(\d+)x(\d+)""")

    override fun setupHooks(prefKey: String, param: PackageReadyParam) {
        when (prefKey) {
            Prefs.WIDGET_IMPORT -> hookWidgetImport(param)
        }
    }

    private fun hookWidgetImport(param: PackageReadyParam) {
        val fileProviderClass = param.classLoader.findClass("androidx.core.content.FileProvider")
        val getUriForFile: Method = fileProviderClass.declaredMethods.first { m ->
            java.lang.reflect.Modifier.isStatic(m.modifiers) &&
            m.returnType == Uri::class.java &&
            m.parameterTypes.contentEquals(arrayOf(Context::class.java, String::class.java, File::class.java))
        }.also { it.isAccessible = true }

        // respond to liveness ping from fliphome so it can detect whether this hook is active
        val widgetExternalProviderClass = param.classLoader.findClass("com.miui.personalassistant.widget.provider.WidgetExternalProvider")
        hook(widgetExternalProviderClass.method("call", String::class.java, String::class.java, android.os.Bundle::class.java), Hooker { chain ->
            if (chain.args[0] as? String != METHOD_PING) return@Hooker chain.proceed()
            android.os.Bundle().apply { putBoolean("active", true) }
        })

        // intercept addWidgetOrMaMl to send widget to fliphome instead
        val itemInfoClass = param.classLoader.findClass("com.miui.personalassistant.widget.entity.ItemInfo")
        val maMlItemInfoClass = param.classLoader.findClass("com.miui.personalassistant.widget.iteminfo.MaMlItemInfo")
        val actionControllerClass = param.classLoader.findClass("com.miui.personalassistant.picker.business.detail.utils.PickerDetailActionController")
        hook(
            actionControllerClass.method("addWidgetOrMaMl", android.view.View::class.java, itemInfoClass, Boolean::class.javaPrimitiveType!!),
            Hooker { chain ->
                val controller = chain.thisObject
                val view = chain.args[0] as? android.view.View ?: return@Hooker chain.proceed()
                val itemInfo = chain.args[1] ?: return@Hooker chain.proceed()
                val pickerActivity = runCatching {
                    controller.getField("pickerDetailFragment")?.callMethod("getMPickerActivity") as? Activity
                }.getOrNull() ?: return@Hooker chain.proceed()
                if (pickerActivity.intent?.getStringExtra(EXTRA_TARGET) != TARGET_FLIPHOME) {
                    return@Hooker chain.proceed()
                }
                if (!maMlItemInfoClass.isInstance(itemInfo)) {
                    Toast.makeText(view.context, "外屏暂只支持 MAML 小部件", Toast.LENGTH_SHORT).show()
                    return@Hooker false
                }

                val context = view.context.applicationContext
                val productId = itemInfo.getField("productId") as? String
                val versionCode = itemInfo.getField("versionCode") as? Int ?: 0
                val resPath = itemInfo.getField("resPath") as? String
                val sourceFile = findPersonalAssistantMamlZip(context, productId, versionCode, resPath)
                if (productId.isNullOrBlank() || sourceFile == null) {
                    log("PA picker import skipped: source file not found, productId=$productId resPath=$resPath")
                    Toast.makeText(view.context, "小部件资源未下载", Toast.LENGTH_SHORT).show()
                    return@Hooker false
                }
                val compatError = checkMtzCompatibility(sourceFile)
                if (compatError != null) {
                    log("PA picker import skipped: $compatError productId=$productId")
                    Toast.makeText(view.context, compatError, Toast.LENGTH_LONG).show()
                    return@Hooker false
                }

                val uri = runCatching { getUriForFile.invoke(null, context, PA_FILE_PROVIDER, sourceFile) as Uri }
                    .onFailure { log("create PA FileProvider uri failed: ${sourceFile.absolutePath}", it) }
                    .getOrNull()
                if (uri == null) {
                    Toast.makeText(view.context, "小部件资源授权失败", Toast.LENGTH_SHORT).show()
                    return@Hooker false
                }
                val destName = "$WIDGET_ID_PREFIX$productId.mtz"

                Thread {
                    context.grantUriPermission(FLIPHOME_PACKAGE, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val success = runCatching {
                        val result = context.contentResolver.call(
                            "content://com.miui.fliphome".toUri(),
                            METHOD_IMPORT,
                            destName,
                            android.os.Bundle().apply { putParcelable("uri", uri) }
                        )
                        result?.getBoolean("success", false) == true
                    }.getOrElse { e ->
                        log("PA import via FlipHomeProvider failed", e)
                        false
                    }.also {
                        context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        if (success) {
                            Toast.makeText(view.context, "添加成功", Toast.LENGTH_SHORT).show()
                            pickerActivity.finish()
                        } else {
                            Toast.makeText(view.context, "添加失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
                false
            }
        )
    }

    // Returns a non-null error message if the file is incompatible, null if OK.
    private fun checkMtzCompatibility(file: File): String? {
        var hasDescription = false
        val sizes = mutableSetOf<String>()
        runCatching {
            ZipInputStream(file.inputStream()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val name = entry.name
                    when (val firstSegment = name.substringBefore('/')) {
                        "description.xml" -> hasDescription = true
                        else -> MTZ_SIZE_REGEX.find(firstSegment)?.let { m ->
                            sizes.add("${m.groupValues[1]}x${m.groupValues[2]}")
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }.onFailure { log("checkMtzCompatibility failed", it); return "小部件文件无法读取" }
        if (!hasDescription) return "小部件格式无效（缺少 description.xml）"
        if ("2x3" !in sizes) return if (sizes.isEmpty()) "小部件不含任何规格信息"
                                    else "小部件不含 2x3 规格（仅含：${sizes.joinToString()}）"
        return null
    }

    private fun findPersonalAssistantMamlZip(context: Context, productId: String?, versionCode: Int, resPath: String?): File? {
        val candidates = buildList {
            if (!resPath.isNullOrBlank()) {
                val resFile = File(resPath)
                if (isMamlArchive(resFile)) add(resFile)
                generateSequence(resFile) { it.parentFile }
                    .take(6)
                    .forEach { dir ->
                        if (!productId.isNullOrBlank()) add(File(dir, "$productId.zip"))
                    }
            }
            if (!productId.isNullOrBlank()) {
                add(File(context.filesDir, "maml/res/0/$productId/$versionCode/$productId.zip"))
                add(File(context.filesDir, "maml/res/$productId.zip"))
            }
        }
        return candidates.firstOrNull { isMamlArchive(it) && it.canRead() }
    }

    private fun isMamlArchive(file: File): Boolean {
        return file.isFile &&
            (file.extension.equals("zip", ignoreCase = true) || file.extension.equals("mtz", ignoreCase = true))
    }
}
