package com.example.fullscreenunlock.hooks;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class InputMethodHook implements IHook {

    private static final String TAG = "FullscreenUnlock";

    @Override
    public void hook(ClassLoader cl) {
        try {
            Class<?> immServiceClass = XposedHelpers.findClass("com.android.server.inputmethod.InputMethodManagerServiceImpl", cl);
            // Hook shouldShowCurrentInput
            XposedHelpers.findAndHookMethod(immServiceClass, "shouldShowCurrentInput", Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(true);
                    XposedBridge.log(TAG + ": forced shouldShowCurrentInput -> true");
                }
            });
            // Hook makeRotateToast
            XposedHelpers.findAndHookMethod(immServiceClass, "makeRotateToast", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(null);
                    XposedBridge.log(TAG + ": suppressed makeRotateToast");
                }
            });
            XposedBridge.log(TAG + ": hooked InputMethodManagerServiceImpl");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook InputMethodManagerServiceImpl - " + t.getMessage());
        }
    }
}