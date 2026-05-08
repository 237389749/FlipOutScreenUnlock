# MixFlip Full-Screen Fix Module

**An LSPosed module that unlocks the full potential of the cover screen on foldable devices (e.g., Xiaomi MIX Flip).**  
**一个 LSPosed 模块，用于释放折叠屏手机外屏的全部潜力（如 Xiaomi MIX Flip）。**

---

## Requirements / 要求
- Rooted device (Magisk / KernelSU / APatch)  
- Zygisk Next
- LSPosed(or compatible Xposed framework)  


## Features / 功能
- **Cover screen keyboard now works in landscape mode**  
  外屏输入法横屏可用
- **All apps run in true full screen on the cover display, with copy/paste fully functional**  
  外屏全应用全屏显示，且复制粘贴功能正常
- **No app launch restrictions or annoying popups on the cover screen; apps from the inner screen can seamlessly move to the outer screen**  
  外屏应用无启动限制与弹窗，内屏应用均可流转到外屏上使用

## Known Issue / 已知问题
- The camera preview is flipped upside down when using the scanner (e.g., QR code scan) on the cover screen.  
  扫一扫（二维码扫描）时，外屏预览画面上下倒置。

## Installation / 安装方法
1. Make sure ZygiskNext And LSPosed is installed and working.  
   确保 ZygiskNext和LSPosed 已安装并正常工作。
2. Enable this module in LSPosed.  
   在 LSPosed 中启用本模块。
3. In the module's scope settings, check the following apps:  
   在模块的作用域中勾选以下应用：
   - `Android System` (Android系统 android)
   - `System FrameWork`(系统框架 system)
   - `System UI` (系统界面 com.android.systemui)
   - `Camera` (相机 com.android.camera)
   - `Always-on display ` (息屏与锁屏编辑 com.miui.aod)
4. Reboot the device.  
   重启设备。
5. The fixes should take effect immediately after reboot.  
   重启后修复即刻生效。
6. 如果不能安装请用mt管理器等授予root后安装，或在开发者选项打开usb安全设置
## Disclaimer / 免责声明
This module is provided for educational and research purposes. Some implementations may be derived from decompiled code reference. Use at your own risk. The developer is not responsible for any damage or data loss.  
本模块仅供学习和研究使用。部分实现可能参考了反编译代码。使用风险自负，开发者不承担任何责任。


---

*Pull requests and bug reports are welcome!*  
*欢迎提交 Pull Request 和 Bug 反馈！*
