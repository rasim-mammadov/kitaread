<div align="center">

# 📚 KitaRead

### A lightweight Android reading app for EPUB, PDF, and CBZ

**A personal project exploring multi-format document rendering, reading progress tracking, and native Android development.**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
![Version](https://img.shields.io/badge/Version-1.1-blue)
![Status](https://img.shields.io/badge/Status-APK%20Available-brightgreen)

[![Download APK](https://img.shields.io/badge/Download-KitaRead%20v1.1%20APK-2EA44F?logo=android&logoColor=white&style=for-the-badge)](https://github.com/rasim-mammadov/kitaread/releases/download/v1.1/KitaRead-v1.1.apk)

[Release page](https://github.com/rasim-mammadov/kitaread/releases/tag/v1.1)

</div>

---

## Download & installation

**KitaRead v1.1 is now available as an Android APK**, published on November 24, 2026.

- [Download KitaRead-v1.1.apk](https://github.com/rasim-mammadov/kitaread/releases/download/v1.1/KitaRead-v1.1.apk)
- [View GitHub Release](https://github.com/rasim-mammadov/kitaread/releases/tag/v1.1)

**Requirements:** Android 8.0 (API 26) or later.

1. Download the APK to your Android device.
2. Open the downloaded file.
3. If prompted, allow installation from the specific browser or file manager used to open it.
4. Install and launch KitaRead. You can disable that installation permission afterwards.

Android Studio is not required to install the APK. The app is not currently distributed through Google Play or the App Store.

## Overview

**KitaRead** is an Android e-book and document reader designed around a simple personal library and a distraction-free reading flow. It supports importing locally stored EPUB, PDF, and CBZ files, navigating their contents, and resuming at a saved reading position.

> **Development status:** v1.1 is publicly available for download and initial use. Initial testing has been completed, but comprehensive validation across physical Android devices and document variants is still ongoing.

## Features

- **Multi-format import** — add local `.epub`, `.pdf`, and `.cbz` files using Android's document picker.
- **Personal library** — see imported books with their reading status and progress.
- **Reading status management** — mark a book as *Not Started*, *Reading*, or *Finished*.
- **Progress tracking** — maintain a per-book percentage and page/chapter position.
- **Resume reading** — store reading position and scroll progress locally.
- **Page navigation** — move between PDF pages, CBZ images, or EPUB chapters.
- **Local storage** — maintain book metadata on-device.
- **Offline reading** — read locally imported books without an internet connection.
- **Reading statistics** — track reading activity and habits.
- **Free to use** — no paywall for current reading features.

## Technology

| Area | Implementation |
| --- | --- |
| Platform | Native Android |
| Language | Kotlin |
| Build system | Gradle (Kotlin DSL) |
| EPUB rendering | WebView-based chapter rendering |
| PDF rendering | Android `PdfRenderer` |
| CBZ rendering | ZIP archive image decoding |
| Local preferences | Android `SharedPreferences` |
| Continuous integration | GitHub Actions (debug APK build) |

## Getting started (for developers)

**Requirements:** Android Studio, JDK 17, and Android SDK 35. The app configuration specifies **minimum Android API level 26**.

1. Clone the repository:

   ```bash
   git clone https://github.com/rasim-mammadov/kitaread.git
   cd kitaread
   ```

2. Open the project in **Android Studio** and let Gradle synchronize.
3. Run the `app` configuration on an emulator or compatible Android device.

### Building the debug APK

The GitHub Actions workflow builds the project with:

```bash
gradle :app:assembleDebug --no-daemon
```

To inspect available workflow runs and their build artifacts, open [GitHub Actions](https://github.com/rasim-mammadov/kitaread/actions). **A successful APK build does not guarantee that all reading functions have passed device testing.**

## Current limitations & next steps

- Complete regression testing for EPUB, PDF, and CBZ across representative files.
- Validate position restoration, chapter/page navigation, and error handling on real devices.
- Refine the user interface and complete the transition from the earlier **Leaf Reader** branding to **KitaRead**.
- Add verified application screenshots and a short demonstration once testing is complete.

These items describe intended improvements, not functionality claimed to be complete.

## Feedback & roadmap

Bug reports, feature suggestions and feedback are welcome through [GitHub Issues](https://github.com/rasim-mammadov/kitaread/issues). Future plans include continued Android improvements and exploring distribution through Google Play and an iOS version for the App Store.

## Project structure

```text
kitaread/
├── .github/workflows/android-apk.yml  # CI debug APK workflow
├── app/
│   ├── build.gradle.kts               # Android app configuration
│   └── src/main/                     # Android source and manifest
├── build.gradle.kts
└── settings.gradle.kts
```

## Author

**Rasim Mammadov**  
Banking & Risk Analytics Professional · Data Analytics & BI · Independent Developer

[GitHub profile](https://github.com/rasim-mammadov)

---

<div align="center"><sub>Built as an independent learning and product-development project.</sub></div>
