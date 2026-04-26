# 🌸 Miko Manga Reader

Miko is a high-performance, ultra-minimalist manga reader built with **Kotlin** and **Jetpack Compose**. It follows the **Material 3 Expressive** design guidelines to provide a fluid, modern, and highly customizable reading experience.

![Miko Icon](miko_icon_no_mouth.svg)

## ✨ Features

- **🚀 Performance-First**: Built with native Kotlin for smooth scrolling and fast image streaming using Coil.
- **🎨 Material 3 Expressive**: Large corner radii, bold typography, and seamless transitions.
- **🌈 Deep Customization**:
    - **Dynamic Color (M3)**: Automatically adapts to your system wallpaper (Android 12+).
    - **Theme Modes**: Full support for System, Light, and Dark modes.
    - **Color Schemes**: Choose from presets like Lavender, Forest, Midnight, Rose, and a high-contrast Monochrome.
- **📚 Powered by MangaDex**: Access a massive library of manga with built-in search and suggestions.
- **📖 Flexible Reader**: Supports both **Vertical (Webtoon)** and **Paged** reading modes with a progress indicator.
- **💾 History & Library**: Automatically tracks your reading progress and lets you save your favorites locally using Room.
- **💖 Support Developer**: Optional opt-in ads and direct UPI support to help keep the project alive.

## 🛠️ Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Networking**: Retrofit & OkHttp
- **Database**: Room (SQLite)
- **Image Loading**: Coil
- **Ads**: Google Mobile Ads SDK (AdMob)
- **Navigation**: Compose Navigation

## 🚀 How to Build

Miko is designed for "no-IDE" development and can be compiled entirely from the terminal.

1. **Clone the Repo**:
   ```bash
   git clone https://github.com/Hotaro26/miko.git
   cd miko
   ```

2. **Setup your AdMob (Optional)**:
   - Replace the `APPLICATION_ID` in `app/src/main/AndroidManifest.xml`.
   - Replace the `AD_UNIT_ID` in `app/src/main/java/com/miko/reader/util/AdHelper.kt`.

3. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on Device**:
   ```bash
   ./gradlew installDebug
   ```

## 👤 Developer

- **Author**: [hotaro](https://github.com/Hotaro26)
- **Discord**: `oi.hotaro`
- **License**: MIT / Apache 2.0

---
*Built with precision and passion.*
