/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Invisible screen that shows Health Connect's permission screen, waits for the answer,
 * adds any finished workouts, then reopens the app on the Phone check.
 */
public class HealthAskActivity extends Activity {
    private static final int REQ_HC = 32;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (b != null) return; // already waiting for the answer after a screen rotation
        try {
            startActivityForResult(HealthSync.permissionIntent(this), REQ_HC);
        } catch (Exception e) {
            back();
        }
    }

    @Override
    protected void onActivityResult(int code, int result, Intent data) {
        super.onActivityResult(code, result, data);
        if (code != REQ_HC) return;
        HealthSync.onPermissionResult(this, result, data);
        back();
    }

    private void back() {
        try {
            startActivity(new Intent(this, LauncherActivity.class)
                    .setAction(Intent.ACTION_VIEW)
                    .setData(Uri.parse(Reminders.APP_URL + "?go=check")));
        } catch (Exception ignored) { }
        finish();
        overridePendingTransition(0, 0);
    }
}
