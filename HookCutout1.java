package com.example.fullscreenunlock.hooks;

import android.graphics.Insets;
import android.graphics.Path;
import android.graphics.Rect;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class CutoutSpecificationHook implements IHook {

    private static final String TAG = "FullscreenUnlock";

    @Override
    public void hook(ClassLoader cl) {
        try {
            Class<?> parserClass = XposedHelpers.findClass("android.view.CutoutSpecification$Parser", cl);
            XposedHelpers.findAndHookMethod(parserClass, "parse", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object spec = param.getResult();
                    if (spec == null) return;
                    String originalSpec = (String) param.args[0];
                    if (originalSpec != null && (originalSpec.contains("M 604,664") || originalSpec.contains("@bind_right_cutout"))) {
                        XposedHelpers.setObjectField(spec, "mLeftBound", new Rect());
                        XposedHelpers.setObjectField(spec, "mTopBound", new Rect());
                        XposedHelpers.setObjectField(spec, "mRightBound", new Rect());
                        XposedHelpers.setObjectField(spec, "mBottomBound", new Rect());
                        XposedHelpers.setObjectField(spec, "mInsets", Insets.of(0, 0, 0, 0));
                        XposedHelpers.setObjectField(spec, "mPath", new Path());
                        XposedBridge.log(TAG + ": cleared outer display cutout");
                    }
                }
            });
            XposedBridge.log(TAG + ": hooked CutoutSpecification.Parser.parse");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to hook CutoutSpecification - " + t.getMessage());
        }
    }
}