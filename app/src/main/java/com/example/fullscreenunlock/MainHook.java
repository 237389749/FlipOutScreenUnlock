package com.example.fullscreenunlock;

import com.example.fullscreenunlock.hooks.*;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "FullscreenUnlock";
    // 针对弹窗的hook不起作用
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 公共的 Cutout 清除 Hook，适用于所有需要全屏的进程
        if (needsFullscreenHook(lpparam.packageName)) {
            new CutoutSpecificationHook().hook(lpparam.classLoader);
        }

        // 原有的系统进程 Hook
        if (lpparam.packageName.equals("android")) {
            new SystemServicesHook().hook(lpparam.classLoader);
            new InputMethodHook().hook(lpparam.classLoader);
            new InterceptHook().hook(lpparam.classLoader);
        }

        // 桌面
        if (lpparam.packageName.equals("com.miui.fliphome")) {
            new WatchOverlayDisableHook().hook(lpparam.classLoader);
        }

        // 其他普通应用（生命周期钩子）
        if (shouldHook(lpparam.packageName)) {
            new ActivityLifecycleHook().hook(lpparam.classLoader);
        }
    }

    // 需要 Cutout 清零的进程包名
    private boolean needsFullscreenHook(String pkg) {
        switch (pkg) {
            case "android":                         // 系统服务
            case "com.android.systemui":            // 锁屏/状态栏
            case "com.miui.aod":                    // AOD 独立进程（如果有）
            case "com.android.camera":              // 相机
                return true;
            default:
                return false;
        }
    }
    private boolean shouldHook(String pkg) {
        // 可根据需要添加黑名单
        return true;
    }
}