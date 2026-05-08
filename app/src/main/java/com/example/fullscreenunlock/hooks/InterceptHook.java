package com.example.fullscreenunlock.hooks;

import android.content.ComponentName;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class InterceptHook implements IHook {

    private static final String TAG = "FullscreenUnlock";

    @Override
    public void hook(ClassLoader cl) {
        // 拦截 InterceptActivityController.isInterceptListUnCheckFold 方法，强制返回 false
        try {
            Class<?> interceptClass = XposedHelpers.findClass("com.android.server.wm.InterceptActivityController", cl);
            XposedHelpers.findAndHookMethod(interceptClass, "isInterceptListUnCheckFold",
                    ComponentName.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(false);
                            XposedBridge.log(TAG + ": forced isInterceptListUnCheckFold -> false");
                        }
                    });
            XposedBridge.log(TAG + ": hooked InterceptActivityController.isInterceptListUnCheckFold");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook isInterceptListUnCheckFold - " + t.getMessage());
        }

        // 可选：拦截 isInterceptListForProperty 双重保险
        try {
            Class<?> interceptClass = XposedHelpers.findClass("com.android.server.wm.InterceptActivityController", cl);
            XposedHelpers.findAndHookMethod(interceptClass, "isInterceptListForProperty",
                    ComponentName.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            // 强制返回 Pair(false, false) 表示不拦截
                            // 由于返回类型是 Pair<Boolean, Boolean>，我们需要构造一个 Pair 对象
                            try {
                                Class<?> pairClass = XposedHelpers.findClass("android.util.Pair", cl);
                                Object falseFalse = XposedHelpers.newInstance(pairClass, false, false);
                                param.setResult(falseFalse);
                                XposedBridge.log(TAG + ": forced isInterceptListForProperty -> (false,false)");
                            } catch (Throwable t2) {
                                XposedBridge.log(TAG + ": failed to create Pair - " + t2.getMessage());
                            }
                        }
                    });
            XposedBridge.log(TAG + ": hooked InterceptActivityController.isInterceptListForProperty");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook isInterceptListForProperty - " + t.getMessage());
        }

        // 补充：Hook ContinuityPolicyService.getAllowStartContinuityPackageNameList 等（可选）
        // 但以上两个 Hook 已足够，因为 isInterceptListUnCheckFold 是最终入口。
    }
}