# 31-Day Reset: project notes for Claude

Owner: Akinwande Akinkunmi Adeniyi (goes by Akinkunmi). This is his first app. Explain technical choices in plain language and keep steps small.

## What the app is
A 31-day fitness and wellness app. Its purpose is to keep people on track, remind them on time (for example 30 minutes before their chosen workout time) and give step-by-step instructions for each day's activities.

## Structure
- Web app: `index.html` (single file), `sw.js` (offline), `manifest.webmanifest`, hosted on GitHub Pages at https://emperorwandy.github.io/31-day-reset/
- Voice coach audio: `audio/ezinne/` and `audio/abeo/` (Azure Nigerian neural voices, chosen at random)
- Download page for the APK: `get/`
- Android app: `android/` (Trusted Web Activity plus custom native code). Read `android/README.md` first.
- Play Store assets and launch guide: `store/`

## Fixed product decisions (do not change without asking)
- Each plan is fixed and not editable by the user. It gets harder over the weeks and covers the whole body, so people cannot pick only what they enjoy.
- Five goals, each with its own 31-day plan: General Fitness, Fat Loss, Healthy Weight Gain, Heart and Stamina, Gentle Start. The goal stays fixed within a plan.
- Coach voice: Nigerian-accent Azure voices Ezinne and Abeo, switching randomly.
- Target: publish on the Google Play Store as an Android app.
- Native Android features: on-time reminders, home screen widget, step counting, app-icon shortcuts.

## Android rules
- Never run `bubblewrap update`. It overwrites the custom Java code.
- Package name `io.github.emperorwandy.reset31` is permanent. Change version numbers in `android/app/build.gradle` (versionCode and versionName together).
- Build with Gradle: `./gradlew assembleRelease bundleRelease` from `android/`.

## Secrets and safety
- This repository is public. Never commit the upload keystore, keystore password, Azure keys or any API key.
- General fitness guidance only, not medical advice. Keep the safety check and this wording in the app.

## Working style
- Use British English in app text. Keep the interface friendly, colourful and simple, with one screen at a time for onboarding.
- Before committing, tell Akinkunmi what changed in plain words. Do not push to the main branch without his go-ahead.
