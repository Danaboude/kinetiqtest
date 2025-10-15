# Kinetiq Protocol Integration

## Description
This Android application serves as a client for the Kinetiq Protocol, enabling users to stake HYPE tokens and receive kHYPE tokens in return. It connects to the Hyperliquid network, allowing users to interact with the Kinetiq smart contracts for staking operations. The app provides a secure (for testing purposes with private keys) and user-friendly interface for managing HYPE and kHYPE balances, performing staking transactions, and monitoring staking limits.

## Features
-   **Wallet Connection:** Securely connect your wallet using a private key (for testing/development), or through WalletConnect and MetaMask (integration placeholders).
-   **HYPE Staking:** Stake your HYPE tokens to earn kHYPE.
-   **kHYPE Balance Tracking:** View your current kHYPE balance.
-   **Real-time Conversion:** See the expected kHYPE amount for your HYPE stake in real-time.
-   **Staking Limits:** Displays minimum and maximum staking amounts, as well as the protocol's total staking limit.
-   **Transaction Status & Feedback:** Provides clear feedback on transaction loading, success, and errors, including specific error messages for insufficient funds, invalid amounts, and network issues.
-   **Slippage Warning:** Notifies users if the received kHYPE is significantly less than expected due to slippage.
-   **Dark Theme UI:** A modern and intuitive user interface built with Jetpack Compose, featuring Kinetiq's brand-specific dark theme.
-   **Hyperliquid Network Integration:** Interacts with smart contracts deployed on the Hyperliquid EVM network.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. This application interacts with blockchain smart contracts, so understanding of Web3 concepts is beneficial.

### Prerequisites
-   Android Studio (recommended)
-   Java Development Kit (JDK) 17 or higher
-   Android SDK
-   A private key for a test wallet on the Hyperliquid network (for private key login)

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/your-username/your-repo-name.git
    cd andriod_test
    ```
2.  Open the project in Android Studio.
    Android Studio should automatically detect the Gradle project and prompt you to sync it. If not, you can manually sync by clicking "Sync Project with Gradle Files" in the toolbar.

## Project Structure
This project follows the standard Android project structure:

```
andriod_test/
├── app/                  # Contains the application module
│   ├── src/              # Source code and resources
│   │   ├── main/         # Main source set (Java/Kotlin code, resources, AndroidManifest.xml)
│   │   ├── androidTest/  # Instrumented tests
│   │   └── test/         # Local unit tests
│   └── build.gradle.kts  # Module-level Gradle build file
├── gradle/               # Gradle wrapper files
├── build.gradle.kts      # Project-level Gradle build file
├── settings.gradle.kts   # Defines project modules
├── gradlew               # Gradle wrapper script (Linux/macOS)
├── gradlew.bat           # Gradle wrapper script (Windows)
└── README.md             # This file
```

## Building the Project
To build the project, you can use Android Studio or the Gradle command line.

### Using Android Studio
Click on `Build > Make Project` or `Build > Rebuild Project`.

### Using Gradle Command Line
Navigate to the project root directory (`andriod_test/`) in your terminal and run:

On Windows:
```bash
.\gradlew.bat build
```

On Linux/macOS:
```bash
./gradlew build
```

## Running the App

### Using Android Studio
1.  Connect an Android device or start an Android Virtual Device (AVD).
2.  Click the "Run" button (green triangle) in the toolbar.

### Using Gradle Command Line
To install the debug APK on a connected device or running emulator:

On Windows:
```bash
.\gradlew.bat installDebug
```

On Linux/macOS:
```bash
./gradlew installDebug
```

## Testing

### Local Unit Tests
Located in `app/src/test/java/`.

To run local unit tests:

On Windows:
```bash
.\gradlew.bat testDebugUnitTest
```

On Linux/macOS:
```bash
./gradlew testDebugUnitTest
```

### Instrumented Tests
Located in `app/src/androidTest/java/`.
These tests run on an actual device or emulator.

To run instrumented tests:

On Windows:
```bash
.\gradlew.bat connectedDebugAndroidTest
```

On Linux/macOS:
```bash
./gradlew connectedDebugAndroidTest
```

## Contributing
Please read `CONTRIBUTING.md` (if available) for details on our code of conduct, and the process for submitting pull requests to us.

## License
This project is licensed under the Apache 2.0 License - see the `LICENSE.md` file (if available) for details.