# StepLab

StepLab contains:
- `web/`: browser-based local step-record simulator. Data stays in browser local storage and is not sent to Health Connect.
- `android/`: Android 15-compatible, read-only Health Connect inspector. It requests permission to read step records and displays the records Health Connect actually contains.

This project intentionally does not write fabricated steps into Health Connect or attempt to make synthetic activity appear as genuine exercise in other apps.

## Web app
Open `web/index.html` in a browser, or publish the `web/` folder with GitHub Pages. It supports sample entries, date filtering, and clearing local demo data.

## Build the Android APK online (no Android Studio required)
1. Create a GitHub repository and upload the contents of this ZIP.
2. Push to the `main` branch.
3. Open **Actions** in GitHub. The workflow **Build StepLab APK** will build a debug APK.
4. Download `app-debug.apk` from the workflow's Artifacts section.
5. Install it on your Android 15 phone. You may need to allow installation from your browser/files app.

The workflow builds the Android project in the `android/` directory.

## Android app
The app uses Health Connect's official SDK to request `READ_STEPS` and inspect recent step records. It does not request write access.
