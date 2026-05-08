package com.example.fullscreenunlock.hooks;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ActivityLifecycleHook implements IHook {
    private static final String TAG = "FullscreenUnlock";

    @Override
    public void hook(ClassLoader cl) {
        // onCreate
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Activity activity = (Activity) param.thisObject;
                    try {
                        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
                        if (attrs.layoutInDisplayCutoutMode != WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS) {
                            attrs.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
                            activity.getWindow().setAttributes(attrs);
                            XposedBridge.log(TAG + ": set ALWAYS cutout for " + activity.getPackageName());
                        }
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + ": error setting cutout mode - " + t.getMessage());
                    }
//                    hideSystemBars(activity);
                }
            });
            XposedBridge.log(TAG + ": hooked Activity.onCreate");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook Activity.onCreate - " + t.getMessage());
        }

        // onResume
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Activity activity = (Activity) param.thisObject;
//                    hideSystemBars(activity);
                    XposedBridge.log(TAG + ": re-hid system bars in onResume for " + activity.getPackageName());
                }
            });
            XposedBridge.log(TAG + ": hooked Activity.onResume");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook Activity.onResume - " + t.getMessage());
        }
    }

    private void hideSystemBars(Activity activity) {
        if (activity == null || activity.getWindow() == null) return;
        // 延迟到 DecorView 创建后再执行
        activity.getWindow().getDecorView().post(() -> {
            WindowInsetsController controller = activity.getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                XposedBridge.log(TAG + ": hid bars via WindowInsetsController");
            } else {
                XposedBridge.log(TAG + ": WindowInsetsController is null");
            }
        });
    }
}