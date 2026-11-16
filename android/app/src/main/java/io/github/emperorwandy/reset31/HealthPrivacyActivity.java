/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Health Connect opens this when someone taps "privacy policy" on its permission screen.
 * It shows the Health Connect part of the app's privacy policy and closes.
 */
public class HealthPrivacyActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://emperorwandy.github.io/31-day-reset/privacy.html#health"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception ignored) { }
        finish();
    }
}
