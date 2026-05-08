package com.example.fullscreenunlock.hooks;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class TinyScreenHook implements IHook{

    private static final String TAG = "TinyScreenHook";

    public void hook(ClassLoader cl) {
        // 1. Hook miui.util.MiuiConfigs.isTinyScreen
        try {
            Class<?> miuiConfigs = XposedHelpers.findClass("miui.util.MiuiConfigs", cl);
            XposedHelpers.findAndHookMethod(miuiConfigs, "isTinyScreen", Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log(TAG + ": forced isTinyScreen -> false");
                }
            });
            XposedBridge.log(TAG + ": hooked MiuiConfigs.isTinyScreen");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook isTinyScreen - " + t.getMessage());
        }

        // 2. Hook miui.util.MiuiConfigs.isFlipTinyScreen
        try {
            Class<?> miuiConfigs = XposedHelpers.findClass("miui.util.MiuiConfigs", cl);
            XposedHelpers.findAndHookMethod(miuiConfigs, "isFlipTinyScreen", Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log(TAG + ": forced isFlipTinyScreen -> false");
                }
            });
            XposedBridge.log(TAG + ": hooked MiuiConfigs.isFlipTinyScreen");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook isFlipTinyScreen - " + t.getMessage());
        }

        // 3. 修改 miuix.os.Build.IS_FLIP 字段
        try {
            Class<?> miuixBuild = XposedHelpers.findClass("miuix.os.Build", cl);
            XposedHelpers.setStaticBooleanField(miuixBuild, "IS_FLIP", false);
            XposedBridge.log(TAG + ": set miuix.os.Build.IS_FLIP = false");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to set miuix.os.Build.IS_FLIP - " + t.getMessage());
        }

        // 4. Hook miui.os.Build.isFlipDevice()
        try {
            Class<?> miuiBuild = XposedHelpers.findClass("miui.os.Build", cl);
            XposedHelpers.findAndHookMethod(miuiBuild, "isFlipDevice", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log(TAG + ": forced miui.os.Build.isFlipDevice -> false");
                }
            });
            XposedBridge.log(TAG + ": hooked miui.os.Build.isFlipDevice");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook miui.os.Build.isFlipDevice - " + t.getMessage());
        }

        // 5. Hook miui.util.MiuiMultiDisplayTypeInfo.isFlipDevice()
        try {
            Class<?> infoClass = XposedHelpers.findClass("miui.util.MiuiMultiDisplayTypeInfo", cl);
            XposedHelpers.findAndHookMethod(infoClass, "isFlipDevice", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log(TAG + ": forced MiuiMultiDisplayTypeInfo.isFlipDevice -> false");
                }
            });
            XposedBridge.log(TAG + ": hooked MiuiMultiDisplayTypeInfo.isFlipDevice");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook MiuiMultiDisplayTypeInfo.isFlipDevice - " + t.getMessage());
        }

        // 6. 可选：修改系统属性 persist.sys.multi_display_type（需要 root 权限，通过命令行）
        // 此操作不在 Hook 范围内，但可通过模块的 post-fs-data.sh 实现，此处不实现。
    }
}