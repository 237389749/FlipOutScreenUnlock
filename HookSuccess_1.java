package com.example.fullscreenunlock;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "FullscreenUnlock";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            hookSystemServices(lpparam.classLoader);
            return;
        }
        if (shouldHook(lpparam.packageName)) {
            hookActivityOnCreate();
            hookActivityOnResume();
        }
    }

    private boolean shouldHook(String pkg) {
        String[] exclude = {
                ""
        };
        for (String e : exclude) if (pkg.equals(e)) return false;
        return true;
    }

    private void hookSystemServices(ClassLoader cl) {
        // 1. Hook BoundsCompatUtils.getFlipCompatModeByApp
        try {
            Class<?> boundsCompatUtils = XposedHelpers.findClass("com.android.server.wm.BoundsCompatUtils", cl);
            Class<?> atmsClass = XposedHelpers.findClass("android.app.ActivityTaskManagerService", cl);
            XposedHelpers.findAndHookMethod(boundsCompatUtils, "getFlipCompatModeByApp",
                    atmsClass, String.class,
                    new XC_MethodHook() {
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

        // 2. Hook BoundsCompatUtils.getFlipCompatModeByActivity
        try {
            Class<?> boundsCompatUtils = XposedHelpers.findClass("com.android.server.wm.BoundsCompatUtils", cl);
            Class<?> activityRecordClass = XposedHelpers.findClass("com.android.server.wm.ActivityRecord", cl);
            XposedHelpers.findAndHookMethod(boundsCompatUtils, "getFlipCompatModeByActivity",
                    activityRecordClass,
                    new XC_MethodHook() {
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

        // 3. Hook WindowManagerServiceImpl.getFullScreenValue
        try {
            Class<?> wmsImpl = XposedHelpers.findClass("com.android.server.wm.WindowManagerServiceImpl", cl);
            Class<?> packageItemInfoClass = XposedHelpers.findClass("android.content.pm.PackageItemInfo", cl);
            XposedHelpers.findAndHookMethod(wmsImpl, "getFullScreenValue",
                    packageItemInfoClass,
                    new XC_MethodHook() {
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

    private void hookActivityOnCreate() {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Activity activity = (Activity) param.thisObject;
                    String pkg = activity.getPackageName();
                    try {
                        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
                        if (attrs.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS) {
                            attrs.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
                            activity.getWindow().setAttributes(attrs);
                            XposedBridge.log(TAG + ": set ALWAYS cutout for " + pkg);
                        }
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + ": error setting cutout mode for " + pkg + " - " + t.getMessage());
                    }
                    hideSystemBars(activity);
                }
            });
            XposedBridge.log(TAG + ": hooked Activity.onCreate");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook Activity.onCreate - " + t.getMessage());
        }
    }

    private void hookActivityOnResume() {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Activity activity = (Activity) param.thisObject;
                    hideSystemBars(activity);
                    XposedBridge.log(TAG + ": re-hid system bars in onResume for " + activity.getPackageName());
                }
            });
            XposedBridge.log(TAG + ": hooked Activity.onResume");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook Activity.onResume - " + t.getMessage());
        }
    }

    private void hideSystemBars(Activity activity) {
        WindowInsetsController controller = activity.getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            XposedBridge.log(TAG + ": hid bars via WindowInsetsController");
        }
    }
}