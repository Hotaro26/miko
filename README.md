#  Miko [Manga Reader]

Miko is a high-performance, ultra-minimalist manga reader built with **Kotlin** and **Jetpack Compose**. It follows the **Material 3 Expressive** design guidelines to provide a fluid, modern, and highly customizable reading experience. It has online catalogues from manga dex database along with anilist integration!

<p align="center">
<img width="300" height="300" alt="miko1" src="https://github.com/user-attachments/assets/78d26201-6189-4a29-9631-cc81fef51855" />

  
</p>

##  Features

-  **Performance-First**: Built with native Kotlin for smooth scrolling and fast image streaming using Coil.
-  **Material 3 Expressive**: Large corner radii, bold typography, and seamless transitions.
-  **Deep Customization**:
    - **Dynamic Color (M3)**: Automatically adapts to your system wallpaper (Android 12+).
    - **Theme Modes**: Full support for System, Light, and Dark modes.
    - **Color Schemes**: Choose from presets like Lavender, Forest, Midnight, Rose, and a high-contrast Monochrome.
- **Powered by MangaDex**: Access a massive library of manga with built-in search and suggestions.
- **Flexible Reader**: Supports both **Vertical (Webtoon)** and **Paged** reading modes with a progress indicator.
- **History & Library**: Automatically tracks your reading progress and lets you save your favorites locally using Room.
- **Support Developer**: Optional opt-in ads and direct UPI support to help keep the project alive.

##  Screenshots

| | | |
|:---:|:---:|:---:|
| <img width="250" src="https://github.com/user-attachments/assets/c284bc12-6895-497f-b06d-f2ee88d93139" /> | <img width="250" src="https://github.com/user-attachments/assets/fa602966-9fbd-4441-9387-484d1838195f" /> | <img width="250" src="https://github.com/user-attachments/assets/b21eee81-2012-4a28-9788-f19b5e6add12" /> |
| <img width="250" src="https://github.com/user-attachments/assets/c463b0f9-17f3-449f-9754-9bcbf4107906" /> | <img width="250" src="https://github.com/user-attachments/assets/f5a27235-46cb-4524-a293-9d34df4a11c0" /> | <img width="250" src="https://github.com/user-attachments/assets/1ae623f0-5746-447d-943b-6aca4f4c8ecb" /> |
##  Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Networking**: Retrofit & OkHttp
- **Database**: Room (SQLite)
- **Image Loading**: Coil
- **Ads**: Google Mobile Ads SDK (AdMob)
- **Navigation**: Compose Navigation

##  How to Build

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

## Star History

<a href="https://www.star-history.com/?repos=Hotaro26%2Fmiko.git&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=Hotaro26/miko.git&type=date&theme=dark&legend=top-left&sealed_token=wcfKetPedn4XpDDgUr7w1o04d769wnHQ1Ch09O2qkUESaJTAgQZPQmiNv4OIVp1u3RB7jez-1FRTlBJpZvJItkAY5JVsUaomwk2PyAn_1WlRqwZvni4j5DeqwjTO5Fg3HEIlF_9JaOlKNXxTXHglXrcKxzrVWSslQxihtv9r-jg2SFp6GEmNNsELInTW" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=Hotaro26/miko.git&type=date&legend=top-left&sealed_token=wcfKetPedn4XpDDgUr7w1o04d769wnHQ1Ch09O2qkUESaJTAgQZPQmiNv4OIVp1u3RB7jez-1FRTlBJpZvJItkAY5JVsUaomwk2PyAn_1WlRqwZvni4j5DeqwjTO5Fg3HEIlF_9JaOlKNXxTXHglXrcKxzrVWSslQxihtv9r-jg2SFp6GEmNNsELInTW" />
   <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=Hotaro26/miko.git&type=date&legend=top-left&sealed_token=wcfKetPedn4XpDDgUr7w1o04d769wnHQ1Ch09O2qkUESaJTAgQZPQmiNv4OIVp1u3RB7jez-1FRTlBJpZvJItkAY5JVsUaomwk2PyAn_1WlRqwZvni4j5DeqwjTO5Fg3HEIlF_9JaOlKNXxTXHglXrcKxzrVWSslQxihtv9r-jg2SFp6GEmNNsELInTW" />
 </picture>
</a>
