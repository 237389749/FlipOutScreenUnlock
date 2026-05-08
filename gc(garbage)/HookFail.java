package com.example.flipman;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    // 外屏 cutout 字符串的资源 ID（根据反编译确定）
    private static final int CUTOUT_STRING_ID = 0x7f070003;
    private static final String FAKE_CUTOUT = "M 0,0 L 0,0 L 0,0 L 0,0 Z @bind_right_cutout";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("android")) {
            hookAndroidProcess(lpparam.classLoader);
        } else if (lpparam.packageName.equals("com.android.systemui")) {
            hookSystemUIProcess(lpparam.classLoader);
        }
    }

    private void hookAndroidProcess(ClassLoader cl) {
        try {
            // 1. WindowState Hook（清除 insets 和 letterbox）
            Class<?> windowStateClass = XposedHelpers.findClass("com.android.server.wm.WindowState", cl);
            XposedHelpers.findAndHookMethod(windowStateClass, "isLetterboxedForDisplayCutout", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log("FlipMan: disabled letterbox");
                }
            });
            XposedHelpers.findAndHookMethod(windowStateClass, "computeFrameLw", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    int displayId = XposedHelpers.getIntField(param.thisObject, "mDisplayId");
                    if (displayId != 0) return;
                    String[] insetFields = {"mContentInsets", "mStableInsets", "mSystemGestureInsets", "mContentInsetsRect"};
                    for (String field : insetFields) {
                        try {
                            Object insets = XposedHelpers.getObjectField(param.thisObject, field);
                            if (insets instanceof Rect) {
                                Rect rect = (Rect) insets;
                                if (rect.top > 0) {
                                    rect.top = 0;
                                    XposedBridge.log("FlipMan: cleared top of " + field);
                                }
                            }
                        } catch (Throwable ignore) {}
                    }
                }
            });

            // 2. 资源 Hook：通过 ID 替换 cutout 字符串
            Class<?> resourcesClass = XposedHelpers.findClass("android.content.res.Resources", cl);
            XposedHelpers.findAndHookMethod(resourcesClass, "getString", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    int id = (int) param.args[0];
                    if (id == CUTOUT_STRING_ID) {
                        param.setResult(FAKE_CUTOUT);
                        XposedBridge.log("FlipMan: replaced cutout string by ID");
                    }
                }
            });
            XposedHelpers.findAndHookMethod(resourcesClass, "getText", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    int id = (int) param.args[0];
                    if (id == CUTOUT_STRING_ID) {
                        param.setResult(FAKE_CUTOUT);
                        XposedBridge.log("FlipMan: replaced cutout text by ID");
                    }
                }
            });

            XposedBridge.log("FlipMan: hooks installed for android process");
        } catch (Throwable t) {
            XposedBridge.log("FlipMan: error in android process - " + t.getMessage());
        }
    }

    private void hookSystemUIProcess(ClassLoader cl) {
        try {
            Class<?> miuiConfigs = XposedHelpers.findClass("miui.util.MiuiConfigs", cl);
            XposedHelpers.findAndHookMethod(miuiConfigs, "isTinyScreen", Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(false);
                    XposedBridge.log("FlipMan: faked isTinyScreen -> false");
                }
            });
            // 可选：同时 Hook isFlipTinyScreen
            try {
                XposedHelpers.findAndHookMethod(miuiConfigs, "isFlipTinyScreen", Context.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(false);
                        XposedBridge.log("FlipMan: faked isFlipTinyScreen -> false");
                    }
                });
            } catch (Throwable ignore) {}
            XposedBridge.log("FlipMan: hooks installed for SystemUI");
        } catch (Throwable t) {
            XposedBridge.log("FlipMan: error in SystemUI - " + t.getMessage());
        }
    }
}

/**
 * 你提供的旧版 flipman 模块的核心思路是系统层强制干预（清除 insets、替换 cutout 资源、欺骗 isTinyScreen），但正是因为干预过于底层（computeFrameLw 高频调用、直接修改 Rect 对象），导致系统不稳定，容易触发 LSPosed 安全模式。而后来我们转向的应用层方案（劫持 Activity.onCreate 设置 ALWAYS 模式 + 隐藏系统栏 + 注入元数据）虽然温和得多，却成功实现了全屏，并解决了触摸拦截问题。因此，从稳定性和维护性角度，旧模块中除 always 模式 Hook 外的其他部分完全没有必要保留，且 computeFrameLw 的 insets 清除因风险过高应坚决抛弃。
 *
 * 下面详细评判每个部分：
 * 1. isLetterboxedForDisplayCutout Hook
 *
 *     必要性：❌ 不必要。
 *     当窗口的 layoutInDisplayCutoutMode == 3 (ALWAYS) 时，isLetterboxedForDisplayCutout 方法会直接返回 false（见你提供的源码）。你已经在应用层强制设置了 ALWAYS，因此系统自然不会再添加 letterbox，无需额外 Hook。
 *
 * 2. computeFrameLw 清除 mContentInsets 等 insets
 *
 *     必要性：⚠️ 理论上需要，但实际已通过应用层标志规避。
 *     设置 ALWAYS 和隐藏系统栏后，系统可能仍会为窗口添加上部安全区（mContentInsets.top），导致内容下移。但在你的测试中，应用已能全屏铺满（尽管有触摸拦截问题），说明当前组合（SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN + WindowInsetsController.hide）已让窗口真正扩展到全屏，mContentInsets 可能已被自动忽略或置零。如果未来某些应用出现顶部黑边，可以尝试在 Activity 中调用 ViewCompat.setOnApplyWindowInsetsListener 清除 insets，但这属于应用层修改，不适合全局 Hook。鉴于该 Hook 极易导致系统崩溃，强烈不建议加入。
 *
 * 3. 替换 cutout 字符串资源
 *
 *     必要性：❌ 不必要。
 *     该资源仅定义系统识别的挖孔区域，设置 ALWAYS 后，应用内容已强制延伸到该区域，即使系统认为有挖孔也不影响显示。此外，你已通过注入 miui.supportFlipWatchOverlayGroupView=false 移除了小部件窗口，触摸问题已解决。修改 cutout 字符串属于“锦上添花”，但风险（可能因资源 ID 错误导致无法开机）远大于收益，不应采用。
 *
 * 4. Hook MiuiConfigs.isTinyScreen 和 isFlipTinyScreen
 *
 *     必要性：❌ 不必要（但无害）。
 *     这两个方法主要用于 SystemUI 显示“展开到内屏”提示和某些布局调整。你已通过注入元数据 miui.supportFlipFullScreen=0 和强制系统服务返回 0，使得系统认为应用支持全屏，提示自然不再出现。保留此 Hook 不会导致崩溃，但也不会带来额外好处，可以移除。
 *
 * 最终稳定方案总结
 *
 * 你当前的最终模块（应用层 Hook + 元数据注入）已经正确且稳定。唯一需要补充的是确保所有 Activity 在 onResume 时重新应用全屏标志（防止某些应用重置），以及注入 miui.supportFlipWatchOverlayGroupView=false 以隐藏小部件区域（解决触摸拦截）。无需再引入任何系统层的 WindowState Hook。
 *
 * 如果仍有应用排版错位（如闲鱼、美团），说明它们对全屏适配不佳，可以采用黑名单机制排除这些应用（不对其进行全屏改造）。这是最简洁、最安全的做法。
 *
 * 结论：旧模块中除 always 模式 Hook 外的其他部分完全没有必要保留，请继续使用并维护当前稳定的应用层模块。
 */