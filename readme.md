# 官方开发指南 https://dev.mi.com/xiaomihyperos/documentation/detail?pId=2026

    对于Flip设备而言（摄像头在左上位置），
    Flip设备外屏size = 1208 * 1392
    且DisplayCutout区域为左上角Rect(0, 0 - 398, 728)

# 资源与配置文件，线索入口
1. config_secondaryBuiltInDisplayCutout 资源

    来源：/product/overlay/DevicesAndroidOverlay.apk 中的 res/values/config.xml

    <string name="config_secondaryBuiltInDisplayCutout">M 604,664 L 604,1392 L 206,1392 L 206,664 Z @bind_right_cutout</string>

    对应资源 ID：0x7f070003。(十进制2131165187)

    作用：定义外屏的物理挖孔区域（SVG 路径）。修改此字符串为零区域（M 0,0 L 0,0 L 0,0 L 0,0 Z @bind_right_cutout）可移除 cutout，其面积为398x728。
   - framework-res.apk:

    <public type="string" name="config_secondaryBuiltInDisplayCutout" id="0x0104032f" />

    <public type="string" name="config_secondaryBuiltInDisplayCutoutRectApproximation" id="0x01040330" />

    <public type="array" name="config_secondaryBuiltInDisplayCutoutSideOverride" id="0x010700ce" />

    <public type="bool" name="config_fillSecondaryBuiltInDisplayCutout" id="0x011101ae" />

    <public type="bool" name="config_maskSecondaryBuiltInDisplayCutout" id="0x011101f4" />

    <string name="config_secondaryBuiltInDisplayCutout" />
    <string name="config_secondaryBuiltInDisplayCutoutRectApproximation">@string/config_secondaryBuiltInDisplayCutout</string>

    	<bool name="config_fillSecondaryBuiltInDisplayCutout">false</bool>
    	<bool name="config_maskSecondaryBuiltInDisplayCutout">false</bool> 


    <string name="config_secondaryBuiltInDisplayCutout">M 604,664 L 604,1392 L 206,1392 L 206,664 Z @bind_right_cutout</string>

    对应资源 ID：0x7f070003。(十进制2131165187)
   - framework.jar
	        public static final int config_secondaryBuiltInDisplayCutoutSideOverride = 0x010700ce; 17236174
        public static final int config_fillSecondaryBuiltInDisplayCutout = 0x011101ae; 17891758
        public static final int config_maskSecondaryBuiltInDisplayCutout = 0x011101f4;  17891828
        public static final int config_secondaryBuiltInDisplayCutout = 0x0104032f;  17040175
        public static final int config_secondaryBuiltInDisplayCutoutRectApproximation = 0x01040330;  17040176

    interface ServiceInfoChangeListener {
        void onServiceInfoChangedLocked(AccessibilityUserState accessibilityUserState);
    }

    boolean isValidMagnificationModeLocked(int displayId) {
        int mode = getMagnificationModeLocked(displayId);
        return (this.mSupportWindowMagnification || mode != 2) && (this.mMagnificationCapabilities & mode) != 0;
    }

    AccessibilityUserState(int userId, Context context, ServiceInfoChangeListener serviceInfoChangeListener) {
        boolean z = false;
        this.mUserId = userId;
        this.mContext = context;
        this.mServiceInfoChangeListener = serviceInfoChangeListener;
        this.mFocusStrokeWidthDefaultValue = this.mContext.getResources().getDimensionPixelSize(R.dimen.accessibility_touch_slop);
        this.mFocusColorDefaultValue = this.mContext.getResources().getColor(R.color.accessibility_focus_highlight_color);
        this.mFocusStrokeWidth = this.mFocusStrokeWidthDefaultValue;
        this.mFocusColor = this.mFocusColorDefaultValue;
        if (this.mContext.getResources().getBoolean(R.bool.config_maskSecondaryBuiltInDisplayCutout) && this.mContext.getPackageManager().hasSystemFeature("android.software.window_magnification")) {
            z = true;
        }
        this.mSupportWindowMagnification = z;
        this.mShortcutTargets.put(2, new ArraySet<>());
        this.mShortcutTargets.put(1, new ArraySet<>());
        this.mShortcutTargets.put(32, new ArraySet<>());
        this.mShortcutTargets.put(16, new ArraySet<>());
        this.mShortcutTargets.put(64, new ArraySet<>());
    }


3. mpa.xml 多显示器策略文件

    路径：/product/etc/mpa.xml

    <contents>
        <designated-package name="com.miui.home" />
        <designated-package name="com.android.systemui" />
        <designated-package name="com.miui.fliphome" />
    </contents>

    作用：定义外屏可用的特权应用。移除外屏桌面条目可让系统使用内屏桌面。

4. display_layout_configuration.xml 显示布局文件

    路径：/odm/etc/displayconfig/display_layout_configuration.xml

    关键内容：根据设备状态（state 0~6）启用不同地址的屏幕。外屏地址 4630947108695800452，内屏地址 4630947227182689923。

    作用：控制折叠/展开时使用哪个显示器，可设置为同时显示。

5. device_state_configuration.xml

    路径：/odm/etc/devicestate/device_state_configuration.xml 和 /product/etc/devicestate/device_state_configuration.xml

    作用：定义传感器值与设备状态（折叠/展开）的映射。

6. continuity_list.json 连续性配置文件

# 反编译范围：
    - miui-appcompat.appcontinuity.jar
      package com.android.server.wm;

      public class AppContinuityRouterImpl extends AppContinuityRouterStub {
      private static final String MIUI_HOME_SHORT_COMPONENT = "com.miui.home/.launcher.Launcher";

          public boolean isKeepContinuityInFold() {
        InterceptActivityController interceptActivityController;
        if (this.mContinuityPolicyService == null || (interceptActivityController = this.mContinuityPolicyService.getInterceptActivityController()) == null) {
            return false;
        }
        return interceptActivityController.isKeepContinuityInFold();
    }

    public boolean isInSuperPowerProcessingState() {
        InterceptActivityController interceptActivityController;
        if (this.mContinuityPolicyService == null || (interceptActivityController = this.mContinuityPolicyService.getInterceptActivityController()) == null) {
            return false;
        }
        return interceptActivityController.isInSuperPowerProcessingState();
    }

    public int getContinuityPolicy(String packageNameOrComponentName) {
        if (this.mContinuityPolicyService == null) {
            Slog.w("AppContinuityRouterStub", "onForegroundActivityChangedLocked mNotificationPolicyService is null.");
            return 0;
        }
        return this.mContinuityPolicyService.getContinuityPolicy(packageNameOrComponentName);
    }

    public int getVisualCloudDatatVersion() {
        if (this.mContinuityPolicyService == null) {
            Slog.w("AppContinuityRouterStub", "getUpdateThirdPartyAppsPropIndex mNotificationPolicyService is null.");
            return 0;
        }
        InterceptActivityController interceptActivityController = this.mContinuityPolicyService.getInterceptActivityController();
        if (interceptActivityController == null) {
            return 0;
        }
        return interceptActivityController.getUpdateThirdAppsPropVersion();
    }

    public int getLocalPolicyCommandAllowStartVersion() {
        if (this.mContinuityPolicyService == null) {
            Slog.w("AppContinuityRouterStub", "getLocalPolicyCommandAllowStartVersion mNotificationPolicyService is null.");
            return 0;
        }
        InterceptActivityController interceptActivityController = this.mContinuityPolicyService.getInterceptActivityController();
        if (interceptActivityController == null) {
            return 0;
        }
        return interceptActivityController.getLocalPolicyCommandAllowStartVersion();
    }

    public List<String> getAllowStartContinuityPackageNameList() {
        if (this.mContinuityPolicyService == null) {
            Slog.w("AppContinuityRouterStub", "getAllowStartContinuityPackageNameList mNotificationPolicyService is null.");
            return ApplicationCompatManager.getInstance().getAllowStartContinuityPackageNameList();
        }
        return this.mContinuityPolicyService.getAllowStartContinuityPackageNameList();
    }

    public List<String> getAllowStartContinuityComponentNameList() {
        if (this.mContinuityPolicyService == null) {
            Slog.w("AppContinuityRouterStub", "getAllowStartContinuityComponentNameList mNotificationPolicyService is null.");
            return ApplicationCompatManager.getInstance().getAllowStartContinuityComponentNameList();
        }
        return this.mContinuityPolicyService.getAllowStartContinuityComponentNameList();
    }

    public List<String> getNotAllowStartContinuityPackageNameList() {
        if (this.mContinuityPolicyService == null) {
            Slog.w("AppContinuityRouterStub", "getNotAllowStartContinuityPackageNameList mNotificationPolicyService is null.");
            return ApplicationCompatManager.getInstance().getNotAllowStartContinuityPackageNameList();
        }
        return this.mContinuityPolicyService.getNotAllowStartContinuityPackageNameList();
    }

    public List<String> getNotAllowStartContinuityComponentNameList() {
        if (this.mContinuityPolicyService == null) {
            Slog.w("AppContinuityRouterStub", "getNotAllowStartContinuityComponentNameList mNotificationPolicyService is null.");
            return ApplicationCompatManager.getInstance().getNotAllowStartContinuityComponentNameList();
        }
        return this.mContinuityPolicyService.getNotAllowStartContinuityComponentNameList();
    }

      package com.android.server.wm;
      public class ContinuityManagerService extends IContinuityManager.Stub {
      private static final String TAG = "ContinuityManagerService";
      public long getContinuityVersion() {
        int visualCloudDatatVersion = AppContinuityRouterImpl.getInstance().getVisualCloudDatatVersion();
        int localPolicyCommandAllowStartVersion = AppContinuityRouterImpl.getInstance().getLocalPolicyCommandAllowStartVersion();
        if (this.mContinuityPolicyService == null) {
            return 1199910L;
        }
        if (visualCloudDatatVersion != 0 || localPolicyCommandAllowStartVersion != 0) {
            long continuityVersion = this.mContinuityPolicyService.getContinuityVersion();
            Slog.w(TAG, "continuityVersion=" + continuityVersion + " visualCloudDatatVersion=" + visualCloudDatatVersion + " localPolicyCommandAllowStartVersion=" + localPolicyCommandAllowStartVersion);
            return ((long) visualCloudDatatVersion) + continuityVersion + ((long) localPolicyCommandAllowStartVersion);
        }
        return this.mContinuityPolicyService.getContinuityVersion();
    }

    public List<String> getContinuityPackages(String policyName, String callerPackageName) {
        try {
            if (checkInvokerAccessPermissions(callerPackageName) && "app_intercept_allowlist".equals(policyName)) {
                ArrayList<String> packageNamesList = new ArrayList<>();
                ArrayList<String> finalPackageNamesList = new ArrayList<>();
                Set<String> packageNamesSet = new ArraySet<>();
                packageNamesList.addAll(ApplicationCompatManager.getInstance().getAllowStartContinuityPackageNameList());
                packageNamesList.removeAll(AppContinuityRouterImpl.getInstance().getNotAllowStartContinuityPackageNameList());
                packageNamesList.addAll(this.mPackageNamesList);
                if (this.mContinuityPolicyService != null) {
                    packageNamesList.addAll(IApplicationCompat.ALLOW_START_LIST);
                    packageNamesList.removeAll(IApplicationCompat.INTERCEPT_LIST);
                }
                if (this.mContinuityPolicyService != null) {
                    Set<String> localPolicyyCommandSet = this.mContinuityPolicyService.getLocalPolicyByCommandMap();
                    packageNamesList.addAll(localPolicyyCommandSet);
                }
                packageNamesSet.addAll(packageNamesList);
                finalPackageNamesList.addAll(packageNamesSet);
                this.mFinalPackageNamesList = finalPackageNamesList;
                return finalPackageNamesList;
            }
        } catch (Exception e) {
            Slog.w(TAG, "Something is wrong:", e);
        }
        Slog.w(TAG, "caller is invalid.");
        return new ArrayList();
    }

    public List<String> getContinuityActivities(String policyName, String callerPackageName) {
        try {
            if (checkInvokerAccessPermissions(callerPackageName) && "app_intercept_component_allowlist".equals(policyName)) {
                ArrayList<String> componentNamesList = new ArrayList<>();
                ArrayList<String> finalComponentNamesList = new ArrayList<>();
                Set<String> componentNamesSet = new ArraySet<>();
                componentNamesList.addAll(ApplicationCompatManager.getInstance().getAllowStartContinuityComponentNameList());
                componentNamesList.removeAll(AppContinuityRouterImpl.getInstance().getNotAllowStartContinuityComponentNameList());
                componentNamesList.addAll(this.mComponentNamesList);
                if (this.mContinuityPolicyService != null) {
                    componentNamesList.addAll(IApplicationCompat.ALLOW_START_COMPONENT_LIST);
                    componentNamesList.removeAll(IApplicationCompat.INTERCEPT_COMPONENT_LIST);
                }
                componentNamesSet.addAll(componentNamesList);
                finalComponentNamesList.addAll(componentNamesSet);
                this.mFinalComponentNamesList = componentNamesList;
                InterceptActivityController interceptActivityController = this.mContinuityPolicyService != null ? this.mContinuityPolicyService.getInterceptActivityController() : null;
                if (interceptActivityController != null) {
                    interceptActivityController.updateFlipComponentNamesList(this.mFinalComponentNamesList);
                }
                return componentNamesList;
            }
        } catch (Exception e) {
            Slog.w(TAG, "Something is wrong:", e);
        }
        Slog.w(TAG, "policyName or caller is invalid.");
        return new ArrayList();
    }
      package com.android.server.wm;
      public class ContinuityPolicyService extends ApplicationCompatPolicy {
    private static final String CONTINUITY_CHANNEL_ID = "Continuity";
    private static final int CONTINUITY_NOTIFICATION_ID = 9990;
    private static final String LOCAL_UPDATE_REASON = "local list";
      public /* synthetic */ boolean lambda$new$0(String command, String[] args, PrintWriter pw) {
        if (TextUtils.isEmpty(args[0]) || args.length != 2 || (!"blocklist".equals(args[1]) && !"allowlist".equals(args[1]) && !"allowstart".equals(args[1]) && !"restartlist".equals(args[1]) && !"relaunchlist".equals(args[1]) && !"relaunch".equals(args[1]) && !"block".equals(args[1]) && !"notrelaunch".equals(args[1]) && !"restart".equals(args[1]) && !"accessibility_notrelaunch".equals(args[1]) && !"interceptlist".equals(args[1]) && !"clear".equals(args[1]) && !"list".equals(args[1]))) {
            if ("debug".equals(args[0])) {
                if ("true".equals(args[1])) {
                    pw.println("FoldContinuity: debug log opened.");
                    DEBUG = true;
                } else if ("false".equals(args[1])) {
                    pw.println("FoldContinuity: debug log closed.");
                    DEBUG = false;
                } else {
                    pw.println("FoldContinuity: Set debug log illegal.");
                }
                return true;
            }
            if ("local".equals(args[0])) {
                if ("true".equals(args[1])) {
                    pw.println("FoldContinuity: local log opened.");
                    LOCAL = true;
                    if (isRoot && LOCAL) {
                        CONTINUITY_ALLOW_LIST.clear();
                        CONTINUITY_BLOCK_LIST.clear();
                        CONTINUITY_RESTART_LIST.clear();
                        CONTINUITY_RELAUNCH_LIST.clear();
                        CONTINUITY_COMPONENT_RELAUNCH_LIST.clear();
                        CONTINUITY_COMPONENT_BLOCK_LIST.clear();
                        CONTINUITY_COMPONENT_NOTRELAUNCH_LIST.clear();
                        CONTINUITY_COMPONENT_RESTART_LIST.clear();
                        CONTINUITY_COMPONENT_ACCESSIBILITY_NOTRELAUNCH_LIST.clear();
                        LocalDataController.getInstance().updatePolicyFromLocal(LOCAL_UPDATE_REASON);
                        Slog.i(TAG, "local list take effect! isRoot = " + isRoot + ", LOCAL = " + LOCAL);
                    }
                } else if ("false".equals(args[1])) {
                    pw.println("FoldContinuity: local log closed.");
                } else {
                    pw.println("FoldContinuity: Set local log illegal.");
                }
                return true;
            }
            if ("version".equals(args[0])) {
                if ("true".equals(args[1])) {
                    pw.println("FoldContinuity: version log opened.");
                    Slog.i(TAG, "ContinuityVersion: " + getContinuityVersion());
                    pw.println("ContinuityVersion: " + getContinuityVersion());
                } else if ("false".equals(args[1])) {
                    pw.println("FoldContinuity: version log closed.");
                } else {
                    pw.println("FoldContinuity: Set version log illegal.");
                }
                return true;
            }
            pw.println(command + " options requires:");
            pw.println(command + " [packageName] [blocklist|allowstart|allowlist|restartlist|relaunchlist|relaunch|block|notrelaunch|restart|accessibility_notrelaunch|interceptlist|clear]");
            pw.println(command + " [packageName:packageName:...] [blocklist|allowstart|allowlist|restartlist|relaunchlist|relaunch|block|notrelaunch|restart|accessibility_notrelaunch|interceptlist|clear]");
        } else {
            String argsPackages = args[0];
            if (argsPackages.contains(":")) {
                String[] packageNames = args[0].split(":");
                for (String str : packageNames) {
                    localPolicyProcess(pw, args, str);
                }
            } else {
                String packageName = args[0];
                localPolicyProcess(pw, args, packageName);
            }
        }
        return true;
    }

       private void handleClear(PrintWriter pw, String packageName) {
        if (!LOCAL_POLICY_BY_COMMAND.containsKey(packageName)) {
            pw.println(packageName + " not config.");
            return;
        }
        String removed = (String) LOCAL_POLICY_BY_COMMAND.remove(packageName);
        boolean isRemove = LOCAL_COMMAND_ALLOW_START_SET.remove(packageName);
        if (isRemove && this.mInterceptActivityController != null) {
            this.mInterceptActivityController.updateLocalPolicyCommandAllowStartVersion();
            if (DEBUG) {
                Slog.d(TAG, "adb setForceDisplayCompatMode allowstart clear:" + packageName + " allow start isRemove=" + isRemove + ", local policy removed=" + removed);
            }
        }
        pw.println(packageName + " config clear isRemove=" + isRemove + ", local policy removed=" + removed);
        AppCompatTask appCompatTask = createAppCompatTask(packageName);
        appCompatTask.clearActivityRelaunch(packageName);
        appCompatTask.clearActivityBlock(packageName);
        appCompatTask.clearActivityNotRelaunch(packageName);
        appCompatTask.clearActivityRestart(packageName);
        appCompatTask.clearActivityAccessibilityNotRelaunch(packageName);
        notifyActivityStart(appCompatTask);
    }

    private void handleSet(PrintWriter pw, String[] args, String packageName) {
        if (args != null && args.length > 1) {
            LOCAL_POLICY_BY_COMMAND.put(packageName, args[1]);
        } else {
            Slog.e(TAG, "Invalid args length for package: " + packageName);
        }
        String policy = (String) LOCAL_POLICY_BY_COMMAND.get(packageName);
        if ("allowstart".equals(policy)) {
            boolean isAdd = LOCAL_COMMAND_ALLOW_START_SET.add(packageName);
            if (isAdd && this.mInterceptActivityController != null) {
                this.mInterceptActivityController.updateLocalPolicyCommandAllowStartVersion();
            }
        }
        AppCompatTask appCompatTask = createAppCompatTask(packageName);
        setActivityRelaunch(appCompatTask, packageName);
        setActivityBlock(appCompatTask, packageName);
        setActivityNotRelaunch(appCompatTask, packageName);
        setActivityRestart(appCompatTask, packageName);
        setActivityAccessibilityNotRelaunch(appCompatTask, packageName);
        if (DEBUG && this.mInterceptActivityController != null) {
            Slog.d(TAG, "adb setForceDisplayCompatMode applied policy:" + policy + " appCompatTask=" + appCompatTask);
        }
        pw.println(packageName + " applied policy:" + policy);
        notifyActivityStart(appCompatTask);
    }

    private void listPolicy(PrintWriter pw, String pkgName) {
        pw.println(pkgName + " 配置: id = " + getContinuityVersion() + ", local = " + LOCAL);
        checkAndPrintList(pw, CONTINUITY_ALLOW_LIST, pkgName, "app_continuity_whitelist");
        checkAndPrintList(pw, CONTINUITY_BLOCK_LIST, pkgName, "app_continuity_blacklist");
        checkAndPrintList(pw, CONTINUITY_RESTART_LIST, pkgName, "app_restart_blacklist");
        checkAndPrintList(pw, CONTINUITY_RELAUNCH_LIST, pkgName, "app_relaunch_blacklist");
        checkAndPrintList(pw, INTERCEPT_LIST, pkgName, "app_intercept_blacklist");
        checkAndPrintList(pw, ALLOW_START_LIST, pkgName, "app_intercept_allowlist");
        checkAndPrintComponentList(pw, CONTINUITY_COMPONENT_RELAUNCH_LIST, pkgName, "app_activity_relaunch_blacklist");
        checkAndPrintComponentList(pw, CONTINUITY_COMPONENT_BLOCK_LIST, pkgName, "app_activity_block_blacklist");
        checkAndPrintComponentList(pw, CONTINUITY_COMPONENT_NOTRELAUNCH_LIST, pkgName, "app_activity_notrelaunch_blacklist");
        checkAndPrintComponentList(pw, CONTINUITY_COMPONENT_RESTART_LIST, pkgName, "app_activity_restart_blacklist");
        checkAndPrintComponentList(pw, CONTINUITY_COMPONENT_ACCESSIBILITY_NOTRELAUNCH_LIST, pkgName, "app_activity_accessibility_notrelaunch_blacklist");
        checkAndPrintComponentList(pw, INTERCEPT_COMPONENT_LIST, pkgName, "app_intercept_component_blacklist");
        checkAndPrintComponentList(pw, ALLOW_START_COMPONENT_LIST, pkgName, "app_intercept_component_allowlist");
    }
      public ContinuityPolicyService(ActivityTaskManagerService service) {
        super(service);
        this.mIsThirdPartyAppsCanStart = false;
        this.mIcon = null;
        this.mAppContinuityEventCallbacks = new ArrayList();
        this.mNotificationShellCommand = new ApplicationCompatPolicy.IShellCommand() { // from class: com.android.server.wm.ContinuityPolicyService$$ExternalSyntheticLambda0
            public final boolean executeShellCommand(String str, String[] strArr, PrintWriter printWriter) {
                return this.f$0.lambda$new$0(str, strArr, printWriter);
            }
        };
        this.mResolver = this.mContext.getContentResolver();
        if (ApplicationCompatUtilsStub.get().isDialogContinuityEnabled()) {
            if (USE_LEGACY_LAUNCHER_SWITCH_CONTROLLER) {
                this.mLauncherSwitchController = new LauncherSwitchController(this, service);
            } else {
                this.mLauncherSwitchController = new LauncherSwitchController2(this, service);
            }
            this.mInterceptActivityController = new InterceptActivityController(this, service);
            this.mAppContinuityEventCallbacks.add(this.mLauncherSwitchController);
            this.mAppContinuityEventCallbacks.add(this.mInterceptActivityController);
            this.ENABLED = true;
            return;
        }
        this.mLauncherSwitchController = null;
        this.mInterceptActivityController = null;
        this.mAppContinuityEventCallbacks.clear();
        this.ENABLED = false;
    }

    public void onBootPhase(final int phase) {
        super.onBootPhase(phase);
        if (phase == 500) {
            this.mContinuityTrack = new ContinuityTrackManager(this.mContext);
            ((DeviceStateManager) this.mContext.getSystemService(DeviceStateManager.class)).registerCallback(new HandlerExecutor(this.mService.mUiHandler), new DeviceStateManager.FoldStateListener(this.mContext, new Consumer() { // from class: com.android.server.wm.ContinuityPolicyService$$ExternalSyntheticLambda11
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.onDeviceStateChanged(((Boolean) obj).booleanValue());
                }
            }));
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
            this.mStatusBarService = (StatusBarManager) this.mContext.getSystemService("statusbar");
            ContentObserver mMiuiSettingsObserver = new ContentObserver(this.mService.mUiHandler) { // from class: com.android.server.wm.ContinuityPolicyService.1
                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange, Uri uri) {
                    byte b;
                    String lastPathSegment = uri.getLastPathSegment();
                    switch (lastPathSegment.hashCode()) {
                        case 1384083403:
                            if (lastPathSegment.equals("device_provisioned")) {
                                b = 0;
                                break;
                            }
                        default:
                            b = -1;
                            break;
                    }
                    switch (b) {
                        case 0:
                            ContinuityPolicyService.this.mIsProvisioned = Settings.Global.getInt(ContinuityPolicyService.this.mResolver, "device_provisioned", 0) != 0;
                            break;
                    }
                }
            };
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, mMiuiSettingsObserver, -1);
        }
        if (phase == 600) {
            this.mIcon = getAppIcon("com.android.settings");
            this.mIsProvisioned = Settings.Global.getInt(this.mResolver, "device_provisioned", 0) != 0;
            this.mIsThirdPartyAppsCanStart = true;
            registerShellCommand("-setForceDisplayCompatMode", this.mNotificationShellCommand);
        }
        this.mAppContinuityEventCallbacks.forEach(new Consumer() { // from class: com.android.server.wm.ContinuityPolicyService$$ExternalSyntheticLambda12
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((IAppContinuityEventCallback) obj).onBootPhase(phase);
            }
        });
    }


      package com.android.server.wm;

      class InterceptActivityController implements IAppContinuityEventCallback {
       private static final String CAR_LINK_PACKAGE_NAME = "com.miui.carlink";
       private static final String COLUMN_ENABLE = "enable";
       private static final String COLUMN_PKG_NAME = "pkgName";
       private static final String COLUMN_USER_ID = "userId";
       private static final String FLIP_CONTINUITY_ENABLED = "flip_continuity_enabled";
       private static final int OPEN_CONTINUITY = 1;
       private static final String TABLE_NAME = "packages";
         
             int getLocalPolicyCommandAllowStartVersion() {
        return this.mLocalPolicyCommandIndex;
    }

    void updateLocalPolicyCommandAllowStartVersion() {
        this.mLocalPolicyCommandIndex += OPEN_CONTINUITY;
    }
       private boolean isPackageNameSupportFlip(String packageName) {
        ArrayList<String> finalPackageNamesList = new ArrayList<>();
        ArrayList<String> packageNamesList = new ArrayList<>();
        Set<String> packageNamesSet = new ArraySet<>();
        packageNamesList.addAll(ApplicationCompatManager.getInstance().getAllowStartContinuityPackageNameList());
        packageNamesList.removeAll(AppContinuityRouterImpl.getInstance().getNotAllowStartContinuityPackageNameList());
        packageNamesList.addAll(Arrays.asList(this.mContext.getResources().getStringArray(285409387)));
        packageNamesList.addAll(ContinuityPolicyService.ALLOW_START_LIST);
        packageNamesList.removeAll(ContinuityPolicyService.INTERCEPT_LIST);
        Set<String> localPolicyyCommandSet = this.mPolicy.getLocalPolicyByCommandMap();
        packageNamesList.addAll(localPolicyyCommandSet);
        packageNamesSet.addAll(packageNamesList);
        finalPackageNamesList.addAll(packageNamesSet);
        return finalPackageNamesList.contains(packageName);
    }

      boolean isInterceptListUnCheckFold(ComponentName activityComponent) {
        if (activityComponent == null) {
            return false;
        }
        String packageName = activityComponent.getPackageName();
        String componentName = activityComponent.flattenToShortString();
        if ((ContinuityPolicyService.LOCAL_POLICY_BY_COMMAND.containsKey(packageName) && "allowstart".equals(ContinuityPolicyService.LOCAL_POLICY_BY_COMMAND.get(packageName))) || (ContinuityPolicyService.LOCAL_POLICY_BY_COMMAND.containsKey(componentName) && "allowstart".equals(ContinuityPolicyService.LOCAL_POLICY_BY_COMMAND.get(componentName)))) {
            Slog.d(TAG, "isInterceptList: setForceDisplayCompatMode allow start with app " + packageName);
            return false;
        }
        if ((ContinuityPolicyService.LOCAL_POLICY_BY_COMMAND.containsKey(packageName) && "interceptlist".equals(ContinuityPolicyService.LOCAL_POLICY_BY_COMMAND.get(packageName))) || (ContinuityPolicyService.LOCAL_POLICY_BY_COMMAND.containsKey(componentName) && "interceptlist".equals(ContinuityPolicyService.LOCAL_POLICY_BY_COMMAND.get(componentName)))) {
            Slog.d(TAG, "isInterceptList: setForceDisplayCompatMode intercept start with app " + packageName);
            return true;
        }
        Pair<Boolean, Boolean> isInterceptListForPropertyPair = isInterceptListForProperty(activityComponent, packageName);
        if (((Boolean) isInterceptListForPropertyPair.first).booleanValue()) {
            return ((Boolean) isInterceptListForPropertyPair.second).booleanValue();
        }
        if (ContinuityPolicyService.INTERCEPT_COMPONENT_LIST.contains(componentName) || ContinuityPolicyService.INTERCEPT_LIST.contains(packageName)) {
            Slog.d(TAG, "isInterceptList: cloud list intercept start with app " + packageName);
            return true;
        }
        if (ContinuityPolicyService.ALLOW_START_COMPONENT_LIST.contains(componentName) || ContinuityPolicyService.ALLOW_START_LIST.contains(packageName) || INTERCEPT_ALLOW_LIST.contains(packageName) || INTERCEPT_COMPONENT_ALLOW_LIST.contains(componentName)) {
            Slog.d(TAG, "isInterceptList: cloud or local list allow start with app " + packageName);
            return false;
        }
        if (!ApplicationCompatUtilsStub.get().isDialogContinuityEnabled()) {
            return false;
        }
        Slog.d(TAG, "isInterceptList: dialog continuity feature default interceptstart with app " + packageName + " and " + componentName);
        return true;
    }

    Pair<Boolean, Boolean> isInterceptListForProperty(ComponentName activityComponent, String packageName) {
        String componentName = activityComponent.flattenToShortString();
        if (this.mPolicy.hasPropertyByActivity("miui.continuity.policy", activityComponent)) {
            Slog.d(TAG, "isInterceptList: Activity config miui.continuity.policy componentName=" + componentName);
            if (this.mPolicy.getAllowStartContinuityComponentNameList().contains(componentName)) {
                Slog.d(TAG, "isInterceptList: config miui.continuity.policy allow start with activity " + componentName);
                return new Pair<>(true, false);
            }
            if (this.mPolicy.getPropertyIntByActivity("miui.continuity.policy", activityComponent) == 5) {
                Slog.d(TAG, "isInterceptList: config miui.continuity.policy allow start with activity " + componentName);
                return new Pair<>(true, false);
            }
            if (this.mPolicy.getNotAllowStartContinuityComponentNameList().contains(componentName)) {
                Slog.d(TAG, "isInterceptList: config miui.continuity.policy intercept start with activity " + componentName);
                return new Pair<>(true, true);
            }
            if (this.mPolicy.getPropertyIntByActivity("miui.continuity.policy", activityComponent) == 4) {
                Slog.d(TAG, "isInterceptList: config miui.continuity.policy intercept start with activity " + componentName);
                return new Pair<>(true, true);
            }
        } else if (this.mPolicy.hasPropertyByApplication("miui.continuity.policy", packageName)) {
            Slog.d(TAG, "isInterceptList: Application config miui.continuity.policy packageName=" + packageName);
            if (this.mPolicy.getAllowStartContinuityPackageNameList().contains(packageName)) {
                Slog.d(TAG, "isInterceptList: config miui.continuity.policy allow start with app " + packageName);
                return new Pair<>(true, false);
            }
            if (this.mPolicy.getPropertyIntByApplication("miui.continuity.policy", packageName) == 5) {
                Slog.d(TAG, "isInterceptList: config miui.continuity.policy allow start with app " + packageName);
                return new Pair<>(true, false);
            }
            if (this.mPolicy.getNotAllowStartContinuityPackageNameList().contains(packageName)) {
                Slog.d(TAG, "isInterceptList: config miui.continuity.policy intercept start with app " + packageName);
                return new Pair<>(true, true);
            }
            if (this.mPolicy.getPropertyIntByApplication("miui.continuity.policy", packageName) == 4) {
                Slog.d(TAG, "isInterceptList: config miui.continuity.policy intercept start with app " + packageName);
                return new Pair<>(true, true);
            }
        }
        return new Pair<>(false, false);
    }

    private void startActivityInnerBlockedWithSnapShotInUnFold(AppCompatTask.StartActivityInnerValuesSnapShot snapShot) throws Throwable {
        AppCompatTask.StartActivityInnerValuesSnapShot startActivityInnerValuesSnapShot;
        this.mStartActivityTipView.hide();
        synchronized (this.mService.mGlobalLock) {
            try {
            } catch (Throwable th) {
                th = th;
                throw th;
            }
            try {
                ActivityStarter activityStarter = snapShot.getActivityStarter();
                ActivityRecord targetRecord = snapShot.getTargetRecord();
                ActivityStarter.Request request = snapShot.getRequest();
                try {
                    startActivityInnerValuesSnapShot = snapShot;
                    try {
                        Slog.d(TAG, "startActivityInnerBlockedWithSnapShotInUnFold: snapShot=" + startActivityInnerValuesSnapShot);
                        int resultCode = activityStarter.executeRequest(request);
                        Slog.d(TAG, "startActivityInnerBlockedWithSnapShotInUnFold: executeRequest resultCode=" + resultCode);
                    } catch (IllegalArgumentException e) {
                        e = e;
                        IllegalArgumentException iae = e;
                        if (targetRecord != null) {
                            try {
                                if (request.intent != null && request.resultTo != null && request.resultWho != null) {
                                    int resultCode2 = this.mService.startActivityAsCaller(request.caller, request.callingPackage, request.intent, request.resolvedType, request.resultTo, request.resultWho, request.requestCode, request.startFlags, request.profilerInfo, targetRecord.mOptions, request.ignoreTargetSecurity, request.userId);
                                    Slog.e(TAG, "startActivityInnerBlockedWithSnapShotInUnFold: startActivityAsCaller resultCode=" + resultCode2);
                                } else {
                                    this.mContext.startActivityAsUser(targetRecord.intent, targetRecord.mOptions, UserHandle.of(targetRecord.mUserId));
                                    Slog.e(TAG, "startActivityInnerBlockedWithSnapShotInUnFold: startActivityAsUser targetRecord=" + targetRecord);
                                }
                            } catch (Exception e2) {
                                Slog.e(TAG, "catch startActivityAsCaller Exception:", e2);
                                startActivityInnerValuesSnapShot.reset();
                            }
                        }
                        Slog.e(TAG, "catch executeRequest IllegalArgumentException:", iae);
                    } catch (Exception e3) {
                        e = e3;
                        Slog.e(TAG, "catch executeRequest Exception:", e);
                        startActivityInnerValuesSnapShot.reset();
                    }
                } catch (IllegalArgumentException e4) {
                    e = e4;
                    startActivityInnerValuesSnapShot = snapShot;
                } catch (Exception e5) {
                    e = e5;
                    startActivityInnerValuesSnapShot = snapShot;
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }





    - com.miui.fliphome.apk
        
        package com.miui.fliphome.widget;
        public class WatchOverlayWindow implements LifecycleOwner {
        private static final int ACTION_ADD_WINDOW = 1;
        private static final int ACTION_REMOVE_WINDOW = 2;
        public static final int GESTURE_END_HOME = 3;
        public static final int GESTURE_START = 2;
        public static final int GESTURE_UNDEFINED = 1;
        
        private void checkShouldHideWidget(PackageManager packageManager, ComponentName component) {
        String packageName = component.getPackageName();
        Object objCallObjectMethod = ReflectUtils.callObjectMethod(packageManager, Object.class, "getProperty", new Class[]{String.class, ComponentName.class}, "miui.supportFlipWatchOverlayGroupView", component);
        if (objCallObjectMethod == null) {
        objCallObjectMethod = ReflectUtils.callObjectMethod(packageManager, Object.class, "getProperty", new Class[]{String.class, String.class}, "miui.supportFlipWatchOverlayGroupView", packageName);
        }
        if (objCallObjectMethod == null) {
        setSupportWatchForNull();
        return;
        }
        if (ReflectUtils.callObjectMethod(objCallObjectMethod, Object.class, "getBoolean", new Class[0], new Object[0]) instanceof Boolean) {
        postRefreshWindow(packageName, !((Boolean) r9).booleanValue());
        }
        }
        相关调用：
        @Override // java.lang.Runnable
        public void run() {
        checkShouldHideWidget(this.packageManager, this.component);
        }

        private void postRefreshWindow(final String packageName, final boolean isHideAppForeground) {
            Executors.MAIN_EXECUTOR.execute(new Runnable() { // from class: com.miui.fliphome.widget.WatchOverlayWindow$CheckAppConfigRunnable$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m242xe336509c(packageName, isHideAppForeground);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$postRefreshWindow$0$com-miui-fliphome-widget-WatchOverlayWindow$CheckAppConfigRunnable, reason: not valid java name */
        /* synthetic */ void m242xe336509c(String str, boolean z) {
            WatchOverlayWindow watchOverlayWindow;
            if (this.handler.hasCallbacks(this) || (watchOverlayWindow = this.windowWeakReference.get()) == null) {
                return;
            }
            watchOverlayWindow.mLastOtherAppPackage = str;
            watchOverlayWindow.mIsCurrentActivitySupport = Boolean.valueOf(!z);
            if (!this.component.getPackageName().equals(watchOverlayWindow.mCurrentAppPackage) || watchOverlayWindow.mIsHideAppForeground == z) {
                return;
            }
            watchOverlayWindow.mIsHideAppForeground = z;
            watchOverlayWindow.refreshWindow(z ? 2 : 1, true);
        }

        private void setSupportWatchForNull() {
            Executors.MAIN_EXECUTOR.execute(new Runnable() { // from class: com.miui.fliphome.widget.WatchOverlayWindow$CheckAppConfigRunnable$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m243xecde1c1e();
                }
            });
        }



    
      package com.miui;
      public class FlipLauncherFragment extends Fragment implements IAnimatorSource {
      private static final String TAG = "FlipLauncherFragment";
    
      @Override // com.miui.fliphome.animator.IAnimatorSource
      public View getGroupView() {
      return this.mRecycleView;
      }
    
    package com.miui.fliphome.animator;
    
    import android.view.View;
    import java.util.List;
    
    /* JADX INFO: loaded from: classes.dex */
    public interface IAnimatorSource {
    List<View> getAnimatorViews();
    
        View getGroupView();
    }
    
    
    package com.miui.fliphome.widget.ui;
    
    import android.content.Context;
    import android.content.res.Configuration;
    import android.util.AttributeSet;
    import android.view.MotionEvent;
    import android.view.ViewGroup;
    import android.view.ViewOverlay;
    import android.view.WindowManager;
    import android.widget.FrameLayout;
    import com.android.systemui.shared.recents.utilities.Utilities;
    import com.miui.creation.logger.FLLog;
    import com.miui.fliphome.FlipApplication;
    import com.miui.fliphome.FlipLauncher;
    import com.miui.fliphome.R;
    import com.miui.fliphome.utils.DeviceConfig;
    import com.miui.fliphome.utils.PerformLaunchAction;
    import com.miui.fliphome.utils.ReflectUtils;
    import java.lang.reflect.Field;
    
    /* JADX INFO: loaded from: classes.dex */
    public class WatchOverlayGroupView extends FrameLayout {
    public static final int EXTRA_FLAG_SHOW_WITH_KEYGUARD_GOING_AWAY;
    public static final int PRIVATE_FLAG_NO_MOVE_ANIMATION = 64;
    private static final String TAG = "WatchOverlayWindow";
    private final int TYPE_LAUNCHER_OVERLAY_WINDOW;
    private boolean mIsNightMode;
    private WindowManager.LayoutParams mLayoutParams;
    private WidgetPagerView mPagerView;
    private int mRotation;
    private IWindowStateListener mScreenStateListener;
    
        public interface IWindowStateListener {
            void onScreenStateChanged(int screenState);
    
            void onUiModeChanged();
        }
    
        @Override // android.widget.FrameLayout, android.view.ViewGroup
        protected /* bridge */ /* synthetic */ ViewGroup.LayoutParams generateDefaultLayoutParams() {
            return super.generateDefaultLayoutParams();
        }
    
        @Override // android.widget.FrameLayout, android.view.ViewGroup
        public /* bridge */ /* synthetic */ ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
            return super.generateLayoutParams(attrs);
        }
    
        @Override // android.view.ViewGroup, android.view.View
        public /* bridge */ /* synthetic */ ViewOverlay getOverlay() {
            return super.getOverlay();
        }
    
        static {
            EXTRA_FLAG_SHOW_WITH_KEYGUARD_GOING_AWAY = Utilities.atLeastAndroidV() ? 131072 : 65536;
        }
    
        public WatchOverlayGroupView(Context context) {
            this(context, null);
        }
    
        public WatchOverlayGroupView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }
    
        public WatchOverlayGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }
    
        public WatchOverlayGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            this.TYPE_LAUNCHER_OVERLAY_WINDOW = PerformLaunchAction.CALL_PHONE_REQUEST_CODE;
            this.mRotation = -1;
            init();
        }
    
        private void init() {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            this.mLayoutParams = layoutParams;
            layoutParams.width = getContext().getResources().getDimensionPixelSize(R.dimen.watch_width);
            this.mLayoutParams.height = getContext().getResources().getDimensionPixelSize(R.dimen.watch_height);
            this.mLayoutParams.type = PerformLaunchAction.CALL_PHONE_REQUEST_CODE;
            this.mLayoutParams.flags = 8519688;
            ReflectUtils.setValue(this.mLayoutParams, "extraFlags", Integer.TYPE, Integer.valueOf(EXTRA_FLAG_SHOW_WITH_KEYGUARD_GOING_AWAY));
            addPrivateFlags();
            this.mLayoutParams.format = -3;
            this.mLayoutParams.softInputMode = 48;
            this.mLayoutParams.layoutInDisplayCutoutMode = 1;
            this.mLayoutParams.setTitle("WatchOverlayGroupView");
            setSystemUiVisibility(512);
            boolean zIsCutoutLeftForRotation0 = DeviceConfig.INSTANCE.isCutoutLeftForRotation0();
            WindowManager.LayoutParams layoutParams2 = new WindowManager.LayoutParams();
            layoutParams2.copyFrom(this.mLayoutParams);
            layoutParams2.gravity = zIsCutoutLeftForRotation0 ? 8388691 : 8388661;
            WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams();
            layoutParams3.copyFrom(this.mLayoutParams);
            layoutParams3.gravity = zIsCutoutLeftForRotation0 ? 8388661 : 8388691;
            WindowManager.LayoutParams[] layoutParamsArr = {layoutParams2, layoutParams2, layoutParams3, layoutParams3};
            try {
                Field declaredField = WindowManager.LayoutParams.class.getDeclaredField("paramsForRotation");
                declaredField.setAccessible(true);
                declaredField.set(this.mLayoutParams, layoutParamsArr);
            } catch (Exception e) {
                FLLog.i(TAG, "setValue", e);
            }
        }
    
        @Override // android.view.View
        protected void onFinishInflate() {
            super.onFinishInflate();
            this.mPagerView = (WidgetPagerView) findViewById(R.id.pager);
        }
    
        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (this.mPagerView.getAlpha() != 1.0f) {
                FlipLauncher launcher = FlipApplication.getLauncher();
                if (launcher == null) {
                    return true;
                }
                ev.setLocation(ev.getRawX() - ev.getX(), 0.0f);
                return launcher.dispatchTouchEvent(ev);
            }
            return super.dispatchTouchEvent(ev);
        }
    
        private void addPrivateFlags() {
            try {
                Field declaredField = WindowManager.LayoutParams.class.getDeclaredField("privateFlags");
                declaredField.setAccessible(true);
                declaredField.set(this.mLayoutParams, Integer.valueOf(declaredField.getInt(this.mLayoutParams) | 64));
            } catch (Exception e) {
                FLLog.e(TAG, "addPrivateFlags", e);
            }
        }
    
        @Override // android.view.View
        public WindowManager.LayoutParams getLayoutParams() {
            return this.mLayoutParams;
        }
    
        public void updateLayoutByOrientation(int rotation) {
            if (this.mRotation == rotation) {
                return;
            }
            this.mRotation = rotation;
            FLLog.v(TAG, "mRotation " + this.mRotation);
            setVisibility(isHide() ? 8 : 0);
        }
    
        @Override // android.view.View
        public void setVisibility(int visibility) {
            if (isHide() && visibility == 0) {
                return;
            }
            super.setVisibility(visibility);
        }
    
        private boolean isHide() {
            int i = this.mRotation;
            return (i == 0 || i == 2) ? false : true;
        }
    
        @Override // android.view.ViewGroup, android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            this.mIsNightMode = (getResources().getConfiguration().uiMode & 48) == 32;
        }
    
        @Override // android.view.View
        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            boolean z = (newConfig.uiMode & 48) == 32;
            if (this.mIsNightMode == z) {
                return;
            }
            this.mIsNightMode = z;
            IWindowStateListener iWindowStateListener = this.mScreenStateListener;
            if (iWindowStateListener != null) {
                iWindowStateListener.onUiModeChanged();
            }
        }
    
        @Override // android.view.View
        public void onScreenStateChanged(int screenState) {
            super.onScreenStateChanged(screenState);
            IWindowStateListener iWindowStateListener = this.mScreenStateListener;
            if (iWindowStateListener != null) {
                iWindowStateListener.onScreenStateChanged(screenState);
            }
        }
    
        public void setScreenStateListener(IWindowStateListener listener) {
            this.mScreenStateListener = listener;
        }
    }
    
        public WatchOverlayGroupView(Context context) {
            this(context, null);
        }
    
        public WatchOverlayGroupView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }
    
        public WatchOverlayGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }
    
        public WatchOverlayGroupView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            this.TYPE_LAUNCHER_OVERLAY_WINDOW = PerformLaunchAction.CALL_PHONE_REQUEST_CODE;
            this.mRotation = -1;
            init();
        }
    
    package com.miui.fliphome.widget;
    public class WatchOverlayWindow implements LifecycleOwner {
    private static final int ACTION_ADD_WINDOW = 1;
    private static final int ACTION_REMOVE_WINDOW = 2;
    public static final int GESTURE_END_HOME = 3;
    public static final int GESTURE_START = 2;
    public static final int GESTURE_UNDEFINED = 1;
    private static final Set<String> PKG_DISMISS_WATCH;
    private static final String TAG = "WatchOverlayWindow";

    private void showHideWindow(boolean show, boolean withAnimation) {
        if (!withAnimation) {
            if (show) {
                Folme.useAt(this.mWatchOverlayGroupView).state().setTo(this.mShowAnimState);
                WindowManager windowManager = this.mWindowManager;
                WatchOverlayGroupView watchOverlayGroupView = this.mWatchOverlayGroupView;
                windowManager.addView(watchOverlayGroupView, watchOverlayGroupView.getLayoutParams());
                return;
            }
            this.mWindowManager.removeViewImmediate(this.mWatchOverlayGroupView);
            return;
        }
        this.mShowHideAnimConfig.removeListeners(new TransitionListener[0]);
        this.mShowHideAnimConfig.setSpecial(ViewProperty.ALPHA, show ? this.alphaShowEase : this.alphaHideEase);
        if (show) {
            WindowManager windowManager2 = this.mWindowManager;
            WatchOverlayGroupView watchOverlayGroupView2 = this.mWatchOverlayGroupView;
            windowManager2.addView(watchOverlayGroupView2, watchOverlayGroupView2.getLayoutParams());
            Folme.useAt(this.mWatchOverlayGroupView).state().fromTo(this.mHideAnimState, this.mShowAnimState, this.mShowHideAnimConfig);
            return;
        }
        Folme.useAt(this.mWatchOverlayGroupView).state().fromTo(this.mShowAnimState, this.mHideAnimState, this.mShowHideAnimConfig.addListeners(new TransitionListener() { // from class: com.miui.fliphome.widget.WatchOverlayWindow.5
            @Override // miuix.animation.listener.TransitionListener
            public void onComplete(Object toTag) {
                if (WatchOverlayWindow.this.mInHideAnimation) {
                    if (WatchOverlayWindow.this.mWatchOverlayGroupView.getParent() != null) {
                        WatchOverlayWindow.this.mWindowManager.removeViewImmediate(WatchOverlayWindow.this.mWatchOverlayGroupView);
                    }
                    WatchOverlayWindow.this.mInHideAnimation = false;
                    return;
                }
                Folme.useAt(WatchOverlayWindow.this.mWatchOverlayGroupView).state().setTo(WatchOverlayWindow.this.mShowAnimState);
            }



    - framework-res.apk
    - framework-ext-res.apk 
        <string name="ime_rotation_tip">当前角度不支持输入，请旋转后使用</string>
        public static final int ime_rotation_tip = 0x110f0453; 十进制：286196819

    - miuisystemui.apk
            public final boolean shouldHideWindow(ComponentName componentName, String str) {
                boolean zContains = this.mPkgDismissWatchSet.contains(str);
                ClockBaseAnimation$$ExternalSyntheticOutline0.m("is in white lists: ", "DecorWindow", zContains);
                if (!zContains) {
                    try {
                        zContains = shouldHideDecorWindow(componentName);
                    } catch (Exception e) {
                        Log.i("DecorWindow", "isHideDecorWindow, exp: " + e.getMessage());
                    }
                    ClockBaseAnimation$$ExternalSyntheticOutline0.m("3rd should show state: ", "DecorWindow", zContains);
                }
                return zContains;
            }
            -   systemui里的控制小组件的：
                属性名miui.supportFlipWatchOverlayGroupView属性值默认不配置则显示小部件（默认值为true）
                如果配置为false则隐藏小部件配置粒度支持Application + Activity。Activity优先
                package com.android.notification.decor;
                public final class DecorWindowManagerImpl
                public final boolean shouldHideDecorWindow(ComponentName componentName) {
                    String packageName = componentName.getPackageName();
                    Object objCallObjectMethod = ReflectUtils.callObjectMethod(this.mPackageManager, "getProperty", new Class[]{String.class, ComponentName.class}, "miui.supportFlipWatchOverlayGroupView", componentName);
                    if (objCallObjectMethod == null) {
                        objCallObjectMethod = ReflectUtils.callObjectMethod(this.mPackageManager, "getProperty", new Class[]{String.class, String.class}, "miui.supportFlipWatchOverlayGroupView", packageName);
                    }
                    if (objCallObjectMethod != null) {
                        if (ReflectUtils.callObjectMethod(objCallObjectMethod, "getBoolean", new Class[0], new Object[0]) instanceof Boolean) {
                            return !((Boolean) r9).booleanValue();
                        }
                    }
                    return false;
                }
            - 关键提示
        public static final int miui_notification_menu_title_no_drawable = 0x7f140996; 十进制：2132019606
        内容为Continue on the inner screen/展开到内屏继续操作。但没找到在哪里使用
    在framework-ext-res.apk发现
    <string name="start_activity_tip">Continue on the inner screen</string>

    <public type="string" name="start_activity_tip" id="0x110f0378" />286196600
    <public type="id" name="start_activity_tip_view" id="0x110a0103" />285868291
    <public type="id" name="start_activity_tip" id="0x110a00c8" />285868232


    package com.miui.utils.configs;
    
    import android.content.Context;
    import android.content.res.Configuration;
    import android.graphics.Point;
    import android.graphics.Rect;
    import android.graphics.Typeface;
    import android.os.Build;
    import android.os.SystemProperties;
    import android.text.TextUtils;
    import android.util.Log;
    import android.view.Display;
    import android.view.DisplayCutout;
    import android.view.DisplayInfo;
    import android.widget.TextView;
    import androidx.constraintlayout.motion.widget.MotionLayout$$ExternalSyntheticOutline0;
    import java.util.List;
    import java.util.Objects;
    import miui.util.DeviceLevel;
    import miui.util.FeatureParser;
    import miui.util.MiuiMultiDisplayTypeInfo;
    import miuix.device.DeviceUtils;
    
    /* JADX INFO: compiled from: go/retraceme 557dde13670c5a7d4cc537f64b2f35dfe280e4adc955f1b9c6e99761ac543733 */
    /* JADX INFO: loaded from: classes3.dex */
    public abstract class MiuiConfigs {
        public static final boolean BACKGROUND_BLUR_SUPPORTED;
        public static final boolean BROAD_SIDE_FP;
        public static final boolean CN_CUSTOMIZATION_TEST;
        public static final String CUSTOMIZED_REGION;
        public static final boolean FOD_SWIPE_SENSOR;
        public static final String FONT_MIPRO_PATH;
        public static final Typeface FONT_WGHT_660;
        public static final float FULL_AOD_FIXED_BRIGHTNESS;
        public static final boolean GXZW_SENSOR;
        public static final boolean HAS_CAMERA_IN_LARGESCREEN;
        public static final boolean IS_CM_CUSTOMIZATION;
        public static final boolean IS_CM_CUSTOMIZATION_TEST;
        public static final boolean IS_CT_CUSTOMIZATION_TEST;
        public static final boolean IS_CUST_SINGLE_SIM;
        public static final boolean IS_DEVELOPMENT_VERSION;
        public static final boolean IS_FOLD;
        public static final boolean IS_INDEPENDENT_REAR_DEVICE;
        public static final boolean IS_INTERNATIONAL_BUILD;
        public static final boolean IS_JP_RK_VERSION;
        public static final boolean IS_KDDI_VERSION;
        public static final boolean IS_MEDIATEK;
        public static final boolean IS_NOTCH;
        public static final boolean IS_OLED_SCREEN;
        public static final boolean IS_PAD;
        public static final boolean IS_REDMI_BRAND;
        public static boolean IS_RSA4_FROM_WC;
        public static final boolean IS_SUPPORT_REAR_SMART_ASSISTANT;
        public static final boolean IS_XRING;
        public static final boolean MIUI_LITE_V2;
        public static final boolean MI_SHADOW_SUPPORTED;
        public static final boolean NOT_SUPPORT_FASHION_GALLERY_CN;
        public static final boolean SHADOW_SUPPORTED;
        public static final boolean SUPPORT_FULL_AOD;
        public static final boolean SUPPORT_GESTURE_WAKEUP;
        public static final boolean SUPPORT_LEAUDIO_CG;
        public static final boolean SUPPORT_MULTIPLE_FACES_AON;
        public static final boolean SUPPORT_NON_UI;
        public static final boolean SUPPORT_OWNER_FACE_AON;
        public static final boolean SUPPORT_PROP_DYNAMIC_ROUND_CORNER;
        public static final int cpuLevel;
        public static final int gpuLevel;
        public static final int sDeviceLevelFromFolme;
        public static int sForceMiddleDevice;
        public static final Configuration sInstantAppConfig;
        public static final Typeface sMiproTypeface;
        public static final Typeface sMiproTypefaceWght460;
        public static final Typeface sMiproTypefaceWght500;
        public static final Typeface sMiproTypefaceWght600;
        public static int sScreenHeight;
        public static int sScreenWidth;

    static {
        String str = Build.VERSION.INCREMENTAL;
        boolean z = !TextUtils.isEmpty(str) && str.matches("\\d+(.\\d+){2,}(-internal)?");
        boolean z2 = !TextUtils.isEmpty(str) && str.matches("^(V\\d{1,})(\\.\\d{1,})*(\\.DEV)$");
        String str2 = SystemProperties.get("ro.miui.customized.region", "");
        CUSTOMIZED_REGION = str2;
        IS_INTERNATIONAL_BUILD = miui.os.Build.IS_INTERNATIONAL_BUILD;
        IS_DEVELOPMENT_VERSION = z || z2;
        String str3 = SystemProperties.get("ro.cust.test", "");
        String str4 = SystemProperties.get("ro.carrier.name", "");
        boolean zEquals = "cm".equals(str3);
        IS_CM_CUSTOMIZATION_TEST = zEquals;
        boolean zEquals2 = "ct".equals(str3);
        IS_CT_CUSTOMIZATION_TEST = zEquals2;
        boolean zEquals3 = "cu".equals(str3);
        IS_CM_CUSTOMIZATION = "cm".equals(str4);
        "ct".equals(str4);
        "cu".equals(str4);
        CN_CUSTOMIZATION_TEST = zEquals || zEquals2 || zEquals3;
        SystemProperties.getInt("ro.debuggable", 0);
        NOT_SUPPORT_FASHION_GALLERY_CN = FeatureParser.getBoolean("not_support_fashion_gallery", false);
        SUPPORT_OWNER_FACE_AON = FeatureParser.getBoolean("support_owner_faces_aon", false);
        SUPPORT_MULTIPLE_FACES_AON = FeatureParser.getBoolean("support_multiple_faces_aon", false);
        IS_FOLD = SystemProperties.getInt("persist.sys.muiltdisplay_type", 0) == 2;
        IS_PAD = SystemProperties.get("ro.build.characteristics").contains("tablet");
        IS_CUST_SINGLE_SIM = SystemProperties.getInt("ro.miui.singlesim", 0) == 1;
        IS_NOTCH = SystemProperties.getInt("ro.miui.notch", 0) == 1;
        IS_MEDIATEK = FeatureParser.getBoolean("is_mediatek", false);
        IS_XRING = TextUtils.equals("xring", FeatureParser.getString("vendor"));
        SUPPORT_LEAUDIO_CG = SystemProperties.get("persist.vendor.bluetooth.leaudio_mode", "").equalsIgnoreCase("ums-cg");
        IS_RSA4_FROM_WC = !Objects.equals(SystemProperties.get("ro.com.miui.rsa.feature"), "");
        GXZW_SENSOR = SystemProperties.getBoolean("ro.hardware.fp.fod", false);
        FOD_SWIPE_SENSOR = SystemProperties.getBoolean("persist.vendor.sys.fp.fod.slide", false);
        SystemProperties.getBoolean("ro.vendor.localhbm.enable", false);
        BROAD_SIDE_FP = SystemProperties.getBoolean("ro.hardware.fp.sideCap", false);
        SUPPORT_NON_UI = SystemProperties.getBoolean("sys.power.nonui", false);
        SystemProperties.get("ro.boot.hwc", "");
        SystemProperties.getBoolean("ro.vendor.miui.support_esim", false);
        SUPPORT_GESTURE_WAKEUP = FeatureParser.getBoolean("support_gesture_wakeup", false);
        "cn".equals(SystemProperties.get("ro.miui.cust_variant"));
        HAS_CAMERA_IN_LARGESCREEN = SystemProperties.getInt("persist.sys.frontcamera_type", 0) == 2;
        String str5 = SystemProperties.get("ro.miui.ui.font.mi_font_path", "/system/fonts/MiSansVF.ttf");
        FONT_MIPRO_PATH = str5;
        sMiproTypeface = new Typeface.Builder(str5).setFontVariationSettings("'wght' 430").build();
        sMiproTypefaceWght460 = new Typeface.Builder(str5).setFontVariationSettings("'wght' 460").build();
        sMiproTypefaceWght500 = new Typeface.Builder(str5).setFontVariationSettings("'wght' 500").build();
        sMiproTypefaceWght600 = new Typeface.Builder(str5).setFontVariationSettings("'wght' 600").build();
        FONT_WGHT_660 = new Typeface.Builder(str5).setFontVariationSettings("'wght' 660").build();
        SUPPORT_FULL_AOD = FeatureParser.getBoolean("support_aod_fullscreen", false);
        FULL_AOD_FIXED_BRIGHTNESS = Float.valueOf(SystemProperties.get("ro.miui.special.aod.mask", "-1f")).floatValue();
        IS_REDMI_BRAND = SystemProperties.get("ro.product.brand").contains("Redmi");
        SystemProperties.get("ro.product.brand").contains("Xiaomi");
        SUPPORT_PROP_DYNAMIC_ROUND_CORNER = SystemProperties.getBoolean("sys.display.rounded_corner_type", false);
        IS_OLED_SCREEN = "oled".equals(SystemProperties.get("ro.vendor.display.type")) || "oled".equals(SystemProperties.get("ro.display.type"));
        sInstantAppConfig = new Configuration();
        IS_INDEPENDENT_REAR_DEVICE = callBooleanMethodQuietly("miui.util.MiuiMultiDisplayTypeInfo", "isIndependentRearDevice", false);
        IS_SUPPORT_REAR_SMART_ASSISTANT = callBooleanMethodQuietly("miui.os.DeviceFeature", "isSupportRearSmartAssistant", true);
        int i = DeviceLevel.TOTAL_RAM;
        MIUI_LITE_V2 = DeviceLevel.getMiuiLiteVersion() == 2;
        BACKGROUND_BLUR_SUPPORTED = SystemProperties.getBoolean("persist.sys.background_blur_supported", false);
        SHADOW_SUPPORTED = SystemProperties.getBoolean("persist.sys.mi_shadow_supported", false);
        MI_SHADOW_SUPPORTED = SystemProperties.getBoolean("persist.sys.support_view_mishadow", true);
        IS_KDDI_VERSION = "jp_kd".equals(str2);
        IS_JP_RK_VERSION = "jp_rk".equals(str2);
        cpuLevel = DeviceLevel.getDeviceLevel(1, DeviceLevel.CPU);
        gpuLevel = DeviceLevel.getDeviceLevel(1, DeviceLevel.GPU);
        sForceMiddleDevice = -1;
        sScreenHeight = 0;
        sScreenWidth = 0;
        int i2 = DeviceUtils.DEV_STANDARD_VERSION;
        sDeviceLevelFromFolme = Math.min(DeviceUtils.getDeviceLevel(i2, DeviceUtils.TYPE_CPU), DeviceUtils.getDeviceLevel(i2, DeviceUtils.TYPE_GPU));
    }

    public static void applyStatusBarTypeface(Typeface typeface, TextView... textViewArr) {
        String[] strArr;
        Typeface typefaceCreate = Typeface.create("mipro-medium", 0);
        if (typefaceCreate == null || (strArr = typefaceCreate.familyName) == null) {
            typeface = typefaceCreate;
        } else {
            for (String str : strArr) {
                if (str.startsWith("mipro")) {
                    break;
                }
            }
            typeface = typefaceCreate;
        }
        for (TextView textView : textViewArr) {
            if (textView != null) {
                textView.setTypeface(typeface);
            }
        }
    }

    public static boolean callBooleanMethodQuietly(String str, String str2, boolean z) {
        try {
            Class[] clsArr = new Class[0];
            Object objInvoke = Class.forName(str).getMethod(str2, null).invoke(null, null);
            return objInvoke instanceof Boolean ? ((Boolean) objInvoke).booleanValue() : z;
        } catch (Exception unused) {
            Log.d("Configs", MotionLayout$$ExternalSyntheticOutline0.m("callBooleanMethodQuietly: ", str, "#", str2, " -> not found"));
            return z;
        }
    }

    public static int getAdjustedRotation(Context context) {
        int rotation = context.getDisplay().getRotation();
        return isFlipTinyScreen(context) ? (rotation + 2) % 4 : rotation;
    }

    public static Rect getCutoutRect(Context context) {
        DisplayCutout cutout = context.getDisplay().getCutout();
        List<Rect> boundingRects = cutout != null ? cutout.getBoundingRects() : null;
        if (boundingRects != null) {
            for (int i = 0; i < boundingRects.size(); i++) {
                Rect rect = boundingRects.get(i);
                if (rect != null && !rect.isEmpty()) {
                    return rect;
                }
            }
        }
        return null;
    }

    public static Point getLockScreenSize(Context context) {
        Point screenSize = getScreenSize(context);
        int iMin = screenSize.x;
        int iMax = screenSize.y;
        if (!isFlipTinyScreen(context) && !isFoldLargeScreenAndNotPad(context) && !IS_PAD) {
            iMin = Math.min(screenSize.x, screenSize.y);
            iMax = Math.max(screenSize.x, screenSize.y);
        }
        return new Point(iMin, iMax);
    }

    public static Rect getSafeInsets(Context context) {
        DisplayInfo displayInfo = new DisplayInfo();
        context.getDisplay().getDisplayInfo(displayInfo);
        DisplayCutout displayCutout = displayInfo.displayCutout;
        if (displayCutout != null) {
            return displayCutout.getSafeInsets();
        }
        return null;
    }

    public static int getScreenHeight(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getConfiguration().windowConfiguration.getMaxBounds().height();
    }

    public static Point getScreenSize(Context context) {
        Rect maxBounds = context.getResources().getConfiguration().windowConfiguration.getMaxBounds();
        return new Point(maxBounds.width(), maxBounds.height());
    }

    public static int getScreenWidth(Context context) {
        if (context == null) {
            return 0;
        }
        return context.getResources().getConfiguration().windowConfiguration.getMaxBounds().width();
    }

    public static boolean isCupAndGupHighLevel() {
        int i = DeviceLevel.HIGH;
        return cpuLevel == i && gpuLevel == i;
    }

    public static boolean isCutoutRight(Context context) {
        DisplayCutout cutout;
        Display display = context.getDisplay();
        return (display == null || (cutout = display.getCutout()) == null || cutout.getSafeInsetRight() <= 0) ? false : true;
    }

    public static boolean isFlipTinyScreen(Context context) {
        return MiuiMultiDisplayTypeInfo.isFlipDevice() && isTinyScreen(context);
    }

    public static boolean isFoldLargeScreenAndNotPad(Context context) {
        return !IS_PAD && IS_FOLD && context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isFoldableDevice() {
        return IS_FOLD || MiuiMultiDisplayTypeInfo.isFlipDevice();
    }

    public static boolean isInstantFlipTinyScreen() {
        return MiuiMultiDisplayTypeInfo.isFlipDevice() && sInstantAppConfig.screenType == 1;
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    public static boolean isLite2Phone() {
        return MIUI_LITE_V2 && !IS_PAD;
    }

    public static boolean isLiteOrLowDevice() {
        int i;
        return isLowEndDevice() || cpuLevel == (i = DeviceLevel.LOW) || gpuLevel == i;
    }

    public static boolean isLowEndDevice() {
        return DeviceLevel.IS_MIUI_LITE_VERSION || DeviceLevel.IS_MIUI_MIDDLE_VERSION || miui.os.Build.IS_MIUI_LITE_VERSION || sForceMiddleDevice == 1;
    }

    public static boolean isNightMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & 48) == 32;
    }

    public static boolean isPadLandscape(Context context) {
        return IS_PAD && context.getResources().getConfiguration().orientation == 2;
    }

    public static boolean isPadOrFoldLargeScreen(Context context) {
        if (IS_PAD) {
            return true;
        }
        return IS_FOLD && context.getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isTinyScreen(Context context) {
        Point screenSize = getScreenSize(context);
        return ((int) (((float) Math.max(screenSize.x, screenSize.y)) / context.getResources().getDisplayMetrics().density)) <= 670;
    }

    public static boolean isTinyScreenLandscape(Context context) {
        return isFlipTinyScreen(context) && context.getResources().getConfiguration().orientation == 2;
    }

    public static boolean isVerticalMode(Context context) {
        return isPadOrFoldLargeScreen(context) || context.getResources().getConfiguration().orientation == 1;
    }

    public static void setMiuiStatusBarWifiTypeface(TextView... textViewArr) {
        String[] strArr;
        Typeface typefaceCreate = Typeface.create("mipro-bold", 0);
        if (typefaceCreate != null && (strArr = typefaceCreate.familyName) != null) {
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                if (strArr[i].startsWith("mipro")) {
                    typefaceCreate = FONT_WGHT_660;
                    break;
                }
                i++;
            }
        }
        for (TextView textView : textViewArr) {
            if (textView != null) {
                textView.setTypeface(typefaceCreate);
            }
        }
    }
}
    - framework.jar
	        public static final int config_secondaryBuiltInDisplayCutoutSideOverride = 0x010700ce; 17236174
        public static final int config_fillSecondaryBuiltInDisplayCutout = 0x011101ae; 17891758
        public static final int config_maskSecondaryBuiltInDisplayCutout = 0x011101f4;  17891828
        public static final int config_secondaryBuiltInDisplayCutout = 0x0104032f;  17040175
        public static final int config_secondaryBuiltInDisplayCutoutRectApproximation = 0x01040330;  17040176
    - miui-framework.jar

package android.appcompat;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.miui.R;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/* JADX INFO: loaded from: classes.dex */
public class FlipTipView extends LinearLayout {
private static final int LETTER_BOX_OUTER_HALF_REGION_WIDTH = 199;
private static final int LETTER_BOX_OUTER_REGION_WIDTH = 398;
private static final String TAG = "FlipTipView";
private final Animatable2.AnimationCallback mAnimationCallback;
private final int mAnimationHeight;
private final ImageView mAnimationView;
private final LinearLayout.LayoutParams mAnimationViewLayoutParams;
private final Context mContext;
private final int mInitDensity;
private final int mInitWidth;
private final LayoutInflater mLayoutInflater;
private final AnimatedVectorDrawable mSendingDrawable;
private final float mTextSize;
private final TextView mTextView;
private final LinearLayout mTipLayout;
private final View mTipView;
private final LinearLayout.LayoutParams mTipViewLayoutParams;
private final IWindowManager mWindowManager;
private final WindowManager mWm;

    public FlipTipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mWm = (WindowManager) this.mContext.getSystemService("window");
        this.mWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mTipView = this.mLayoutInflater.inflate(R.layout.continuity_intercept_dialog_layout, this);
        this.mTipLayout = (LinearLayout) this.mTipView.findViewById(R.id.continuity_dialog);
        this.mTextView = (TextView) this.mTipView.findViewById(R.id.start_activity_tip);
        this.mTextView.setInputType(524288);
        this.mAnimationView = (ImageView) this.mTipView.findViewById(R.id.openanimation);
        this.mTipViewLayoutParams = (LinearLayout.LayoutParams) this.mTipLayout.getLayoutParams();
        this.mAnimationViewLayoutParams = (LinearLayout.LayoutParams) this.mAnimationView.getLayoutParams();
        this.mInitDensity = getForcedDensity(this.mContext.getUserId());
        this.mInitWidth = this.mTipViewLayoutParams.width;
        this.mAnimationHeight = this.mAnimationViewLayoutParams.height;
        this.mTextSize = this.mTextView.getTextSize();
        this.mSendingDrawable = (AnimatedVectorDrawable) this.mContext.getDrawable(R.drawable.openanimation);
        this.mAnimationCallback = new Animatable2.AnimationCallback() { // from class: android.appcompat.FlipTipView.1
            @Override // android.graphics.drawable.Animatable2.AnimationCallback
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                FlipTipView.this.mSendingDrawable.setVisible(true, true);
                try {
                    FlipTipView.this.mSendingDrawable.forceAnimationOnUI();
                } catch (Exception e) {
                    Slog.e(FlipTipView.TAG, "forceAnimationOnUI catch some exception:", e);
                }
                FlipTipView.this.mSendingDrawable.start();
            }
        };
        this.mTipLayout.getBackground().setAlpha(255);
        this.mAnimationView.setImageDrawable(this.mSendingDrawable);
        updateLocaleText();
        try {
            this.mSendingDrawable.forceAnimationOnUI();
        } catch (Exception e) {
            Slog.e(TAG, "forceAnimationOnUI catch some exception:", e);
        }
        this.mTipView.setSystemUiVisibility(4870);
    }

    public void show() {
        fixRectWithConfiguration();
        if (this.mTipView != null) {
            this.mTipView.setVisibility(0);
        }
        if (this.mSendingDrawable != null) {
            this.mSendingDrawable.clearAnimationCallbacks();
            this.mSendingDrawable.registerAnimationCallback(this.mAnimationCallback);
            if (this.mSendingDrawable.isRunning()) {
                this.mSendingDrawable.stop();
            } else {
                try {
                    this.mSendingDrawable.forceAnimationOnUI();
                } catch (Exception e) {
                    Slog.e(TAG, "forceAnimationOnUI catch some exception:", e);
                }
            }
            this.mSendingDrawable.start();
        }
    }

    public void hide() {
        if (this.mSendingDrawable != null) {
            this.mSendingDrawable.stop();
            this.mSendingDrawable.setVisible(false, true);
            this.mSendingDrawable.unregisterAnimationCallback(this.mAnimationCallback);
        }
        if (this.mTipView != null) {
            this.mTipView.setVisibility(8);
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLocaleText();
    }

    private void fixRectWithConfiguration() {
        float scale = getBaseDensity() / this.mInitDensity;
        this.mTipViewLayoutParams.width = (int) (this.mInitWidth * scale);
        this.mTipLayout.setLayoutParams(this.mTipViewLayoutParams);
        this.mAnimationViewLayoutParams.height = (int) (this.mAnimationHeight * scale);
        this.mAnimationView.setLayoutParams(this.mAnimationViewLayoutParams);
        this.mTextView.setTextSize(0, this.mTextSize * scale);
    }

    private int getForcedDensity(int userId) {
        String densityString = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "display_density_forced", userId);
        if (TextUtils.isEmpty(densityString)) {
            return getBaseDensity();
        }
        return Integer.valueOf(densityString).intValue();
    }

    private int getBaseDensity() {
        try {
            return this.mWindowManager.getBaseDisplayDensity(0);
        } catch (RemoteException e) {
            e.printStackTrace();
            return this.mContext.getResources().getConfiguration().densityDpi;
        }
    }

    private void updateLocaleText() {
        if (this.mTipView != null) {
            this.mTextView.setText(R.string.start_activity_tip);
            this.mTextView.setMaxLines(4);
            if (this.mTextView.getLineCount() > 3) {
                this.mTextView.setEllipsize(TextUtils.TruncateAt.END);
            }
        }
    }
}
package miui.util;
/* JADX INFO: loaded from: classes.dex */
public final class MiuiMultiDisplayTypeInfo {
    public static final int DEVICE_FLIP_TYPE = 4;
    public static final int DEVICE_FOLD_INSIDE_TYPE = 3;
    public static final int DEVICE_FOLD_OUTSIDE_TYPE = 5;
    public static final int DEVICE_INDEPENDENT_REAR_PHONE_TYPE = 6;
    public static final int DEVICE_PHONE_TYPE = 1;
    public static final int DEVICE_REAR_PHONE_TYPE = 2;
    private static final boolean IS_TABLET;
    private static final int sDeviceStateInfo;
    private static final int sDeviceType;

    /* JADX WARN: Removed duplicated region for block: B:6:0x001b  */
    static {
        /*
            java.lang.String r0 = "ro.build.characteristics"
            java.lang.String r1 = ""
            java.lang.String r0 = android.os.SystemProperties.get(r0, r1)
            java.lang.String r1 = "tablet"
            boolean r0 = r1.equals(r0)
            r1 = 1
            if (r0 != 0) goto L1b
            java.lang.String r0 = "ro.config.tablet"
            r2 = 0
            boolean r0 = android.os.SystemProperties.getBoolean(r0, r2)
            if (r0 == 0) goto L1c
        L1b:
            r2 = r1
        L1c:
            miui.util.MiuiMultiDisplayTypeInfo.IS_TABLET = r2
            java.lang.String r0 = "persist.sys.multi_display_type"
            int r0 = android.os.SystemProperties.getInt(r0, r1)
            miui.util.MiuiMultiDisplayTypeInfo.sDeviceStateInfo = r0
            int r0 = miui.util.MiuiMultiDisplayTypeInfo.sDeviceStateInfo
            r0 = r0 & 255(0xff, float:3.57E-43)
            miui.util.MiuiMultiDisplayTypeInfo.sDeviceType = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: miui.util.MiuiMultiDisplayTypeInfo.<clinit>():void");
    }

    public static int getDeviceType() {
        return sDeviceType;
    }

    public static boolean isFoldDeviceInside() {
        return sDeviceType == 3;
    }

    public static boolean isFoldDeviceOutside() {
        return sDeviceType == 5;
    }

    public static boolean isFoldDevice() {
        return sDeviceType == 3 || sDeviceType == 5;
    }

    public static boolean isRearDevice() {
        return sDeviceType == 2;
    }

    public static boolean isFlipDevice() {
        return sDeviceType == 4;
    }

    public static boolean isIndependentRearDevice() {
        return sDeviceType == 6;
    }

    public static boolean isTableDevice() {
        return IS_TABLET;
    }
}


    - services.jar:

boolean isLetterboxedForDisplayCutout() {
        if (this.mActivityRecord == null || !this.mWindowFrames.parentFrameWasClippedByDisplayCutout() || this.mAttrs.layoutInDisplayCutoutMode == 3 || this.mWindowStateStub.isMiuiLayoutInCutoutAlways(this.mAttrs) || !this.mAttrs.isFullscreen()) {
            return false;
        }
        return !frameCoversEntireAppTokenBounds();
    }

    AccessibilityUserState(int userId, Context context, ServiceInfoChangeListener serviceInfoChangeListener) {
        boolean z = false;
        this.mUserId = userId;
        this.mContext = context;
        this.mServiceInfoChangeListener = serviceInfoChangeListener;
        this.mFocusStrokeWidthDefaultValue = this.mContext.getResources().getDimensionPixelSize(R.dimen.accessibility_touch_slop);
        this.mFocusColorDefaultValue = this.mContext.getResources().getColor(R.color.accessibility_focus_highlight_color);
        this.mFocusStrokeWidth = this.mFocusStrokeWidthDefaultValue;
        this.mFocusColor = this.mFocusColorDefaultValue;
        if (this.mContext.getResources().getBoolean(R.bool.config_maskSecondaryBuiltInDisplayCutout) && this.mContext.getPackageManager().hasSystemFeature("android.software.window_magnification")) {
            z = true;
        }
        this.mSupportWindowMagnification = z;
        this.mShortcutTargets.put(2, new ArraySet<>());
        this.mShortcutTargets.put(1, new ArraySet<>());
        this.mShortcutTargets.put(32, new ArraySet<>());
        this.mShortcutTargets.put(16, new ArraySet<>());
        this.mShortcutTargets.put(64, new ArraySet<>());
    }
boolean isLetterboxedForDisplayCutout() {
        if (this.mActivityRecord == null || !this.mWindowFrames.parentFrameWasClippedByDisplayCutout() || this.mAttrs.layoutInDisplayCutoutMode == 3 || this.mWindowStateStub.isMiuiLayoutInCutoutAlways(this.mAttrs) || !this.mAttrs.isFullscreen()) {
            return false;
        }
        return !frameCoversEntireAppTokenBounds();
    }
    - miui-services.jar
	package com.miui.server.input.gesture.multifingergesture;

/* JADX INFO: loaded from: classes.dex */
public class MiuiSubScreenMultiFingerGestureManager implements MiuiGestureListener {
    private static final int NEED_DISPLAY_ID = 1;
    private static final String TAG = "MiuiSubScreenMultiFingerGestureManager";
    private static volatile MiuiSubScreenMultiFingerGestureManager sInstance = null;
    private WindowManagerPolicy.WindowState mFocusedWindow = null;
    private final List<MiuiSubScreenMultiFingerGestureListener> mGestureListeners = new ArrayList();
    private final MiuiGestureMonitor mMiuiGestureMonitor;

    private MiuiSubScreenMultiFingerGestureManager(Context context) {
        this.mGestureListeners.add(new MiuiSubscreenDoubleTapGesture(context));
        this.mGestureListeners.add(new MiuiSubscreenThreeFingerDownGesture(context));
        this.mMiuiGestureMonitor = MiuiGestureMonitor.getInstance(context);
        this.mMiuiGestureMonitor.registerPointerEventListener(this, 1);
    }

    public static void init(Context context) {
        if (context == null) {
            Slog.e(TAG, "init context is null, init failed");
        } else if (MiuiMultiDisplayTypeInfo.isIndependentRearDevice() && sInstance == null) {
            synchronized (MiuiSubScreenMultiFingerGestureManager.class) {
                if (sInstance == null) {
                    sInstance = new MiuiSubScreenMultiFingerGestureManager(context);
                }
            }
        }
    }

    public static void onFocusedWindowChanged(int displayId, WindowManagerPolicy.WindowState oldFocus, WindowManagerPolicy.WindowState newFocus) {
        if (sInstance == null || displayId != 1) {
            return;
        }
        for (MiuiSubScreenMultiFingerGestureListener listener : sInstance.mGestureListeners) {
            listener.onFocusedWindowChanged(oldFocus, newFocus);
        }
        sInstance.mFocusedWindow = newFocus;
        Slog.e(TAG, "onFocusedWindowChanged to " + (newFocus == null ? "null" : sInstance.mFocusedWindow.getOwningPackage()));
    }

    public static WindowManagerPolicy.WindowState getFocusedWindow() {
        if (sInstance == null) {
            return null;
        }
        return sInstance.mFocusedWindow;
    }

    public static void onGamePadConnectChanged(boolean isConnected) {
        if (sInstance == null) {
            return;
        }
        for (MiuiSubScreenMultiFingerGestureListener listener : sInstance.mGestureListeners) {
            listener.onGamePadConnectChanged(isConnected);
        }
    }

    public static void pilferPointers() {
        if (sInstance == null) {
            return;
        }
        sInstance.mMiuiGestureMonitor.pilferPointers(1);
    }

    public static void onUserSwitch(int newUserId) {
        if (sInstance == null) {
            return;
        }
        for (MiuiSubScreenMultiFingerGestureListener listener : sInstance.mGestureListeners) {
            listener.onUserSwitch(newUserId);
        }
    }

    public static void dump(final String prefix, final PrintWriter pw) {
        pw.print("    ");
        pw.println("MiuiSubScreenMultiFingerGestureManager = " + (sInstance != null ? MiuiCustomizeShortCutUtils.ATTRIBUTE_ENABLE : "disable"));
        if (sInstance == null) {
            return;
        }
        pw.println(prefix + "mGestureListeners size = " + sInstance.mGestureListeners.size());
        sInstance.mGestureListeners.forEach(new Consumer() { // from class: com.miui.server.input.gesture.multifingergesture.MiuiSubScreenMultiFingerGestureManager$$ExternalSyntheticLambda0
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                MiuiSubScreenMultiFingerGestureManager.lambda$dump$0(pw, prefix, (MiuiSubScreenMultiFingerGestureManager.MiuiSubScreenMultiFingerGestureListener) obj);
            }
        });
    }

    static /* synthetic */ void lambda$dump$0(PrintWriter pw, String prefix, MiuiSubScreenMultiFingerGestureListener listener) {
        pw.print(prefix);
        pw.println("gestureName=" + listener.getClass().getSimpleName());
        listener.dump(prefix + "    ", pw);
    }

    static void setDeviceProvisioned(boolean deviceProvisioned) {
        if (sInstance == null) {
            return;
        }
        for (MiuiSubScreenMultiFingerGestureListener listener : sInstance.mGestureListeners) {
            listener.setDeviceProvisioned(deviceProvisioned);
        }
    }

    public static void setBackscreenDoubleTapEnable(boolean enable) {
        if (sInstance == null) {
            return;
        }
        for (MiuiSubScreenMultiFingerGestureListener listener : sInstance.mGestureListeners) {
            if (listener instanceof MiuiSubscreenDoubleTapGesture) {
                ((MiuiSubscreenDoubleTapGesture) listener).setBackscreenDoubleTapEnable(enable);
            }
        }
    }

    @Override // com.miui.server.input.gesture.MiuiGestureListener
    public void onPointerEvent(MotionEvent event) {
        for (MiuiSubScreenMultiFingerGestureListener listener : this.mGestureListeners) {
            listener.onPointerEvent(event);
        }
    }

    public interface MiuiSubScreenMultiFingerGestureListener {
        default void onPointerEvent(MotionEvent event) {
        }

        default void onFocusedWindowChanged(WindowManagerPolicy.WindowState oldFocus, WindowManagerPolicy.WindowState newFocus) {
        }

        default void onGamePadConnectChanged(boolean isConnected) {
        }

        default void onUserSwitch(int newUserId) {
        }

        default void dump(String prefix, PrintWriter pw) {
        }

        default void setDeviceProvisioned(boolean deviceProvisioned) {
        }
    }
}

        找了下只在miui-services里发现了踪迹：第一个叫com.android.server.wallpaper.WallpaperManagerServiceProxy但是类名显示是在package com.android.server.wm;内容也和第二个utils类里的一样似乎是代理复制出来的？
    public int getFlipCompatModeByApp(ActivityTaskManagerService atms, String packageName) {
        try {
            ApplicationInfo applicationInfo = atms.getPackageManager().getApplicationInfo(packageName, 128L, UserHandle.myUserId());
            if (applicationInfo != null && applicationInfo.metaData != null && applicationInfo.metaData.containsKey("miui.supportFlipFullScreen")) {
                return applicationInfo.metaData.getInt("miui.supportFlipFullScreen");
            }
            return -1;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public int getFlipCompatModeByActivity(ActivityRecord activityRecord) {
        try {
            ActivityInfo activityInfo = activityRecord.mAtmService.getPackageManager().getActivityInfo(activityRecord.mActivityComponent, 128L, UserHandle.myUserId());
            if (activityInfo != null && activityInfo.metaData != null && activityInfo.metaData.containsKey("miui.supportFlipFullScreen")) {
                return activityInfo.metaData.getInt("miui.supportFlipFullScreen");
            }
            return -1;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
第二个package com.android.server.wm;
public class BoundsCompatUtils

    public int getFlipCompatModeByApp(ActivityTaskManagerService atms, String packageName) {
        try {
            ApplicationInfo applicationInfo = atms.getPackageManager().getApplicationInfo(packageName, 128L, UserHandle.myUserId());
            if (applicationInfo != null && applicationInfo.metaData != null && applicationInfo.metaData.containsKey("miui.supportFlipFullScreen")) {
                return applicationInfo.metaData.getInt("miui.supportFlipFullScreen");
            }
            return -1;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public int getFlipCompatModeByActivity(ActivityRecord activityRecord) {
        try {
            ActivityInfo activityInfo = activityRecord.mAtmService.getPackageManager().getActivityInfo(activityRecord.mActivityComponent, 128L, UserHandle.myUserId());
            if (activityInfo != null && activityInfo.metaData != null && activityInfo.metaData.containsKey("miui.supportFlipFullScreen")) {
                return activityInfo.metaData.getInt("miui.supportFlipFullScreen");
            }
            return -1;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
        第三个也是package com.android.server.wm;
        public class WindowManagerServiceImpl implements WindowManagerServiceStub这个类里的
        private int getFullScreenValue(PackageItemInfo info) {
        if (info != null && info.metaData != null && info.metaData.containsKey("miui.supportFlipFullScreen")) {
        return info.metaData.getInt("miui.supportFlipFullScreen");
        }
        return -1;

        <string name="ime_rotation_tip">当前角度不支持输入，请旋转后使用</string>
        public static final int ime_rotation_tip = 0x110f0453; 十进制：286196819
        package com.android.server.inputmethod;
        /* JADX INFO: loaded from: classes.dex */
        public class InputMethodManagerServiceImpl extends InputMethodManagerServiceStub {

        public boolean shouldShowCurrentInput(Context context) {
        boolean shouldShowCurrentInput = true;
        if (context == null) {
        return false;
        }
        if (!isFlipDevice()) {
        return true;
        }
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration == null) {
        return false;
        }
        if (configuration.getScreenType() != 1) {
        return true;
        }
        int currentRotation = getRotation(context);
        Slog.d(TAG, "currentRotation: " + currentRotation);
        if (currentRotation == 1 || currentRotation == 3) {
        shouldShowCurrentInput = false;
        }
        if (!shouldShowCurrentInput && !this.mToastPending) {
        makeRotateToast();
        }
        return shouldShowCurrentInput;
        }
        
        public void makeRotateToast() {
            this.mHandler.post(new Runnable() { // from class: com.android.server.inputmethod.InputMethodManagerServiceImpl$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$makeRotateToast$1();
                }
            });
        }
    
        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$makeRotateToast$1() {
            Toast.makeText(this.mContext, this.mContext.getString(286196819), 1000).show();
            this.mToastPending = true;
            this.mHandler.postDelayed(new Runnable() { // from class: com.android.server.inputmethod.InputMethodManagerServiceImpl$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.lambda$makeRotateToast$0();
                }
            }, 3000L);
        }
    
        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$makeRotateToast$0() {
            this.mToastPending = false;
        }


        
    - miuix.jar
	package miuix.os;

import android.os.Build;
import miuix.core.util.SystemProperties;

/* JADX INFO: loaded from: classes.dex */
public class Build {
    public static final boolean IS_DEBUGGABLE;
    public static final boolean IS_FLIP;
    public static final boolean IS_FOLDABLE;
    public static final boolean IS_FOLD_INSIDE;
    public static final boolean IS_FOLD_OUTSIDE;
    public static final boolean IS_INDEPENDENT_REAR;
    public static final boolean IS_REAR;
    public static final boolean IS_INTERNATIONAL_BUILD = SystemProperties.get("ro.product.mod_device", "").contains("_global");
    public static final boolean IS_TABLET = isTablet();
    public static final boolean IS_AUTOMOTIVE = isAutoMotive();

    static {
        IS_DEBUGGABLE = SystemProperties.getInt("ro.debuggable", 0) == 1;
        if (Build.VERSION.SDK_INT > 33) {
            int i2 = SystemProperties.getInt("persist.sys.multi_display_type", 1);
            if (i2 > 1) {
                int i3 = i2 & 15;
                IS_REAR = i3 == 2;
                IS_FOLD_INSIDE = i3 == 3;
                IS_FLIP = i3 == 4;
                IS_FOLD_OUTSIDE = i3 == 5;
                IS_INDEPENDENT_REAR = i3 == 6;
            } else {
                int i4 = SystemProperties.getInt("persist.sys.muiltdisplay_type", 0);
                IS_REAR = i4 == 1;
                IS_FOLD_INSIDE = i4 == 2;
                IS_FLIP = false;
                IS_FOLD_OUTSIDE = false;
                IS_INDEPENDENT_REAR = false;
            }
        } else {
            int i5 = SystemProperties.getInt("persist.sys.muiltdisplay_type", 0);
            IS_REAR = i5 == 1;
            IS_FOLD_INSIDE = i5 == 2;
            IS_FLIP = false;
            IS_FOLD_OUTSIDE = false;
            IS_INDEPENDENT_REAR = false;
        }
        IS_FOLDABLE = IS_FOLD_INSIDE || IS_FOLD_OUTSIDE || IS_FLIP;
    }

    private static boolean isAutoMotive() {
        return SystemProperties.get("ro.build.characteristics").contains("automotive");
    }

    private static boolean isTablet() {
        return SystemProperties.get("ro.build.characteristics").contains("tablet");
    }
}

package miuix.os;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import c.g0;
import miuix.core.util.EnvStateManager;
import miuix.core.util.WindowUtils;

/* JADX INFO: loaded from: classes.dex */
public class DeviceHelper {
    private static final int SUB_BUILTIN_DISPLAY;
    private static final String TAG = "Miuix.DeviceHelper";

    static {
        int iIntValue = -1;
        try {
            Object obj = Class.forName("android.view.Display").getDeclaredField("SUB_BUILTIN_DISPLAY").get(null);
            if (obj instanceof Integer) {
                iIntValue = ((Integer) obj).intValue();
            }
        } catch (Exception unused) {
        }
        SUB_BUILTIN_DISPLAY = iIntValue;
    }

    public static int detectType(Context context) {
        if (Build.IS_FOLD_INSIDE) {
            return 3;
        }
        if (Build.IS_FLIP) {
            return 4;
        }
        if (Build.IS_FOLD_OUTSIDE) {
            return 5;
        }
        if (Build.IS_TABLET) {
            return 2;
        }
        return Build.IS_AUTOMOTIVE ? 11 : 1;
    }

    public static boolean hasIndependentRearDisplay() {
        return Build.IS_INDEPENDENT_REAR;
    }

    public static boolean isCarWithScreen(Context context, @g0 Display display) {
        if (display == null) {
            if (Build.VERSION.SDK_INT >= 30) {
                try {
                    display = context.getDisplay();
                } catch (Exception unused) {
                }
            }
            if (display == null) {
                try {
                    display = Build.VERSION.SDK_INT >= 17 ? ((DisplayManager) context.getSystemService("display")).getDisplay(0) : ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
                } catch (Exception unused2) {
                }
            }
        }
        if (display != null) {
            return TextUtils.equals("com.miui.carlink", display.getName());
        }
        return false;
    }

    @Deprecated
    public static boolean isExternalScreen(Context context) {
        return Build.IS_FLIP ? isTinyScreen(context) : Build.IS_FOLDABLE && !isWideScreen(context);
    }

    public static boolean isFoldable() {
        return Build.IS_FOLDABLE;
    }

    public static boolean isInRearDisplay(@g0 Context context) {
        if (context == null || !hasIndependentRearDisplay() || Build.VERSION.SDK_INT < 30) {
            return false;
        }
        try {
            return context.getDisplay().getDisplayId() == SUB_BUILTIN_DISPLAY;
        } catch (Exception unused) {
            return false;
        }
    }

    @Deprecated
    public static boolean isInternalScreen(Context context) {
        return Build.IS_FLIP ? !isTinyScreen(context) : Build.IS_FOLDABLE && isWideScreen(context);
    }

    public static boolean isTablet() {
        return Build.IS_TABLET;
    }

    public static boolean isTinyScreen(Context context) {
        int screenType = WindowUtils.getScreenType(context.getResources().getConfiguration());
        if (detectType(context) == 4) {
            return screenType == 1;
        }
        Point screenSize = WindowUtils.getScreenSize(context);
        return ((int) (((float) Math.max(screenSize.x, screenSize.y)) / context.getResources().getDisplayMetrics().density)) <= 640;
    }

    public static boolean isWideScreen(Context context) {
        return ((float) EnvStateManager.getScreenShortEdge(context)) > context.getResources().getDisplayMetrics().density * 600.0f;
    }

    public static boolean isXiaomiSynergy(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), "synergy_mode", 0) == 1;
        } catch (Exception e2) {
            Log.w(TAG, "isXiaomiSynergy warning!! context cannot get synergy_mode: " + e2);
            return false;
        }
    }

    public static boolean isInRearDisplay(@g0 Display display) {
        return display != null && hasIndependentRearDisplay() && display.getDisplayId() == SUB_BUILTIN_DISPLAY;
    }
}

    package miuix.stub;
    
    import android.app.Dialog;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.res.Configuration;
    import android.util.Log;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.FrameLayout;
    import android.widget.LinearLayout;
    import miuix.animation.Folme;
    import miuix.animation.base.AnimConfig;
    import miuix.appcompat.app.AlertDialog;
    import miuix.appcompat.internal.widget.DialogButtonPanel;
    import miuix.appcompat.internal.widget.DialogParentPanel2;
    import miuix.autodensity.AutoDensityConfig;
    import miuix.os.Build;
    import miuix.os.DeviceHelper;
    
    /* JADX INFO: loaded from: classes2.dex */
    public class MiuixStub {
        private static final boolean IS_INTERNATIONAL_BUILD = Build.IS_INTERNATIONAL_BUILD;
        private static String TAG = "MiuiXStub";
        private static int sButtonMarginTop = 0;
        private static boolean sIsOpenStyle = false;
        private static int sPreferHeight = 0;
        private static boolean sPrivacyProtected = false;

    public static Dialog createMiuiAlertDialog(Context context, int i2, CharSequence charSequence, CharSequence charSequence2, boolean z2, CharSequence charSequence3, DialogInterface.OnClickListener onClickListener, CharSequence charSequence4, DialogInterface.OnClickListener onClickListener2, DialogInterface.OnCancelListener onCancelListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, i2);
        builder.setTitle(charSequence);
        builder.setMessage(charSequence2);
        builder.setCancelable(z2);
        builder.setNegativeButton(charSequence3, onClickListener);
        builder.setPositiveButton(charSequence4, onClickListener2);
        builder.setOnCancelListener(onCancelListener);
        builder.setEnableDialogImmersive(true);
        return builder.create();
    }

    public static Dialog createMiuiShareAlertDialog(Context context, int i2, CharSequence charSequence, CharSequence charSequence2, boolean z2, View view, View view2, CharSequence charSequence3, DialogInterface.OnClickListener onClickListener, CharSequence charSequence4, DialogInterface.OnClickListener onClickListener2, CharSequence charSequence5, DialogInterface.OnClickListener onClickListener3, DialogInterface.OnDismissListener onDismissListener, boolean z3, boolean z4) {
        sPrivacyProtected = z3;
        sIsOpenStyle = z4;
        resetCustomTile(view);
        boolean zIsClosable = isClosable(context);
        if (zIsClosable) {
            charSequence = null;
        } else {
            view = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context, i2);
        builder.setTitle(charSequence);
        builder.setMessage(charSequence2);
        builder.setCancelable(z2);
        builder.setCustomTitle(view);
        builder.setView(view2);
        builder.setPositiveButton(charSequence3, onClickListener);
        builder.setNeutralButton(charSequence4, onClickListener2);
        builder.setNegativeButton(charSequence5, onClickListener3);
        builder.setOnDismissListener(onDismissListener);
        builder.setEnableDialogImmersive(true);
        AlertDialog alertDialogShow = builder.show();
        resetCustomView(alertDialogShow, zIsClosable, view2);
        return alertDialogShow;
    }

    public static void dismissWithAnim(Dialog dialog) {
        if (dialog == null) {
            return;
        }
        ((AlertDialog) dialog).dismiss();
    }

    public static Context getAutoDensityContextWrapper(Context context) {
        return AutoDensityConfig.createAutoDensityContextWrapper(context);
    }

    public static boolean isClosable(Context context) {
        if (sIsOpenStyle) {
            return false;
        }
        isInSplitScreenMode(context);
        return false;
    }

    public static boolean isFlipTinyScreen(Context context) {
        return Build.IS_FLIP && DeviceHelper.isTinyScreen(context);
    }

    private static boolean isInSplitScreenMode(Context context) {
        return false;
    }

    public static void onConfigurationChanged(Dialog dialog, Configuration configuration) {
        DialogParentPanel2 dialogParentPanel2 = (DialogParentPanel2) dialog.findViewById(1711931748);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) dialogParentPanel2.getLayoutParams();
        if (isClosable(dialog.getContext())) {
            layoutParams.width = dialog.getContext().getResources().getDimensionPixelSize(R.dimen.miui_alert_dialog_big_screen_width);
            layoutParams.height = sPreferHeight;
            updateDialogSize(dialogParentPanel2, layoutParams);
        }
    }

    private static void resetCustomTile(View view) {
        if (view == null) {
            return;
        }
        LinearLayout linearLayout = (LinearLayout) view;
        int childCount = linearLayout.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = linearLayout.getChildAt(i2);
            if (childAt instanceof Button) {
                Folme.useAt(childAt).touch().handleTouchOf(childAt, new AnimConfig[0]);
                return;
            }
        }
    }

            private static void resetCustomView(AlertDialog alertDialog, boolean z2, View view) {
                if (alertDialog == null) {
                    return;
                }
                DialogParentPanel2 dialogParentPanel2 = (DialogParentPanel2) alertDialog.findViewById(1711931748);
                int dimensionPixelSize = alertDialog.getContext().getResources().getDimensionPixelSize(R.dimen.miui_alert_dialog_parent_panel_padding_top_bottom_phone);
                dialogParentPanel2.setPadding(0, dimensionPixelSize, 0, dimensionPixelSize);
                LinearLayout linearLayout = (LinearLayout) alertDialog.findViewById(1711931881);
                linearLayout.setPadding(0, 0, 0, 0);
                if (!z2) {
                    linearLayout.setPadding(0, 0, 0, alertDialog.getContext().getResources().getDimensionPixelSize(R.dimen.miui_alert_dialog_top_panel_padding_bottom));
                }
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) dialogParentPanel2.getLayoutParams();
                if (z2) {
                    int dimensionPixelSize2 = alertDialog.getContext().getResources().getDimensionPixelSize(R.dimen.miui_alert_dialog_parent_panel_padding_top_bottom_pad);
                    dialogParentPanel2.setPadding(0, dimensionPixelSize2, 0, dimensionPixelSize2);
                    ((DialogButtonPanel) alertDialog.findViewById(1711931521)).setVisibility(8);
                    layoutParams.width = alertDialog.getContext().getResources().getDimensionPixelSize(R.dimen.miui_alert_dialog_big_screen_width);
                }
                updateDialogSize(dialogParentPanel2, layoutParams);
                DialogButtonPanel dialogButtonPanel = (DialogButtonPanel) alertDialog.findViewById(1711931521);
                dialogButtonPanel.setLayoutParams((ViewGroup.MarginLayoutParams) dialogButtonPanel.getLayoutParams());
            }
        
            private static void updateDialogSize(final DialogParentPanel2 dialogParentPanel2, final FrameLayout.LayoutParams layoutParams) {
                if (dialogParentPanel2 == null || layoutParams == null) {
                    Log.d(TAG, "updateDialogSize failed");
                } else {
                    dialogParentPanel2.post(new Runnable() { // from class: miuix.stub.MiuixStub.1
                        @Override // java.lang.Runnable
                        public void run() {
                            dialogParentPanel2.setLayoutParams(layoutParams);
                        }
                    });
                }
            }
        }

    - framework-res.apk:
    	<bool name="config_fillSecondaryBuiltInDisplayCutout">false</bool>
    	<bool name="config_maskSecondaryBuiltInDisplayCutout">false</bool>
    - framework-ext.apk:
         public static final int config_second_display_touch_cover_protection_rect = 0x11030050;  285409360
             <array name="config_second_display_touch_cover_protection_rect">
                 <item>0</item>
                 <item>0</item>
                 <item>0</item>
                 <item>0</item>
             </array>







        public static boolean isTinyScreen(Context context) {
                Point screenSize = getScreenSize(context);
                return ((int) (((float) Math.max(screenSize.x, screenSize.y)) / context.getResources().getDisplayMetrics().density)) <= 670;
            }
        public static boolean isFlipTinyScreen(Context context) {
                return MiuiMultiDisplayTypeInfo.isFlipDevice() && isTinyScreen(context);
            }
    
    
        经典函数
        public class DeviceUtils {
            private static final int SCREEN_TYPE_EXPAND = 0;
            private static final int SCREEN_TYPE_FOLD = 1;
        
            public static boolean isFlipTinyScreen(Context context) {
                Configuration configuration;
                return isFlipDevice() && (configuration = context.getResources().getConfiguration()) != null && configuration.getScreenType() == 1;
            }
        
            public static boolean isFlipDevice() {
                return MiuiMultiDisplayTypeInfo.isFlipDevice();
            }
        }



package android.view;

import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.text.TextUtils;
import android.util.Log;
import android.util.PathParser;
import java.util.Objects;

/* JADX INFO: loaded from: classes4.dex */
public class CutoutSpecification {
private static final String BIND_LEFT_CUTOUT_MARKER = "@bind_left_cutout";
private static final String BIND_RIGHT_CUTOUT_MARKER = "@bind_right_cutout";
private static final String BOTTOM_MARKER = "@bottom";
private static final String CENTER_VERTICAL_MARKER = "@center_vertical";
private static final String CUTOUT_MARKER = "@cutout";
private static final boolean DEBUG = false;
private static final String DP_MARKER = "@dp";
private static final String LEFT_MARKER = "@left";
private static final char MARKER_START_CHAR = '@';
private static final int MINIMAL_ACCEPTABLE_PATH_LENGTH = "H1V1Z".length();
private static final String RIGHT_MARKER = "@right";
private static final String TAG = "CutoutSpecification";
private final Rect mBottomBound;
private Insets mInsets;
private final Rect mLeftBound;
private final Path mPath;
private final Rect mRightBound;
private final Rect mTopBound;

    private CutoutSpecification(Parser parser) {
        this.mPath = parser.mPath;
        this.mLeftBound = parser.mLeftBound;
        this.mTopBound = parser.mTopBound;
        this.mRightBound = parser.mRightBound;
        this.mBottomBound = parser.mBottomBound;
        this.mInsets = parser.mInsets;
        applyPhysicalPixelDisplaySizeRatio(parser.mPhysicalPixelDisplaySizeRatio);
    }

    private void applyPhysicalPixelDisplaySizeRatio(float physicalPixelDisplaySizeRatio) {
        if (physicalPixelDisplaySizeRatio == 1.0f) {
            return;
        }
        if (this.mPath != null && !this.mPath.isEmpty()) {
            Matrix matrix = new Matrix();
            matrix.postScale(physicalPixelDisplaySizeRatio, physicalPixelDisplaySizeRatio);
            this.mPath.transform(matrix);
        }
        scaleBounds(this.mLeftBound, physicalPixelDisplaySizeRatio);
        scaleBounds(this.mTopBound, physicalPixelDisplaySizeRatio);
        scaleBounds(this.mRightBound, physicalPixelDisplaySizeRatio);
        scaleBounds(this.mBottomBound, physicalPixelDisplaySizeRatio);
        this.mInsets = scaleInsets(this.mInsets, physicalPixelDisplaySizeRatio);
    }

    private void scaleBounds(Rect r, float ratio) {
        if (r != null && !r.isEmpty()) {
            r.scale(ratio);
        }
    }

    private Insets scaleInsets(Insets insets, float ratio) {
        return Insets.of((int) ((insets.left * ratio) + 0.5f), (int) ((insets.top * ratio) + 0.5f), (int) ((insets.right * ratio) + 0.5f), (int) ((insets.bottom * ratio) + 0.5f));
    }

    public Path getPath() {
        return this.mPath;
    }

    public Rect getLeftBound() {
        return this.mLeftBound;
    }

    public Rect getTopBound() {
        return this.mTopBound;
    }

    public Rect getRightBound() {
        return this.mRightBound;
    }

    public Rect getBottomBound() {
        return this.mBottomBound;
    }

    public Rect getSafeInset() {
        return this.mInsets.toRect();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int decideWhichEdge(boolean isTopEdgeShortEdge, boolean isShortEdge, boolean isStart) {
        return isTopEdgeShortEdge ? isShortEdge ? isStart ? 48 : 80 : isStart ? 3 : 5 : isShortEdge ? isStart ? 3 : 5 : isStart ? 48 : 80;
    }

    public static class Parser {
        private boolean mBindBottomCutout;
        private boolean mBindLeftCutout;
        private boolean mBindRightCutout;
        private Rect mBottomBound;
        private boolean mInDp;
        private Insets mInsets;
        private boolean mIsCloserToStartSide;
        private final boolean mIsShortEdgeOnTop;
        private boolean mIsTouchShortEdgeEnd;
        private boolean mIsTouchShortEdgeStart;
        private Rect mLeftBound;
        private final Matrix mMatrix;
        private Path mPath;
        private final int mPhysicalDisplayHeight;
        private final int mPhysicalDisplayWidth;
        private final float mPhysicalPixelDisplaySizeRatio;
        private boolean mPositionFromBottom;
        private boolean mPositionFromCenterVertical;
        private boolean mPositionFromLeft;
        private boolean mPositionFromRight;
        private Rect mRightBound;
        private int mSafeInsetBottom;
        private int mSafeInsetLeft;
        private int mSafeInsetRight;
        private int mSafeInsetTop;
        private final float mStableDensity;
        private final Rect mTmpRect;
        private final RectF mTmpRectF;
        private Rect mTopBound;

        public Parser(float stableDensity, int physicalDisplayWidth, int physicalDisplayHeight) {
            this(stableDensity, physicalDisplayWidth, physicalDisplayHeight, 1.0f);
        }

        Parser(float stableDensity, int physicalDisplayWidth, int physicalDisplayHeight, float physicalPixelDisplaySizeRatio) {
            this.mTmpRect = new Rect();
            this.mTmpRectF = new RectF();
            this.mPositionFromLeft = false;
            this.mPositionFromRight = false;
            this.mPositionFromBottom = false;
            this.mPositionFromCenterVertical = false;
            this.mBindLeftCutout = false;
            this.mBindRightCutout = false;
            this.mBindBottomCutout = false;
            this.mStableDensity = stableDensity;
            this.mPhysicalDisplayWidth = physicalDisplayWidth;
            this.mPhysicalDisplayHeight = physicalDisplayHeight;
            this.mPhysicalPixelDisplaySizeRatio = physicalPixelDisplaySizeRatio;
            this.mMatrix = new Matrix();
            this.mIsShortEdgeOnTop = this.mPhysicalDisplayWidth < this.mPhysicalDisplayHeight;
        }

        private void computeBoundsRectAndAddToRegion(Path p, Region inoutRegion, Rect inoutRect) {
            this.mTmpRectF.setEmpty();
            p.computeBounds(this.mTmpRectF, false);
            this.mTmpRectF.round(inoutRect);
            inoutRegion.op(inoutRect, Region.Op.UNION);
        }

        private void resetStatus(StringBuilder sb) {
            sb.setLength(0);
            this.mPositionFromBottom = false;
            this.mPositionFromLeft = false;
            this.mPositionFromRight = false;
            this.mPositionFromCenterVertical = false;
            this.mBindLeftCutout = false;
            this.mBindRightCutout = false;
            this.mBindBottomCutout = false;
        }

        private void translateMatrix() {
            float offsetX;
            float offsetY;
            if (this.mPositionFromRight) {
                offsetX = this.mPhysicalDisplayWidth;
            } else if (this.mPositionFromLeft) {
                offsetX = 0.0f;
            } else {
                offsetX = this.mPhysicalDisplayWidth / 2.0f;
            }
            if (this.mPositionFromBottom) {
                offsetY = this.mPhysicalDisplayHeight;
            } else if (this.mPositionFromCenterVertical) {
                offsetY = this.mPhysicalDisplayHeight / 2.0f;
            } else {
                offsetY = 0.0f;
            }
            this.mMatrix.reset();
            if (this.mInDp) {
                this.mMatrix.postScale(this.mStableDensity, this.mStableDensity);
            }
            this.mMatrix.postTranslate(offsetX, offsetY);
        }

        private int computeSafeInsets(int gravity, Rect rect) {
            if (gravity == 3 && rect.right > 0 && rect.right < this.mPhysicalDisplayWidth) {
                return rect.right;
            }
            if (gravity == 48 && rect.bottom > 0 && rect.bottom < this.mPhysicalDisplayHeight) {
                return rect.bottom;
            }
            if (gravity == 5 && rect.left > 0 && rect.left < this.mPhysicalDisplayWidth) {
                return this.mPhysicalDisplayWidth - rect.left;
            }
            if (gravity == 80 && rect.top > 0 && rect.top < this.mPhysicalDisplayHeight) {
                return this.mPhysicalDisplayHeight - rect.top;
            }
            return 0;
        }

        private void setSafeInset(int gravity, int inset) {
            if (gravity == 3) {
                this.mSafeInsetLeft = inset;
                return;
            }
            if (gravity == 48) {
                this.mSafeInsetTop = inset;
            } else if (gravity == 5) {
                this.mSafeInsetRight = inset;
            } else if (gravity == 80) {
                this.mSafeInsetBottom = inset;
            }
        }

        private int getSafeInset(int gravity) {
            if (gravity == 3) {
                return this.mSafeInsetLeft;
            }
            if (gravity == 48) {
                return this.mSafeInsetTop;
            }
            if (gravity == 5) {
                return this.mSafeInsetRight;
            }
            if (gravity == 80) {
                return this.mSafeInsetBottom;
            }
            return 0;
        }

        private Rect onSetEdgeCutout(boolean isStart, boolean isShortEdge, Rect rect) {
            int gravity;
            if (isShortEdge) {
                gravity = CutoutSpecification.decideWhichEdge(this.mIsShortEdgeOnTop, true, isStart);
            } else if (this.mIsTouchShortEdgeStart && this.mIsTouchShortEdgeEnd) {
                gravity = CutoutSpecification.decideWhichEdge(this.mIsShortEdgeOnTop, false, isStart);
            } else {
                gravity = (this.mIsTouchShortEdgeStart || this.mIsTouchShortEdgeEnd) ? CutoutSpecification.decideWhichEdge(this.mIsShortEdgeOnTop, true, this.mIsCloserToStartSide) : CutoutSpecification.decideWhichEdge(this.mIsShortEdgeOnTop, isShortEdge, isStart);
            }
            int oldSafeInset = getSafeInset(gravity);
            int newSafeInset = computeSafeInsets(gravity, rect);
            if (oldSafeInset < newSafeInset) {
                setSafeInset(gravity, newSafeInset);
            }
            return new Rect(rect);
        }

        private void setEdgeCutout(Path newPath) {
            if (this.mBindRightCutout && this.mRightBound == null) {
                this.mRightBound = onSetEdgeCutout(false, !this.mIsShortEdgeOnTop, this.mTmpRect);
            } else if (this.mBindLeftCutout && this.mLeftBound == null) {
                this.mLeftBound = onSetEdgeCutout(true, !this.mIsShortEdgeOnTop, this.mTmpRect);
            } else if (this.mBindBottomCutout && this.mBottomBound == null) {
                this.mBottomBound = onSetEdgeCutout(false, this.mIsShortEdgeOnTop, this.mTmpRect);
            } else if (!this.mBindBottomCutout && !this.mBindLeftCutout && !this.mBindRightCutout && this.mTopBound == null) {
                this.mTopBound = onSetEdgeCutout(true, this.mIsShortEdgeOnTop, this.mTmpRect);
            } else {
                return;
            }
            if (this.mPath != null) {
                this.mPath.addPath(newPath);
            } else {
                this.mPath = newPath;
            }
        }

        private void parseSvgPathSpec(Region region, String spec) {
            if (TextUtils.length(spec) < CutoutSpecification.MINIMAL_ACCEPTABLE_PATH_LENGTH) {
                Log.e(CutoutSpecification.TAG, "According to SVG definition, it shouldn't happen");
                return;
            }
            translateMatrix();
            Path newPath = PathParser.createPathFromPathData(spec);
            newPath.transform(this.mMatrix);
            computeBoundsRectAndAddToRegion(newPath, region, this.mTmpRect);
            if (this.mTmpRect.isEmpty()) {
                return;
            }
            if (this.mIsShortEdgeOnTop) {
                this.mIsTouchShortEdgeStart = this.mTmpRect.top <= 0;
                this.mIsTouchShortEdgeEnd = this.mTmpRect.bottom >= this.mPhysicalDisplayHeight;
                this.mIsCloserToStartSide = this.mTmpRect.centerY() < this.mPhysicalDisplayHeight / 2;
            } else {
                this.mIsTouchShortEdgeStart = this.mTmpRect.left <= 0;
                this.mIsTouchShortEdgeEnd = this.mTmpRect.right >= this.mPhysicalDisplayWidth;
                this.mIsCloserToStartSide = this.mTmpRect.centerX() < this.mPhysicalDisplayWidth / 2;
            }
            setEdgeCutout(newPath);
        }

        private void parseSpecWithoutDp(String specWithoutDp) {
            int currentIndex;
            Region region = Region.obtain();
            StringBuilder sb = null;
            int lastIndex = 0;
            while (true) {
                int currentIndex2 = specWithoutDp.indexOf(64, lastIndex);
                if (currentIndex2 == -1) {
                    break;
                }
                if (sb == null) {
                    sb = new StringBuilder(specWithoutDp.length());
                }
                sb.append((CharSequence) specWithoutDp, lastIndex, currentIndex2);
                if (specWithoutDp.startsWith(CutoutSpecification.LEFT_MARKER, currentIndex2)) {
                    if (!this.mPositionFromRight) {
                        this.mPositionFromLeft = true;
                    }
                    currentIndex = currentIndex2 + CutoutSpecification.LEFT_MARKER.length();
                } else if (specWithoutDp.startsWith(CutoutSpecification.RIGHT_MARKER, currentIndex2)) {
                    if (!this.mPositionFromLeft) {
                        this.mPositionFromRight = true;
                    }
                    currentIndex = currentIndex2 + CutoutSpecification.RIGHT_MARKER.length();
                } else if (specWithoutDp.startsWith(CutoutSpecification.BOTTOM_MARKER, currentIndex2)) {
                    parseSvgPathSpec(region, sb.toString());
                    currentIndex = currentIndex2 + CutoutSpecification.BOTTOM_MARKER.length();
                    resetStatus(sb);
                    this.mBindBottomCutout = true;
                    this.mPositionFromBottom = true;
                } else if (specWithoutDp.startsWith(CutoutSpecification.CENTER_VERTICAL_MARKER, currentIndex2)) {
                    parseSvgPathSpec(region, sb.toString());
                    currentIndex = currentIndex2 + CutoutSpecification.CENTER_VERTICAL_MARKER.length();
                    resetStatus(sb);
                    this.mPositionFromCenterVertical = true;
                } else if (specWithoutDp.startsWith(CutoutSpecification.CUTOUT_MARKER, currentIndex2)) {
                    parseSvgPathSpec(region, sb.toString());
                    currentIndex = currentIndex2 + CutoutSpecification.CUTOUT_MARKER.length();
                    resetStatus(sb);
                } else if (specWithoutDp.startsWith(CutoutSpecification.BIND_LEFT_CUTOUT_MARKER, currentIndex2)) {
                    this.mBindBottomCutout = false;
                    this.mBindRightCutout = false;
                    this.mBindLeftCutout = true;
                    currentIndex = currentIndex2 + CutoutSpecification.BIND_LEFT_CUTOUT_MARKER.length();
                } else if (specWithoutDp.startsWith(CutoutSpecification.BIND_RIGHT_CUTOUT_MARKER, currentIndex2)) {
                    this.mBindBottomCutout = false;
                    this.mBindLeftCutout = false;
                    this.mBindRightCutout = true;
                    currentIndex = currentIndex2 + CutoutSpecification.BIND_RIGHT_CUTOUT_MARKER.length();
                } else {
                    currentIndex = currentIndex2 + 1;
                }
                lastIndex = currentIndex;
            }
            if (sb == null) {
                parseSvgPathSpec(region, specWithoutDp);
            } else {
                sb.append((CharSequence) specWithoutDp, lastIndex, specWithoutDp.length());
                parseSvgPathSpec(region, sb.toString());
            }
            region.recycle();
        }

        public CutoutSpecification parse(String originalSpec) {
            String spec;
            Objects.requireNonNull(originalSpec);
            int dpIndex = originalSpec.lastIndexOf(CutoutSpecification.DP_MARKER);
            this.mInDp = dpIndex != -1;
            if (dpIndex != -1) {
                spec = originalSpec.substring(0, dpIndex) + originalSpec.substring(CutoutSpecification.DP_MARKER.length() + dpIndex);
            } else {
                spec = originalSpec;
            }
            parseSpecWithoutDp(spec);
            this.mInsets = Insets.of(this.mSafeInsetLeft, this.mSafeInsetTop, this.mSafeInsetRight, this.mSafeInsetBottom);
            return new CutoutSpecification(this);
        }
    }
}
<string name="config_secondaryBuiltInDisplayCutout" />
<string name="config_secondaryBuiltInDisplayCutoutRectApproximation">@string/config_secondaryBuiltInDisplayCutout</string>
<bool name="config_fillSecondaryBuiltInDisplayCutout">false</bool>
<bool name="config_maskSecondaryBuiltInDisplayCutout">false</bool>
<string name="config_secondaryBuiltInDisplayCutout">M 604,664 L 604,1392 L 206,1392 L 206,664 Z @bind_right_cutout</string>对应资源 ID：0x7f070003。(十进制2131165187)
public static final int config_secondaryBuiltInDisplayCutoutSideOverride = 0x010700ce; 17236174
public static final int config_fillSecondaryBuiltInDisplayCutout = 0x011101ae; 17891758
public static final int config_maskSecondaryBuiltInDisplayCutout = 0x011101f4;  17891828
public static final int config_secondaryBuiltInDisplayCutout = 0x0104032f;  17040175
public static final int config_secondaryBuiltInDisplayCutoutRectApproximation = 0x01040330;  17040176
