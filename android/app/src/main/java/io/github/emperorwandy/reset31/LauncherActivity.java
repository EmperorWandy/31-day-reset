/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.emperorwandy.reset31;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;



public class LauncherActivity
        extends com.google.androidbrowserhelper.trusted.LauncherActivity {
    

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final android.content.Context app = getApplicationContext();
        Reminders.scheduleAll(app);
        Steps.read(app, () -> TodayWidget.updateAll(app));
        // Setting an orientation crashes the app due to the transparent background on Android 8.0
        // Oreo and below. We only set the orientation on Oreo and above. This only affects the
        // splash screen and Chrome will still respect the orientation.
        // See https://github.com/GoogleChromeLabs/bubblewrap/issues/496 for details.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @Override
    protected Uri getLaunchingUrl() {
        // Get the original launch Url.
        Uri uri = super.getLaunchingUrl();
        // Tell the web app it is running inside the Android app, and pass on
        // ticks made from notifications or the widget, plus the step count.
        try {
            Store s = new Store(this);
            Uri.Builder b = uri.buildUpon().appendQueryParameter("src", "apk");
            String nd = s.pendingList();
            if (nd.length() > 0) b.appendQueryParameter("nd", nd);
            String st = s.stepsParam();
            if (st.length() > 0) b.appendQueryParameter("st", st);
            b.appendQueryParameter("sp", Steps.available(this) ? (Steps.allowed(this) ? "1" : "0") : "x");
            b.appendQueryParameter("ls", String.valueOf(s.lastReadAt()));
            b.appendQueryParameter("av", String.valueOf(BuildConfig.VERSION_CODE));
            b.appendQueryParameter("np", Reminders.canNotify(this) ? "1" : "0");
            android.app.AlarmManager am = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            b.appendQueryParameter("ea", (Build.VERSION.SDK_INT < 31 || (am != null && am.canScheduleExactAlarms())) ? "1" : "0");
            String lp = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ? "2"
                    : (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ? "1" : "0");
            b.appendQueryParameter("lp", lp);
            return b.build();
        } catch (Exception e) {
            return uri;
        }
    }
}
