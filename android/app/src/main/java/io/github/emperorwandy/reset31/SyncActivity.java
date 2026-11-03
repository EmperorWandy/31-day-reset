/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Invisible screen the web app opens (reset31://sync?d=...) to pass its plan and ticks to Android.
 * It saves them, sets the reminders, refreshes the widget and closes straight away.
 * With p=1 it first asks for the notification and step-counting permissions.
 */
public class SyncActivity extends Activity {
    private static final int REQ = 31;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        boolean ask = false;
        try {
            Uri u = getIntent() == null ? null : getIntent().getData();
            if (u != null && "sync".equals(u.getHost())) {
                String d = u.getQueryParameter("d");
                if (d != null) {
                    JSONObject o = new JSONObject(d);
                    Store s = new Store(this);
                    s.setWebDone(o.optString("d", ""));
                    o.remove("d");
                    s.saveCfg(o);
                }
                ask = "1".equals(u.getQueryParameter("p"));
            }
        } catch (Exception ignored) { }

        Store s = new Store(this);
        List<String> need = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            need.add(Manifest.permission.POST_NOTIFICATIONS);
        if (Build.VERSION.SDK_INT >= 29 && Steps.available(this) && checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED)
            need.add(Manifest.permission.ACTIVITY_RECOGNITION);
        // ask when the person taps "Turn on reminders", or once on the first sync
        if (ask && !need.isEmpty() && Build.VERSION.SDK_INT >= 23) {
            s.setAskedPerms();
            requestPermissions(need.toArray(new String[0]), REQ);
            return;
        }
        done();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        done();
    }

    private void done() {
        final android.content.Context c = getApplicationContext();
        Reminders.scheduleAll(c);
        TodayWidget.updateAll(c);
        Steps.read(c, () -> TodayWidget.updateAll(c));
        finish();
        overridePendingTransition(0, 0);
    }
}
