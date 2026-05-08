package com.example.fullscreenunlock.hooks;

import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class WatchOverlayGroupViewHook implements IHook {

    private static final String TAG = "FullscreenUnlock";

    @Override
    public void hook(ClassLoader cl) {
        try {
            Class<?> clazz = XposedHelpers.findClass("com.miui.fliphome.widget.ui.WatchOverlayGroupView", cl);

            // 1. 在构造函数中直接修改布局参数，并强制隐藏
            XposedHelpers.findAndHookConstructor(clazz, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object view = param.thisObject;
                    // 获取 mLayoutParams
                    Object layoutParams = XposedHelpers.getObjectField(view, "mLayoutParams");
                    if (layoutParams != null) {
                        int flags = XposedHelpers.getIntField(layoutParams, "flags");
                        flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                        flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        XposedHelpers.setIntField(layoutParams, "flags", flags);
                        XposedBridge.log(TAG + ": constructor: set flags NOT_TOUCHABLE|NOT_FOCUSABLE");
                    }
                    // 强制隐藏
                    XposedHelpers.callMethod(view, "setVisibility", View.GONE);
                    XposedBridge.log(TAG + ": constructor: set visibility GONE");
                }
            });

            // 2. 拦截 init 方法（原代码已存在，但加强）
            XposedHelpers.findAndHookMethod(clazz, "init", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object view = param.thisObject;
                    Object layoutParams = XposedHelpers.getObjectField(view, "mLayoutParams");
                    if (layoutParams != null) {
                        int flags = XposedHelpers.getIntField(layoutParams, "flags");
                        flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                        flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        XposedHelpers.setIntField(layoutParams, "flags", flags);
                    }
                    XposedHelpers.callMethod(view, "setVisibility", View.GONE);
                    // 额外：将内部 mPagerView 也禁用
                    Object pager = XposedHelpers.getObjectField(view, "mPagerView");
                    if (pager != null) {
                        XposedHelpers.callMethod(pager, "setAlpha", 0.0f);
                        XposedHelpers.callMethod(pager, "setVisibility", View.GONE);
                    }
                    XposedBridge.log(TAG + ": init: forced invisible and not touchable");
                }
            });

            // 3. 禁用 dispatchTouchEvent（原代码）
            XposedHelpers.findAndHookMethod(clazz, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log(TAG + ": dispatchTouchEvent -> false");
                }
            });

            // 4. 强制 isHide() 始终返回 true（使窗口在方向更新时自动隐藏）
            try {
                XposedHelpers.findAndHookMethod(clazz, "isHide", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(true);
                        XposedBridge.log(TAG + ": isHide -> true");
                    }
                });
            } catch (Throwable t) {
                XposedBridge.log(TAG + ": isHide method not found, skipping");
            }

            // 5. 拦截 setVisibility，阻止任何将其设为 VISIBLE 的调用
            XposedHelpers.findAndHookMethod(clazz, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    int visibility = (int) param.args[0];
                    if (visibility == View.VISIBLE) {
                        param.args[0] = View.GONE;
                        XposedBridge.log(TAG + ": setVisibility(VISIBLE) -> GONE");
                    }
                }
            });

            // 6. 拦截 updateLayoutByOrientation，直接隐藏
            XposedHelpers.findAndHookMethod(clazz, "updateLayoutByOrientation", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    XposedHelpers.callMethod(param.thisObject, "setVisibility", View.GONE);
                    param.setResult(null);
                    XposedBridge.log(TAG + ": updateLayoutByOrientation -> hide");
                }
            });

            // 7. 可选：Hook onAttachedToWindow 再次确保隐藏
            XposedHelpers.findAndHookMethod(clazz, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    XposedHelpers.callMethod(param.thisObject, "setVisibility", View.GONE);
                    XposedBridge.log(TAG + ": onAttachedToWindow -> set GONE");
                }
            });

            XposedBridge.log(TAG + ": WatchOverlayGroupView hooks installed (enhanced)");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook WatchOverlayGroupView - " + t.getMessage());
        }
    }
}