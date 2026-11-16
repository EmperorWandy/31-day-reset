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

## Part 3: Build the signed 7.2 (no Google Play account needed)

GitHub can build the signed app for you, using your upload key stored as an encrypted secret.
Encrypted secrets cannot be read by anyone, including you, once saved, and they never appear in the
public code. You do this setup once; every later version is one button press.

### Step A: turn the key file into text (once)

GitHub secrets only take text, so the key file is converted to a long line of letters ("base64").
Do this on your own computer, in the folder that holds `reset31-upload.keystore`.
Do not use a website for this: never upload your key anywhere except GitHub secrets.

- **Windows:** open the folder, click the address bar, type `powershell` and press Enter, then paste:

      [Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\reset31-upload.keystore")) | Set-Clipboard

  The text is now copied, ready to paste in Step B.
- **Mac:** open Terminal in that folder and paste:

      base64 -i reset31-upload.keystore | pbcopy

### Step B: add four secrets to GitHub (once)

1. Open **github.com/EmperorWandy/31-day-reset**, then **Settings → Secrets and variables → Actions**.
2. Click **New repository secret** for each of these:

| Name | Value |
|---|---|
| `RESET31_KEYSTORE_B64` | Paste the text from Step A |
| `RESET31_KEYSTORE_PASSWORD` | The keystore password from `KEYSTORE-PASSWORD.txt` |
| `RESET31_KEY_PASSWORD` | The key password, if `KEYSTORE-PASSWORD.txt` lists a separate one (otherwise skip) |
| `RESET31_KEY_ALIAS` | Skip this: the alias is `upload` |

### Step C: build (every new version)

1. Open the **Actions** tab, choose **Build signed release** on the left, then **Run workflow**.
2. Wait about 3 minutes for the green tick.
3. Open the run and download **31-day-reset-signed**. It contains:
   - `31-day-reset-7.2.apk`: for your download page. Tell me when it is ready and I will put it on `get/`.
   - `31-day-reset-7.2.aab`: for Google Play, when you open your developer account.

The build checks that it was signed with your real upload key (the one your website trusts). If the
key or password is wrong, it stops with a clear message instead of making a file that cannot update 7.1.

## How to test Health Connect on your phone

1. Install the 7.2 test APK.
2. Open the app, go to **Phone check** and tap **Connect to Health Connect**. Allow both items.
3. Finish any guided workout and tap **Finish**.
4. Open Health Connect (or Samsung Health or Google Fit). The workout should appear, with its
   distance if GPS was on.
