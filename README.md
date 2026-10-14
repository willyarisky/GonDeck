# BrainDeck 🚀

**BrainDeck** transforms your Android phone (Google Pixel 7a, Tensor, Snapdragon) into a fully functional **Mini Laptop Desktop Computer**: running a complete KDE Plasma 6 desktop environment with terminal, multi-window productivity apps, file manager, web browsing, and Linux development tools.

Forked from GonDeck / DroidDeck, **BrainDeck** shifts the paradigm from gaming handheld emulation to a clean, fast, and responsive **Pocket Mini Laptop OS** designed specifically for hardware like Cyberdeck kits, mini Bluetooth keyboards (AxPro/M7), and external monitors.

---

## 🌟 What Makes BrainDeck Unique?

1. **Full Linux Desktop & Mini Laptop Experience:**
   - Powers up directly into **KDE Plasma Desktop** with taskbar, start menu, multi-window management, clipboard, and full-screen workspace.
   - Built for mini keyboards (AxPro/M7, Bluetooth) and trackpads/mouse controls.
2. **Native ARM Mali & Google Tensor Support:**
   - Hardware detection for `/dev/mali0` and Valhall architecture (Mali-G710 on Pixel 7a, G715, G720, etc.) without requiring Adreno Mesa Turnip.
   - Dual rendering engine: fast, crash-free software rendering for desktop stability + native hardware acceleration.
3. **Flathub & Linux App Ecosystem:**
   - Install desktop software directly from the built-in Flathub store: LibreOffice, VS Code / Code-OSS, Chromium/Firefox, GIMP, VLC, and developer tools.
4. **Automated Phantom Process Fix:**
   - Seamlessly handles Android 12-15 background process restrictions so your background desktop tasks don't get killed.
5. **Productivity Focused (No Gaming Clutter):**
   - Cleaned up gaming bloatware, focusing on speed, responsiveness, and genuine mini-laptop utility.

---

## 📱 Hardware Requirements

- **Supported Chipsets:**
  - **Google Tensor Series:** Tensor G2 (Pixel 7, 7 Pro, 7a), Tensor G3, Tensor G4.
  - **MediaTek Dimensity:** Dimensity 8000/9000 series with Mali-G610/G710/G715/G720.
  - **Qualcomm Snapdragon:** Full support for Adreno 6xx, 7xx, and 8xx.
- **Android Version:** Android 10 or newer (tested on Android 14/15). No root required.
- **Storage:** ~850 MB for the base desktop environment and runtime.

---

## 📥 Installation

Download the latest release APK from [BrainDeck Releases](https://github.com/willyarisky/GonDeck/releases) or install via ADB:
```bash
adb install -r BrainDeck-release.apk
```

---

## 🛠️ Debugging & Troubleshooting

### 1. Capturing Real-Time Android Logcat

To inspect session startup, process lifecycle, and desktop events:

```bash
adb logcat -d | grep -iE "braindeck|droiddeck|SessionService|HostProcess|wayland|mali"
```

### 2. Inspecting Desktop & App Logs

Session logs are automatically organized in the app directory:
- App log: `/data/user/0/com.droiddeck.launcher/files/logs/<session-id>/app.log`
- Session stdout/stderr: `/data/user/0/com.droiddeck.launcher/files/logs/<session-id>/session.log`

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
```

---

## 📄 Credits & License

- **BrainDeck:** Developed by [Willy Arisky](https://github.com/willyarisky).
- **Core Engine:** Built upon DroidDeck / GonDeck and the open-source Linux-on-Android ecosystem.
- **License:** GPL-3.0 License.
