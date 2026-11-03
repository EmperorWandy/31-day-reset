/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/** Handles alarms, ticks from notifications and the widget, and phone restarts. */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final Context c = context.getApplicationContext();
        final String a = intent.getAction() == null ? "" : intent.getAction();

        if (a.equals(Reminders.ACTION_DONE) || a.equals(Reminders.ACTION_TICK)) {
            int n = intent.getIntExtra("n", 0);
            String k = intent.getStringExtra("k");
            if (n >= 1 && n <= 31 && ("w".equals(k) || "e".equals(k))) {
                new Store(c).markDone(n, k);
                Reminders.cancelFor(c, n, k);
                TodayWidget.updateAll(c);
                Toast.makeText(c, "Ticked off. Well done!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        final PendingResult pr = goAsync();
        final int kind = intent.getIntExtra("kind", 0);
        Steps.read(c, new Runnable() {
            @Override public void run() {
                try {
                    if (a.equals(Reminders.ACTION_FIRE)) {
                        if (kind >= Reminders.PRE && kind <= Reminders.EVE) Reminders.fire(c, kind);
                        if (kind != 0) Reminders.scheduleAll(c); // set the next one
                    } else {
                        // restart, app update, or clock / time zone change
                        Reminders.scheduleAll(c);
                    }
                    TodayWidget.updateAll(c);
                } finally {
                    pr.finish();
                }
            }
        });
    }
}
