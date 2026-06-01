# 📱 UPI PayTrack — UPI Budget & Expense Tracker

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-Material3-purple.svg)](https://developer.android.com/jetpack/compose)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

**UPI PayTrack** is a native, modern, and privacy-first Android application designed to streamline personal financial management and group expense sharing. Powered by **Jetpack Compose**, **Room Database**, and **Kotlin Flows**, the app provides a highly reactive, responsive, and beautiful user experience offline, with zero external server dependencies.

---

## 🌟 Key Features

* **💳 Personal Ledger**: Keep record of all daily transactions. Categorize expenses (Food, Shopping, Entertainment, Rent, Bills, etc.) and visualize cash flow trends.
* **👥 Roommates & Family splits**: Seamlessly register group members, log joint expenditures, and calculate equal splits. Includes single-tap settles and quick payment details sharing.
* **📊 Limits & Period Budgets**: Define monthly category caps, weekly allowances, or trip-specific custom budgets.
* **🔔 Reactive Alerts**: Stay informed with persistent warning overlays and system notifications when your category spending reaches **50%**, **80%**, or exceeds **100%** of your allocated budget.
* **🎯 Savings Targets**: Create visual campaigns for major goals (e.g., vacation fund, vehicle purchase) with dynamic days-left trackers and incremental funding options.
* **📈 Smart Analytics & Reports**:
  * **Cash Flow Curve**: Custom Canvas-drawn chart plotting daily income vs. expense.
  * **Certified CSV Report Sharing**: Export detailed monthly reports to a CSV file and share them via your device's native sharing sheet.
  * **Habits & Patterns Identifier**: Highlighting weekend outlays and subscription services.
* **🎮 Interactive Sandbox Simulator**: A zero-stakes, offline playground where you can simulate daily activities to see how split algorithms and budget alert systems react under the hood!

---

## 🛠️ Architecture & Tech Stack

This project is built following standard **Android Modern App Architecture Guidelines (MVVM)**:

* **UI Layer**: Jetpack Compose (Declarative UI) with Material 3 design tokens.
* **State Management**: `ViewModel` combined with asynchronous `StateFlow` and `CoroutineScope` for handling UI updates reactively.
* **Storage Layer**: SQLite abstracted via **Room Database** utilizing asynchronous queries (Kotlin Flows) for automatic UI updates when the database updates.
* **Dependency Catalog**: Configured using modern Gradle **Version Catalogs (`libs.versions.toml`)** for cleaner, centralized version management.

---

## 🚀 Getting Started (Developer Setup)

Follow these instructions to clone, build, and run the project locally on your machine.

### 📋 Prerequisites

* **Android Studio** (Ladybug 2024.2.1 or newer recommended)
* **JDK 17** or newer
* An **Android emulator** or a **physical Android device** running Android 7.0 (API Level 24) or higher.

### 📥 Building the App

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/upi-budget-tracker.git
   cd upi-budget-tracker
   ```

2. **Open in Android Studio**:
   * Open Android Studio.
   * Select **Open An Existing Project** and choose the root directory of this project.
   * Allow Android Studio to index the project files and perform initial project synchronization.
   * *Note: If Gradle Wrapper files (`gradlew`, `gradlew.bat`) are missing from the folder, Android Studio will automatically generate them during the project import/sync.*

3. **Configure Environment File**:
   * Create a copy of the environment template file:
     ```bash
     cp .env.example .env
     ```
   * *Note: While the app is fully local and does not use any cloud servers out of the box, the secrets plugin uses this file structure for modularity.*

4. **Signing configuration**:
   * By default, the debug build uses a standard local debug signing key.
   * For release builds, make sure you configure your production keystore path and passwords in `gradle.properties` or environment variables:
     * `KEYSTORE_PATH`
     * `STORE_PASSWORD`
     * `KEY_PASSWORD`

5. **Run the App**:
   * Connect your physical device (with USB debugging enabled) or start your emulator.
   * Click the green **Run (Play)** button in Android Studio's top toolbar, or select `app` from the run configuration drop-down.

---

## 🤝 Contributing

Contributions make the open-source community an amazing place. Any contributions you make are **greatly appreciated**. 

Please read our [Contributing Guidelines](CONTRIBUTING.md) to understand our coding standards, branch naming conventions, and Pull Request submission process. Please review the [Code of Conduct](CODE_OF_CONDUCT.md) before joining community discussions.

---

## 📜 License

Distributed under the MIT License. See [LICENSE](LICENSE) for more information.
