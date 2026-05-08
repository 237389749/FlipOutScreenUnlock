package com.example.fullscreenunlock;

import android.app.Activity;
import android.content.Context;
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
            hookInputMethodManager(lpparam.classLoader); // 新增：处理输入法旋转限制
        } else if (lpparam.packageName.equals("com.miui.fliphome")) {
            hookWatchOverlayGroupView(lpparam.classLoader);
        } else if (shouldHook(lpparam.packageName)) {
            hookActivityOnCreate();
            hookActivityOnResume();
        }
    }

    private boolean shouldHook(String pkg) {
        // 可在此添加黑名单包名，例如 "com.taobao.idlefish"
        return true;
    }

    // ==================== 1. 系统服务 Hook（全屏兼容） ====================
    private void hookSystemServices(ClassLoader cl) {
        // 1.1 BoundsCompatUtils.getFlipCompatModeByApp
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

        // 1.2 BoundsCompatUtils.getFlipCompatModeByActivity
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

        // 1.3 WindowManagerServiceImpl.getFullScreenValue
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

    // ==================== 2. 输入法旋转提示和限制解除 ====================
    private void hookInputMethodManager(ClassLoader cl) {
        try {
            Class<?> immServiceClass = XposedHelpers.findClass("com.android.server.inputmethod.InputMethodManagerServiceImpl", cl);
            // Hook shouldShowCurrentInput 强制返回 true（允许输入法弹出）
            XposedHelpers.findAndHookMethod(immServiceClass, "shouldShowCurrentInput", Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(true);
                    XposedBridge.log(TAG + ": forced shouldShowCurrentInput -> true");
                }
            });
            // Hook makeRotateToast 阻止弹窗
            XposedHelpers.findAndHookMethod(immServiceClass, "makeRotateToast", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(null); // 阻止方法执行
                    XposedBridge.log(TAG + ": suppressed makeRotateToast");
                }
            });
            XposedBridge.log(TAG + ": hooked InputMethodManagerServiceImpl");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook InputMethodManagerServiceImpl - " + t.getMessage());
        }
    }

    // ==================== 3. 外屏小部件处理（使触摸穿透） ====================
    private void hookWatchOverlayGroupView(ClassLoader cl) {
        try {
            Class<?> clazz = XposedHelpers.findClass("com.miui.fliphome.widget.ui.WatchOverlayGroupView", cl);

            // 3.1 修改窗口标志，使其不可触摸
            XposedHelpers.findAndHookMethod(clazz, "init", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object view = param.thisObject;
                    Object layoutParams = XposedHelpers.getObjectField(view, "mLayoutParams");
                    if (layoutParams != null) {
                        int flags = XposedHelpers.getIntField(layoutParams, "flags");
                        flags |= 0x10; // FLAG_NOT_TOUCHABLE
                        flags |= 0x8;  // FLAG_NOT_FOCUSABLE
                        XposedHelpers.setIntField(layoutParams, "flags", flags);
                        XposedBridge.log(TAG + ": set WatchOverlayGroupView flags NOT_TOUCHABLE|NOT_FOCUSABLE");
                    }
                }
            });
            XposedBridge.log(TAG + ": hooked WatchOverlayGroupView.init");

            // 3.2 拦截触摸事件，直接返回 false
            XposedHelpers.findAndHookMethod(clazz, "dispatchTouchEvent", android.view.MotionEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log(TAG + ": intercepted dispatchTouchEvent -> false");
                }
            });
            XposedBridge.log(TAG + ": hooked dispatchTouchEvent");

            // 3.3 视图附加后隐藏（GONE）
            XposedHelpers.findAndHookMethod(clazz, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    XposedHelpers.callMethod(param.thisObject, "setVisibility", android.view.View.GONE);
                    XposedBridge.log(TAG + ": set WatchOverlayGroupView to GONE");
                }
            });
            XposedBridge.log(TAG + ": hooked onAttachedToWindow");

            // 3.4 方向更新时强制隐藏
            XposedHelpers.findAndHookMethod(clazz, "updateLayoutByOrientation", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
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

    // ==================== 4. Activity 生命周期劫持（全屏+隐藏系统栏） ====================
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