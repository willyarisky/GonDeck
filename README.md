# GonDeck 🚀

**GonDeck** brings the full SteamOS Big Picture and desktop PC gaming ecosystem to Android: Valve's official Steam client, Windows PC gaming via ARM64 Proton and FEX-Emu, and full integration with KDE Plasma Desktop.

Forked from DroidDeck, **GonDeck** breaks the Qualcomm Adreno-only restriction, bringing full native compatibility to **ARM Mali GPUs (Google Tensor G2/G3/G4 in Pixel 7/8/9 series, and MediaTek Dimensity)** using custom Vulkan interception layers and [malitools](https://github.com/willyarisky/malitools).

---

## 🌟 What Makes GonDeck Different?

1. **Native ARM Mali & Google Tensor Support:**
   - Automatic detection of `/dev/mali0` and Valhall architecture (Mali-G710 on Pixel 7a, G715, G720, etc.).
   - Replaces the hardcoded requirement for Adreno Mesa Turnip (`kgsl-3d0`) with direct compositing on Android's native `libvulkan.so`.
2. **Bundled [malitools](https://github.com/willyarisky/malitools) Vulkan Shim:**
   - Injects missing desktop Vulkan extensions required by DXVK / Proton (`VK_EXT_transform_feedback`, `VK_EXT_robustness2`, `VK_EXT_custom_border_color`, etc.) directly into the GPU pipeline.
   - Prevents `VK_ERROR_EXTENSION_NOT_PRESENT` fatal crashes during 3D game initialization.
3. **Automated Dependency & Tool Installation:**
   - Automatically requests and installs **FEX-Emu (App ID `3127680`)** and **SteamLinuxRuntime Sniper (`1628350`)** from Steam on first launch without manual searching.
   - Automatically assigns **Proton Experimental (ARM64)** to all Windows titles in Steam without requiring per-game manual compatibility property changes.
4. **Stable Dual-Layer Rendering Engine:**
   - Steam client UI and KDE Plasma run on lightweight, crash-free Software / LLVMpipe rasterization (bypassing CEF ANGLE / GLX crashes).
   - 3D Games render through hardware-accelerated Zink / Malitools Vulkan pipeline.
5. **Direct Touch & Gamepad Integration:**
   - Touchscreen input works as native direct-tap on desktop and Steam UI.
   - Built-in recognition for Xbox Series S|X, DualSense, DualShock 4, and Steam Deck controllers.

---

## 📱 Hardware Requirements

- **Supported Chipsets:**
  - **Google Tensor Series:** Tensor G2 (Pixel 7, 7 Pro, 7a), Tensor G3 (Pixel 8 series), Tensor G4 (Pixel 9 series).
  - **MediaTek Dimensity:** Dimensity 8000/9000 series with Mali-G610/G710/G715/G720.
  - **Qualcomm Snapdragon:** Full upstream support for Adreno 6xx, 7xx, and 8xx via Mesa Turnip remains 100% active.
- **Android Version:** Android 10 or newer (tested on Android 14). No root required.
- **Storage:**
  - ~800 MB for Base Linux Rootfs Runtime.
  - ~850 MB for KDE Plasma Desktop.
  - Storage space for Steam games (e.g. 1-5 GB depending on title).

---

## 📥 Installation Guide

### Method 1: Prebuilt APK (Recommended)

1. Download the latest release APK from [GonDeck Releases](https://github.com/willyarisky/GonDeck/releases).
2. Install via ADB or transfer to phone:
   ```bash
   adb install -r GonDeck-0.4.0-mali.apk
   ```
3. Grant necessary permissions (Storage / Notifications).

### Method 2: Android Developer Settings (Fix Phantom Process Limits)

Android 12+ aggressively kills heavy background processes by default. To prevent games and Steam from closing silently, disable child process restrictions via ADB:

```bash
adb shell "device_config set_sync_disabled_for_tests persistent"
adb shell "device_config put activity_manager max_phantom_processes 2147483647"
adb shell "settings put global settings_enable_monitor_phantom_procs false"
```

---

## 🚀 First-Time Setup & Launch

1. **Launch GonDeck** from your Android launcher.
2. On first launch, click **Desktop** on the left rail -> **Install & open desktop**. GonDeck will download and extract the KDE Plasma desktop package (~846 MB).
3. Once the desktop appears:
   - Steam will automatically launch in desktop mode.
   - Sign in with your Steam account or scan the QR code using the Steam Mobile App.
4. **Automatic Component Installation:**
   - GonDeck automatically requests **Proton Experimental (ARM64)** and **FEX-Emu** in Steam Downloads.
   - Wait for the downloads in the **Downloads** tab to complete.
5. **Install & Play Games:**
   - Install any Windows game (e.g. *Among Us*, *Garfield Kart*, indie titles).
   - Press **PLAY**. GonDeck automatically handles all compatibility tools and rendering flags!

---

## 🛠️ Debugging & Troubleshooting

### 1. Capturing Real-Time Android Logcat

To inspect session startup, process lifecycle, and kernel events:

```bash
adb logcat -d | grep -iE "droiddeck|SessionService|HostProcess|wayland|mali"
```

### 2. Inspecting Steam & Game Logs

Session logs are automatically organized in the app directory:
- App log: `/data/user/0/com.droiddeck.launcher/files/logs/<session-id>/app.log`
- Session stdout/stderr: `/data/user/0/com.droiddeck.launcher/files/logs/<session-id>/session.log`
- Steam client logs: `/data/user/0/com.droiddeck.launcher/files/logs/<session-id>/steam/`

To export logs to your external storage without root:
- Inside the GonDeck session drawer, tap **Share logs**. The zipped archive will be shared or exported.

### 3. Proton & DXVK Debugging Logs

When running on ARM Mali, GonDeck can export granular DirectX / Vulkan logs directly to public storage:

```bash
adb shell "ls -la /sdcard/Android/data/com.droiddeck.launcher/files/"
```
Look for:
- `steam-<appid>.log` (Proton runtime trace)
- `<game_exe>_dxgi.log` & `<game_exe>_d3d11.log` (DXVK API calls and driver fallback state)

### 4. Overriding Display & Driver Manually

If you need to force custom driver behavior, write to `/sdcard/Download/droiddeck-driver`:
- `system` — Forces Android native `libvulkan.so` (default on Mali).
- `a7xx` / `a8xx` — Forces Turnip builds on Adreno.

---

## 🏗️ Building from Source

### Prerequisites:
- Ubuntu 22.04 / 24.04 or Debian-based Linux
- OpenJDK 17 or 21
- Android SDK (API 34, Build-Tools 34.0.0, NDK r26b)
- CMake 3.22+, Python 3, zstd, and Apktool

### Build Command:
```bash
git clone https://github.com/willyarisky/GonDeck.git
cd GonDeck

# Build using Android Gradle Plugin
./gradlew assembleRelease

# Or package using the custom Apktool & NDK toolchain helper
./tools/build_local.sh
```

---

## 📄 Credits & License

- **GonDeck:** Developed by [Willy Arisky](https://github.com/willyarisky).
- **Core Engine:** Built on the foundations of [DroidDeck](https://github.com/Droid-Deck/DroidDeck), WinNative, and Bannerlator (maxjivi05).
- **Mali Vulkan Shim:** Powered by [malitools](https://github.com/willyarisky/malitools).
- **License:** GPL-3.0 License.
- **Trademark Notice:** Steam, SteamOS, and Proton are trademarks of Valve Corporation. This project is open-source and not affiliated with Valve Corporation.
