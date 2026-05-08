package com.example.fullscreenunlock.hooks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

public class WhiteListHook implements IHook {
    private static final String TAG = "WhitelistHook";
    private static volatile boolean isUpdating = false;
    private Context systemContext;

    @Override
    public void hook(ClassLoader cl) {
        try {
            systemContext = (Context) XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", cl),
                    "systemMain");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to get system context - " + t.getMessage());
            return;
        }

        updateWhitelist(cl);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        systemContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isUpdating) {
                    isUpdating = true;
                    new Thread(() -> {
                        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                        updateWhitelist(cl);
                        isUpdating = false;
                    }).start();
                }
            }
        }, filter);
        XposedBridge.log(TAG + ": registered package broadcast receiver");
    }

    private void updateWhitelist(ClassLoader cl) {
        try {
            PackageManager pm = systemContext.getPackageManager();
            List<android.content.pm.ApplicationInfo> apps = pm.getInstalledApplications(0);
            StringBuilder sb = new StringBuilder();
            for (android.content.pm.ApplicationInfo info : apps) {
                sb.append(info.packageName).append(":");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            String allApps = sb.toString();

            // 通过反射获取 ServiceManager 和 window 服务
            Class<?> smClass = XposedHelpers.findClass("android.os.ServiceManager", cl);
            IBinder windowBinder = (IBinder) XposedHelpers.callStaticMethod(smClass, "getService", "window");
            Method dumpMethod = windowBinder.getClass().getMethod("dump", java.io.FileDescriptor.class, String[].class);
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            dumpMethod.invoke(windowBinder, pipe[1].getFileDescriptor(),
                    new String[]{"-setForceDisplayCompatMode", allApps, "allowstart"});
            // 读取输出避免阻塞
            InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(pipe[0]);
            byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) { /* ignore */ }
            is.close();
            XposedBridge.log(TAG + ": updated whitelist with " + apps.size() + " apps");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": updateWhitelist error - " + t.getMessage());
        }
    }
}