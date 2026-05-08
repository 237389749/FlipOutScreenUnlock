package com.example.fullscreenunlock.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SystemServicesHook implements IHook {

    private static final String TAG = "FullscreenUnlock";

    @Override
    public void hook(ClassLoader cl) {
        // 1. BoundsCompatUtils.getFlipCompatModeByApp
        try {
            Class<?> boundsCompatUtils = XposedHelpers.findClass("com.android.server.wm.BoundsCompatUtils", cl);
            Class<?> atmsClass = XposedHelpers.findClass("android.app.ActivityTaskManagerService", cl);
            XposedHelpers.findAndHookMethod(boundsCompatUtils, "getFlipCompatModeByApp",
                    atmsClass, String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(0);
                            XposedBridge.log(TAG + ": forced BoundsCompatUtils.getFlipCompatModeByApp -> 0");
                        }
                    });
            XposedBridge.log(TAG + ": hooked BoundsCompatUtils.getFlipCompatModeByApp");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook BoundsCompatUtils.getFlipCompatModeByApp - " + t.getMessage());
        }

        // 2. BoundsCompatUtils.getFlipCompatModeByActivity
        try {
            Class<?> boundsCompatUtils = XposedHelpers.findClass("com.android.server.wm.BoundsCompatUtils", cl);
            Class<?> activityRecordClass = XposedHelpers.findClass("com.android.server.wm.ActivityRecord", cl);
            XposedHelpers.findAndHookMethod(boundsCompatUtils, "getFlipCompatModeByActivity",
                    activityRecordClass, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(0);
                            XposedBridge.log(TAG + ": forced BoundsCompatUtils.getFlipCompatModeByActivity -> 0");
                        }
                    });
            XposedBridge.log(TAG + ": hooked BoundsCompatUtils.getFlipCompatModeByActivity");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook getFlipCompatModeByActivity - " + t.getMessage());
        }

        // 3. WindowManagerServiceImpl.getFullScreenValue
        try {
            Class<?> wmsImpl = XposedHelpers.findClass("com.android.server.wm.WindowManagerServiceImpl", cl);
            Class<?> packageItemInfoClass = XposedHelpers.findClass("android.content.pm.PackageItemInfo", cl);
            XposedHelpers.findAndHookMethod(wmsImpl, "getFullScreenValue",
                    packageItemInfoClass, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(0);
                            XposedBridge.log(TAG + ": forced WindowManagerServiceImpl.getFullScreenValue -> 0");
                        }
                    });
            XposedBridge.log(TAG + ": hooked WindowManagerServiceImpl.getFullScreenValue");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook getFullScreenValue - " + t.getMessage());
        }
    }
}