# 31-Day Reset: Android app

The Android wrapper (Trusted Web Activity) for 31-Day Reset, plus native features:
reminders that ring on time (`Reminders`, `AlarmReceiver`), the home screen widget (`TodayWidget`),
the step counter (`Steps`), app shortcuts (`res/xml/app_shortcuts.xml`) and the bridge that
receives the plan from the web app (`SyncActivity`, `reset31://sync`).

**Do not run `bubblewrap update`**: it would overwrite the custom Java code. Change version numbers in
`app/build.gradle` and build with Gradle, then sign with the upload key (kept privately, never in this repo):

    ./gradlew assembleRelease bundleRelease

(c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved.
