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
        } else if (lpparam.packageName.equals("com.miui.fliphome")) {
            // 针对外屏桌面进程，多管齐下处理小部件覆盖层
            hookWatchOverlayGroupView(lpparam.classLoader);
        } else if (shouldHook(lpparam.packageName)) {
            hookActivityOnCreate();
            hookActivityOnResume();
        }
    }

    private boolean shouldHook(String pkg) {
        // 可在此添加黑名单
        return true;
    }

    // ==================== 1. 系统服务 Hook（全屏兼容） ====================
    private void hookSystemServices(ClassLoader cl) {
        // ... 保持不变，省略之前已稳定的代码 ...
        // 请将你原有的 hookSystemServices 完整内容粘贴于此（为避免重复，此处省略，实际使用中必须包含）
        // 为节省篇幅，这里用注释代替，你需保留原有的三个 Hook。
    }

    // ==================== 2. 多管齐下处理 WatchOverlayGroupView ====================
    private void hookWatchOverlayGroupView(ClassLoader cl) {
        try {
            Class<?> clazz = XposedHelpers.findClass("com.miui.fliphome.widget.ui.WatchOverlayGroupView", cl);

            // 1) 在 init 完成后修改窗口标志，使其不可触摸和不可聚焦
            XposedHelpers.findAndHookMethod(clazz, "init", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object view = param.thisObject;
                    Object layoutParams = XposedHelpers.getObjectField(view, "mLayoutParams");
                    if (layoutParams != null) {
                        int flags = XposedHelpers.getIntField(layoutParams, "flags");
                        // FLAG_NOT_TOUCHABLE = 0x10，FLAG_NOT_FOCUSABLE = 0x8
                        flags |= 0x10;
                        flags |= 0x8;
                        XposedHelpers.setIntField(layoutParams, "flags", flags);
                        XposedBridge.log(TAG + ": set flags NOT_TOUCHABLE|NOT_FOCUSABLE");
                    }
                }
            });
            XposedBridge.log(TAG + ": hooked WatchOverlayGroupView.init");

            // 2) 拦截 dispatchTouchEvent，直接返回 false，避免触摸转发
            XposedHelpers.findAndHookMethod(clazz, "dispatchTouchEvent", android.view.MotionEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log(TAG + ": intercepted dispatchTouchEvent -> false");
                }
            });
            XposedBridge.log(TAG + ": hooked dispatchTouchEvent");

            // 3) 在视图附加到窗口后立即将其隐藏（GONE），使其不可见且不消耗任何事件
            XposedHelpers.findAndHookMethod(clazz, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object view = param.thisObject;
                    XposedHelpers.callMethod(view, "setVisibility", android.view.View.GONE);
                    XposedBridge.log(TAG + ": set WatchOverlayGroupView to GONE");
                }
            });
            XposedBridge.log(TAG + ": hooked onAttachedToWindow");

            // 4) 可选：使 updateLayoutByOrientation 强行隐藏
            XposedHelpers.findAndHookMethod(clazz, "updateLayoutByOrientation", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // 强制设置为隐藏
                    param.setResult(null);
                    XposedHelpers.callMethod(param.thisObject, "setVisibility", android.view.View.GONE);
                    XposedBridge.log(TAG + ": forced updateLayoutByOrientation to hide");
                }
            });
            XposedBridge.log(TAG + ": hooked updateLayoutByOrientation");

        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook WatchOverlayGroupView - " + t.getMessage());
        }
    }

    // ==================== 3. Activity 生命周期劫持（全屏+隐藏系统栏） ====================
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
                        XposedBridge.log(TAG + ": error setting cutout mode - " + t.getMessage());
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
                    XposedBridge.log(TAG + ": re-hid system bars in onResume");
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