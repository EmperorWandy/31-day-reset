# 31-Day Reset: Google Play launch pack

Prepared 3 November 2026 for Akinwande Akinkunmi Adeniyi.

## What is in this pack

| File | What it is for |
|---|---|
| `31-day-reset-7.0.aab` | The file you upload to Google Play |
| `31-day-reset-7.0-test.apk` | Install this on your own Android phone to try the app before Play approves it |
| `reset31-upload.keystore` + `KEYSTORE-PASSWORD.txt` | Your upload key. **Keep both safe** (Google Drive and a flash drive). Every future update must be signed with this key. Never share them. |
| `app-icon-512.png` | Store icon |
| `feature-graphic-1024x500.png` | Store banner |
| `phone-1.png` to `phone-6.png` | Phone screenshots (1080 x 1920) |

App details fixed in the build:

- Package name: `io.github.emperorwandy.reset31` (permanent once uploaded)
- Version: 7.0 (version code 70). Next update: 7.1 = code 71, 8.0 = code 80.
- Target Android version: API 36. Works on Android 7.0 and newer.
- Permissions: notifications, alarms, physical activity (step counting), start at boot (to restore reminders), and location (only used when GPS distance is switched on).

## Step 1: Create your developer account (one time)

1. Go to play.google.com/console and sign in with your Gmail.
2. Choose **Personal** account (for yourself).
3. Pay the one-time US$25 fee. Many Nigerian naira cards fail here; a dollar card or virtual dollar card usually works.
4. Verify your identity with your NIN slip, international passport or driver's licence. Your name must match your card. This can take a few days.
5. Developer name to use: **Akinwande Akinkunmi Adeniyi**. This is what shows under the app on Play.

## Step 2: Create the app

1. **Create app** → App name: `31-Day Reset` → App → Free.
2. In **Test and release → Setup → App signing**, keep **Play App Signing** on (the default).
3. Upload `31-day-reset-7.0.aab` to a **Closed testing** track (not Internal; only closed testing counts).

## Step 3: Send me the Play signing fingerprint (important)

Google re-signs the app with its own key. After the first upload, open **Test and release → Setup → App signing**, copy the **SHA-256 certificate fingerprint** under *App signing key certificate*, and send it to me. I will add it to the website verification file. Without it, the app from Play will show a browser address bar at the top.

## Step 4: The 12-tester rule

New personal accounts must run a closed test with **at least 12 testers who stay opted in for 14 days in a row** before Google lets you publish to everyone.

1. In the closed test, add a list of testers' Gmail addresses (friends, family, colleagues, fellow MSc students). Aim for 15 or more in case some drop out.
2. Share the opt-in link Play gives you. Each tester taps **Become a tester**, then installs from Play.
3. Ask them to open the app a few times a week. Google now checks that testers actually use it.
4. After 14 days, go to **Dashboard → Apply for production**. Approval usually takes a few days.

## Step 5: Store listing text (copy and paste)

**App name** (30 max): 31-Day Reset

**Short description** (80 max):
Your daily fitness coach: one goal, 31 days, guided workouts and reminders.

**Full description:**

31-Day Reset is a daily fitness and wellness coach that tells you what to do, shows you how to do it and reminds you so you actually do it.

Choose one goal and follow a fixed 31-day plan that gets a little harder each week:
• General Fitness: all-round strength, cardio and mobility
• Fat Loss: brisk walking, aerobic dance and circuits
• Healthy Weight Gain: four strength days a week and sensible eating
• Heart and Stamina: steady cardio, intervals and a lower resting pulse
• Gentle Start: short walks, chair-based strength and balance

Every day has two tasks: a workout and an evening habit such as sleep, water, fibre or a drink swap.

What you get:
• Guided workouts with timers, moving demonstrations and a Nigerian coach voice
• Step-by-step instructions for every session, including warm-up and cool-down
• Reminders that ring on time, even when the app is closed
• A home screen widget with today's tasks and tick buttons
• All-day step counting with your phone's built-in sensor
• GPS distance for outdoor walks and runs
• Retests on Day 15 and Day 31 so you can see real progress
• Measurements, charts, streaks, badges and a certificate at the end
• Share milestones with friends on WhatsApp
• Works offline once opened

Private by design: no account, no adverts. Everything you enter stays on your phone.

A safety check at the start points anyone with a health risk to the Gentle Start plan and to their doctor. This app gives general fitness guidance and is not medical advice.

Created by Akinwande Akinkunmi Adeniyi, Exercise Physiology, University of Lagos.

**Category:** Health & Fitness
**Contact email:** akinkunmiakinwande@gmail.com
**Privacy policy:** https://emperorwandy.github.io/31-day-reset/privacy.html

## Step 6: App content forms (suggested answers)

- **Privacy policy:** the link above.
- **Ads:** No.
- **App access:** All functionality is available without special access.
- **Target audience:** 18 and over.
- **Content rating questionnaire:** Health & Fitness reference app; answer No to violence, sexual content, gambling and user-generated content.
- **Health apps declaration:** Fitness / activity tracking. Not a medical device.
- **Data safety:** "No data collected" and "No data shared". All data is processed and stored only on the user's device, including location, which is used on the device to measure distance and is never sent off it. Data is not encrypted in transit because none is sent. Users can delete data with Start over or by uninstalling.
- **Location permission:** used only in the foreground during a workout when the user turns on GPS distance. No background location.
- **Physical activity (steps):** read from the phone's step sensor; daily totals stay on the device.
- **Alarms:** if Play asks about exact alarms, explain they ring the workout and evening reminders at the times the user chose.

## Updating the app later

Most changes (new features, fixes) go live on the website and reach Play users automatically, with no new upload. A new Play upload is only needed when the Android part changes (reminders, widget, steps, icon, name or permissions). The Android source code is saved in your GitHub repository under `android/`. Bring me the keystore and password and I will rebuild it.
