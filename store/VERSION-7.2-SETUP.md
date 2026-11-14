# Version 7.2: what you need to do

Prepared 14 November 2026 for Akinwande Akinkunmi Adeniyi.

Version 7.2 adds a light look, a food and water log, Health Connect and Google Drive backup.
The light look and the food and water log work as soon as the website updates. The other two
need a few steps from you, because they use your Google accounts.

## Part 1: Google Drive backup (about 20 minutes)

The backup is hidden until you create a free "OAuth client ID" in Google Cloud. This ID is a
public label for your app, not a password, so it is safe to put in the website code.

1. Go to **console.cloud.google.com** and sign in with your Gmail.
2. At the top, click the project list, then **New project**. Name it `31-Day Reset` and click **Create**.
3. In the search bar, type **Google Drive API**, open it and click **Enable**.
4. Open the menu, choose **Google Auth Platform** (it may be called **OAuth consent screen**), then **Get started**:
   - App name: `31-Day Reset`
   - User support email: your Gmail
   - Audience: **External**
   - Contact email: your Gmail
5. Under **Branding**, add:
   - App home page: `https://emperorwandy.github.io/31-day-reset/`
   - Privacy policy: `https://emperorwandy.github.io/31-day-reset/privacy.html`
   - Authorised domain: `emperorwandy.github.io`
   - App logo: `store/app-icon-512.png` (optional, but it makes the sign-in screen look trustworthy)
6. Under **Data access**, click **Add or remove scopes**, tick `.../auth/drive.file` ("See, edit, create and delete only the specific Google Drive files you use with this app") and save.
7. Under **Clients**, click **Create client**:
   - Application type: **Web application**
   - Name: `31-Day Reset website`
   - Authorised JavaScript origins: `https://emperorwandy.github.io`
   - Leave "Authorised redirect URIs" empty.
8. Copy the **Client ID** (it ends in `.apps.googleusercontent.com`) and send it to me, or paste it
   between the quotes on the `googleClientId: ""` line near the top of `index.html`.
9. Under **Audience**, click **Publish app** so anyone can sign in. While it says "Testing", only
   people you list can sign in, and their sign-in stops working after 7 days. `drive.file` is
   Google's lowest-risk Drive permission, so publishing normally needs only a quick brand check.
   Google may ask you to confirm you own `emperorwandy.github.io` through Google Search Console.
   If so, send me the verification file and I will add it to the website.

To test it: open the app, go to **Backup**, tap **Connect Google Drive** and sign in. You should see
"Last backup: just now", and a file called `31-Day Reset backup.json` in your Google Drive.

## Part 2: Health Connect on Google Play

Health Connect permissions need a short declaration in the Play Console before Google will accept
version 7.2.

1. In the Play Console, open the app, then **Policy and programmes → App content**.
2. Find **Health apps** (or **Health Connect permissions**) and click **Start**.
3. Say the app uses Health Connect to **write** these two data types, and give this reason for both:
   > 31-Day Reset adds the user's completed guided workouts (time, type and GPS distance) to
   > Health Connect when they choose "Connect to Health Connect", so the workouts appear in the
   > other health apps they use. The app does not read any Health Connect data.
4. Privacy policy: the policy at `privacy.html` already has a Health Connect section.
5. Update the **Data safety** form:
   - Health Connect: data is written to the phone and not sent off it, so it does not count as collected.
   - Google Drive backup: this is optional and goes to the user's own Google account. Google treats
     it as data that leaves the phone. Mark "Health and fitness" and "App activity" as **collected,
     optional, for app functionality (backup)**, **encrypted in transit**, and **deletable by the user**.
   Read each question carefully before answering. Google's wording changes from time to time.

## Part 3: Build and upload 7.2

The GitHub "Build Android app" action makes a test APK automatically. For Google Play:

1. On your computer, from the `android` folder, run `./gradlew bundleRelease` and sign the
   file with your upload key, as before.
2. Upload `app-release.aab` to your closed testing track.
3. When you are happy, I will put the new APK on the download page (`get/`), which still offers 7.1.

## How to test Health Connect on your phone

1. Install the 7.2 test APK.
2. Open the app, go to **Phone check** and tap **Connect to Health Connect**. Allow both items.
3. Finish any guided workout and tap **Finish**.
4. Open Health Connect (or Samsung Health or Google Fit). The workout should appear, with its
   distance if GPS was on.
