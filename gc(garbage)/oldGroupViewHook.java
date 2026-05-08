package com.example.fullscreenunlock.hooks;

import android.view.MotionEvent;
import android.view.View;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class WatchOverlayGroupViewHook implements IHook {

    private static final String TAG = "FullscreenUnlock";

    @Override
    public void hook(ClassLoader cl) {
        try {
            Class<?> clazz = XposedHelpers.findClass("com.miui.fliphome.widget.ui.WatchOverlayGroupView", cl);

            // 修改窗口标志
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

            // 拦截触摸事件
            XposedHelpers.findAndHookMethod(clazz, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log(TAG + ": intercepted dispatchTouchEvent -> false");
                }
            });

            // 视图附加后隐藏
            XposedHelpers.findAndHookMethod(clazz, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    XposedHelpers.callMethod(param.thisObject, "setVisibility", View.GONE);
                    XposedBridge.log(TAG + ": set WatchOverlayGroupView to GONE");
                }
            });

            // 方向更新时强制隐藏
            XposedHelpers.findAndHookMethod(clazz, "updateLayoutByOrientation", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(null);
                    XposedHelpers.callMethod(param.thisObject, "setVisibility", View.GONE);
                    XposedBridge.log(TAG + ": forced updateLayoutByOrientation to hide");
                }
            });

            XposedBridge.log(TAG + ": hooked WatchOverlayGroupView");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook WatchOverlayGroupView - " + t.getMessage());
        }
    }
}