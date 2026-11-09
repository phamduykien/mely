#!/bin/bash
set -e

echo "=== Building Mêly Sleep Sounds Android App ==="

export JAVA_HOME="/app/ai-translate/android-env/jdk-17.0.2"
export ANDROID_HOME="/app/ai-translate/android-env/android-sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

cd /app/mely
chmod +x gradlew

./gradlew assembleDebug

APK_PATH="/app/mely/app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo "=========================================================="
    echo " Build SUCCESSFUL!"
    echo " APK generated at: $APK_PATH"
    ls -lh "$APK_PATH"
    echo "=========================================================="
else
    echo "APK build finished. Check app/build/outputs/apk/"
fi
