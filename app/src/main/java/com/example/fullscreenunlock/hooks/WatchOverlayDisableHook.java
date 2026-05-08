package com.example.fullscreenunlock.hooks;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class WatchOverlayDisableHook implements IHook {

    private static final String TAG = "FullscreenUnlock";

    @Override
    public void hook(ClassLoader cl) {
        // 1. 控制器层：强制隐藏窗口
        hookCheckAppConfigRunnable(cl);

        // 2. 视图层：多重保险，确保小部件不可见且不可触摸
        hookWatchOverlayGroupView(cl);

        // 3. 窗口层：阻止窗口添加，并拦截所有触摸事件
        hookWatchOverlayWindow(cl);

        hookWindowManagerAddView(cl);
    }

    // ========== 1. 控制器层：强制隐藏窗口 ==========
    private void hookCheckAppConfigRunnable(ClassLoader cl) {
        try {
            Class<?> runnableClass = XposedHelpers.findClass(
                    "com.miui.fliphome.widget.WatchOverlayWindow$CheckAppConfigRunnable", cl);
            XposedHelpers.findAndHookMethod(runnableClass, "checkShouldHideWidget",
                    PackageManager.class, ComponentName.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                Object runnable = param.thisObject;
                                Object window = XposedHelpers.getObjectField(runnable, "this$0");
                                if (window != null) {
                                    // 设置隐藏标志
                                    XposedHelpers.setBooleanField(window, "mIsHideAppForeground", true);
                                    XposedHelpers.callMethod(window, "refreshWindow", 2, true);
                                    XposedBridge.log(TAG + ": WatchOverlayWindow forced hidden");
                                }
                            } catch (Throwable t) {
                                XposedBridge.log(TAG + ": error in checkShouldHideWidget callback - " + t.getMessage());
                            }
                        }
                    });
            XposedBridge.log(TAG + ": hooked CheckAppConfigRunnable.checkShouldHideWidget");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook CheckAppConfigRunnable - " + t.getMessage());
        }
    }

    // ========== 2. 视图层：WatchOverlayGroupView 全部禁用 ==========
    private void hookWatchOverlayGroupView(ClassLoader cl) {
        try {
            Class<?> clazz = XposedHelpers.findClass(
                    "com.miui.fliphome.widget.ui.WatchOverlayGroupView", cl);

            // 2.1 构造函数：修改窗口 flags 并立即隐藏
            XposedHelpers.findAndHookConstructor(clazz, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object view = param.thisObject;
                    setNotTouchableAndGone(view);
                    XposedBridge.log(TAG + ": WatchOverlayGroupView constructor -> GONE & NOT_TOUCHABLE");
                }
            });

            // 2.2 init 方法：再次强化
            XposedHelpers.findAndHookMethod(clazz, "init", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object view = param.thisObject;
                    setNotTouchableAndGone(view);
                    // 同时隐藏内部分页视图
                    try {
                        Object pager = XposedHelpers.getObjectField(view, "mPagerView");
                        if (pager != null) {
                            XposedHelpers.callMethod(pager, "setAlpha", 0.0f);
                            XposedHelpers.callMethod(pager, "setVisibility", View.GONE);
                        }
                    } catch (Throwable ignored) {}
                    XposedBridge.log(TAG + ": WatchOverlayGroupView init -> GONE & NOT_TOUCHABLE");
                }
            });

            // 2.3 拦截触摸事件（直接返回 false）
            XposedHelpers.findAndHookMethod(clazz, "dispatchTouchEvent", MotionEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log(TAG + ": WatchOverlayGroupView.dispatchTouchEvent -> false");
                }
            });

            // 2.4 强制 isHide 始终返回 true
            try {
                XposedHelpers.findAndHookMethod(clazz, "isHide", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(true);
                    }
                });
            } catch (Throwable ignored) {}

            // 2.5 阻止任何外部设置 VISIBLE
            XposedHelpers.findAndHookMethod(clazz, "setVisibility", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    int visibility = (int) param.args[0];
                    if (visibility == View.VISIBLE) {
                        param.args[0] = View.GONE;
                        XposedBridge.log(TAG + ": WatchOverlayGroupView.setVisibility(VISIBLE) -> GONE");
                    }
                }
            });

            // 2.6 方向更新：直接移除窗口并隐藏
            XposedHelpers.findAndHookMethod(clazz, "updateLayoutByOrientation", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    View view = (View) param.thisObject;
                    // 强制隐藏
                    XposedHelpers.callMethod(view, "setVisibility", View.GONE);

                    // 如果窗口仍附着在 WindowManager 上，立即移除
                    try {
                        if (view.getParent() != null) {
                            WindowManager wm = (WindowManager) view.getContext()
                                    .getSystemService(Context.WINDOW_SERVICE);
                            wm.removeViewImmediate(view);
                            XposedBridge.log(TAG + ": updateLayoutByOrientation -> removed window");
                        }
                    } catch (Throwable t) {
                        XposedBridge.log(TAG + ": updateLayoutByOrientation remove error: " + t.getMessage());
                    }
                    param.setResult(null);
                }
            });

            // 2.7 附加到窗口后再次确保隐藏和不可触摸
            XposedHelpers.findAndHookMethod(clazz, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    View view = (View) param.thisObject;
                    XposedHelpers.callMethod(view, "setVisibility", View.GONE);
                    setNotTouchableAndGone(view);
                    // 再次尝试移除（如果一个已经隐藏的窗口被意外添加）
                    try {
                        WindowManager wm = (WindowManager) view.getContext()
                                .getSystemService(Context.WINDOW_SERVICE);
                        wm.removeViewImmediate(view);
                    } catch (Throwable ignored) {}
                }
            });

            XposedBridge.log(TAG + ": WatchOverlayGroupView hooks installed");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook WatchOverlayGroupView - " + t.getMessage());
        }
    }

    // ========== 3. 窗口层：WatchOverlayWindow 本身 ==========
    private void hookWatchOverlayWindow(ClassLoader cl) {
        try {
            Class<?> watchWindowClass = XposedHelpers.findClass(
                    "com.miui.fliphome.widget.WatchOverlayWindow", cl);

            // 3.1 拦截 refreshWindow：将任何 ADD 请求转为 REMOVE
            XposedHelpers.findAndHookMethod(watchWindowClass, "refreshWindow",
                    int.class, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            int action = (int) param.args[0];
                            if (action == 1) {
                                param.args[0] = 2;       // ACTION_REMOVE
                                param.args[1] = false;   // 不用动画
                                XposedBridge.log(TAG + ": refreshWindow ADD → REMOVE");
                            }
                        }
                    });

            // 3.2 拦截 onInputMonitorEvent：直接返回 false，不消费任何触摸
            XposedHelpers.findAndHookMethod(watchWindowClass, "onInputMonitorEvent",
                    MotionEvent.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(false);
                            XposedBridge.log(TAG + ": onInputMonitorEvent -> false");
                        }
                    });

            XposedBridge.log(TAG + ": hooked WatchOverlayWindow");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook WatchOverlayWindow - " + t.getMessage());
        }
    }

    // 辅助方法：设置 View 不可触摸并隐藏
    private void setNotTouchableAndGone(Object viewObj) {
        try {
            View view = (View) viewObj;
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
            if (lp != null) {
                lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                WindowManager wm = (WindowManager) view.getContext()
                        .getSystemService(Context.WINDOW_SERVICE);
                wm.updateViewLayout(view, lp);
            }
            view.setVisibility(View.GONE);
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": setNotTouchableAndGone error: " + t.getMessage());
        }
    }

    // ========== 4. 终极拦截：禁止 WatchOverlayGroupView 窗口被添加 ==========
    private void hookWindowManagerAddView(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(
                    android.view.WindowManager.class,
                    "addView",
                    android.view.View.class,
                    android.view.ViewGroup.LayoutParams.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            View view = (View) param.args[0];
                            if (view.getClass().getName().contains("WatchOverlayGroupView")) {
                                param.setResult(null);  // 直接丢弃，不添加到窗口管理器
                                XposedBridge.log(TAG + ": 🔥 Blocked WindowManager.addView for WatchOverlayGroupView");
                            }
                        }
                    });
            XposedBridge.log(TAG + ": hooked WindowManager.addView → intercept WatchOverlayGroupView");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed hook WindowManager.addView: " + t.getMessage());
        }
    }
}