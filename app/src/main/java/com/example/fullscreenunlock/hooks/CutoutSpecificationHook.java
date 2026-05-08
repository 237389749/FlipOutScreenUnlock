package com.example.fullscreenunlock.hooks;

import android.graphics.Insets;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.Display;
import android.view.DisplayCutout;

import com.example.fullscreenunlock.hooks.IHook;

import java.lang.reflect.Constructor;
import java.util.Collections;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class CutoutSpecificationHook implements IHook {

    private DisplayCutout zeroCutout;

    public void hook(ClassLoader cl) {
        // 1. 清理 CutoutSpecification 解析结果（可选，双保险）
        hookCutoutParser(cl);

        // 2. 核心：拦截 Display.getCutout()，永远返回全零对象
        hookDisplayGetCutout(cl);

        // 3. 直接消除 SystemUI/AOD 的崩溃入口
        hookDisplayUtilsGetCutoutPosition(cl);
    }

    private void hookCutoutParser(ClassLoader cl) {
        try {
            Class<?> parserClass = XposedHelpers.findClass("android.view.CutoutSpecification$Parser", cl);
            XposedHelpers.findAndHookMethod(parserClass, "parse", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object spec = param.getResult();
                    if (spec == null) return;
                    String originalSpec = (String) param.args[0];
                    if (originalSpec != null && (originalSpec.contains("M 604,664") || originalSpec.contains("@bind_right_cutout"))) {
                        XposedHelpers.setObjectField(spec, "mLeftBound", new Rect(0,0,0,0));
                        XposedHelpers.setObjectField(spec, "mTopBound", new Rect(0,0,0,0));
                        XposedHelpers.setObjectField(spec, "mRightBound", new Rect(0,0,0,0));
                        XposedHelpers.setObjectField(spec, "mBottomBound", new Rect(0,0,0,0));
                        XposedHelpers.setObjectField(spec, "mInsets", Insets.of(0, 0, 0, 0));
                        XposedHelpers.setObjectField(spec, "mPath", new Path());
                        XposedBridge.log("CutoutFix: cleared outer display cutout in parser");
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("CutoutFix: failed hook parser: " + t.getMessage());
        }
    }

    private void hookDisplayGetCutout(ClassLoader cl) {
        try {
            XposedHelpers.findAndHookMethod(Display.class, "getCutout", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {

                    DisplayCutout zero = getZeroCutout();
                    if (zero != null) {
                        param.setResult(zero);
                    }

                }
            });
            XposedBridge.log("CutoutFix: hooked Display.getCutout");
        } catch (Throwable t) {
            XposedBridge.log("CutoutFix: failed hook Display.getCutout: " + t.getMessage());
        }
    }

    private void hookDisplayUtilsGetCutoutPosition(ClassLoader cl) {
        try {
            Class<?> displayUtilsClass = XposedHelpers.findClass("com.miui.aod.util.DisplayUtils", cl);
            Class<?> directionClass = XposedHelpers.findClass("com.miui.aod.widget.Direction", cl);
            Object noneDirection = XposedHelpers.getStaticObjectField(directionClass, "CAMERA_CUTOUT_ON_NONE");

            XposedHelpers.findAndHookMethod(displayUtilsClass, "getCutoutPosition",
                    android.content.Context.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(noneDirection);
                        }
                    });
            XposedBridge.log("CutoutFix: hooked DisplayUtils.getCutoutPosition → always NONE");
        } catch (Throwable t) {
            XposedBridge.log("CutoutFix: failed hook DisplayUtils: " + t.getMessage());
        }
    }

    private DisplayCutout getZeroCutout() {
        if (zeroCutout != null) return zeroCutout;
        try {
            zeroCutout = constructZeroCutout();
        } catch (Throwable t) {
            XposedBridge.log("CutoutFix: construct zero cutout failed: " + t.getMessage());
        }
        return zeroCutout;
    }

    private DisplayCutout constructZeroCutout() throws Exception {
        Class<?> dcClass = DisplayCutout.class;
        Constructor<?>[] constructors = dcClass.getDeclaredConstructors();
        Constructor<?> chosen = null;
        for (Constructor<?> c : constructors) {
            if (chosen == null || c.getParameterCount() < chosen.getParameterCount()) {
                chosen = c;
            }
        }
        if (chosen == null) throw new NoSuchMethodException("No DisplayCutout constructor");
        chosen.setAccessible(true);
        Class<?>[] paramTypes = chosen.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];
            if (type == Insets.class) {
                args[i] = Insets.of(0, 0, 0, 0);
            } else if (type == Rect.class) {
                args[i] = new Rect(0, 0, 0, 0);
            } else if (type == Path.class) {
                args[i] = new Path();
            } else if (type == int.class || type == Integer.class) {
                args[i] = 0;
            } else if (type == boolean.class || type == Boolean.class) {
                args[i] = false;
            } else if (type == java.util.List.class) {
                args[i] = Collections.emptyList();
            } else {
                args[i] = null;
            }
        }
        return (DisplayCutout) chosen.newInstance(args);
    }
}