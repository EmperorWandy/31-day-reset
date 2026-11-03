/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Locale;

/** Native reminders: they ring on time even when the app is closed, and come back after a restart. */
final class Reminders {
    private Reminders() { }

    static final String CH = "reminders";
    static final String ACTION_FIRE = "io.github.emperorwandy.reset31.FIRE";
    static final String ACTION_DONE = "io.github.emperorwandy.reset31.DONE";
    static final String ACTION_TICK = "io.github.emperorwandy.reset31.TICK";
    // kinds of alarm
    static final int PRE = 1, WORK = 2, EVE = 3, MIDNIGHT = 4;
    static final String APP_URL = "https://emperorwandy.github.io/31-day-reset/";

    static int mins(String hm) {
        try { String[] p = hm.split(":"); return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]); }
        catch (Exception e) { return 17 * 60; }
    }

    static String fmt12(int m) {
        m = ((m % 1440) + 1440) % 1440;
        int h = m / 60, mm = m % 60;
        String ap = h < 12 ? "AM" : "PM";
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return String.format(Locale.UK, "%d:%02d %s", h12, mm, ap);
    }

    /** Next time (today or tomorrow) at minute-of-day m. */
    static long next(int m) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.MINUTE, m);
        if (c.getTimeInMillis() <= System.currentTimeMillis() + 5000) c.add(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }

    static PendingIntent firePI(Context c, int kind) {
        Intent i = new Intent(c, AlarmReceiver.class).setAction(ACTION_FIRE).putExtra("kind", kind);
        return PendingIntent.getBroadcast(c, 100 + kind, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void set(Context c, int kind, long at) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = firePI(c, kind);
        try {
            if (kind != MIDNIGHT && (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms())) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            }
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        }
    }

    /** Sets (or clears) every alarm from the current plan. Safe to call any time. */
    static void scheduleAll(Context ctx) {
        Context c = ctx.getApplicationContext();
        Store s = new Store(c);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        set(c, MIDNIGHT, next(5)); // 00:05: new day for the widget and the step count
        // a light, battery-friendly refresh every few hours for the widget and steps
        Intent r = new Intent(c, AlarmReceiver.class).setAction(ACTION_FIRE).putExtra("kind", 0);
        PendingIntent rp = PendingIntent.getBroadcast(c, 199, r, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR * 3, rp);
        if (!s.hasPlan() || !s.remindersOn()) {
            am.cancel(firePI(c, PRE)); am.cancel(firePI(c, WORK)); am.cancel(firePI(c, EVE));
            return;
        }
        int w = mins(s.workoutTime()), e = mins(s.eveningTime());
        set(c, PRE, next(w - 30 < 0 ? w - 30 + 1440 : w - 30));
        set(c, WORK, next(w));
        set(c, EVE, next(e));
    }

    static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm == null || nm.getNotificationChannel(CH) != null) return;
        NotificationChannel ch = new NotificationChannel(CH, "Daily reminders", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Your workout and evening habit reminders");
        nm.createNotificationChannel(ch);
    }

    static boolean canNotify(Context c) {
        return Build.VERSION.SDK_INT < 33
                || c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    static PendingIntent openApp(Context c, String go, int req) {
        Intent i = new Intent(c, LauncherActivity.class)
                .setAction(Intent.ACTION_VIEW)
                .setData(Uri.parse(APP_URL + "?go=" + go))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(c, req, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static PendingIntent donePI(Context c, String action, int n, String k, int req) {
        Intent i = new Intent(c, AlarmReceiver.class).setAction(action).putExtra("n", n).putExtra("k", k);
        return PendingIntent.getBroadcast(c, req, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** Shows the reminder for an alarm that has just rung, if it is still needed. */
    static void fire(Context c, int kind) {
        Store s = new Store(c);
        if (!s.hasPlan() || !s.remindersOn() || !canNotify(c)) return;
        int n = s.dayToday();
        if (n < 1 || n > 31) return;
        String title, body, k;
        int w = mins(s.workoutTime());
        if (kind == PRE) {
            if (s.isDone(n, "w")) return;
            k = "w"; title = "Workout in 30 minutes";
            body = "Day " + n + ": " + s.title(n, 0) + " at " + fmt12(w) + ". Time to get ready.";
        } else if (kind == WORK) {
            if (s.isDone(n, "w")) return;
            k = "w"; title = "Workout time";
            body = "Day " + n + ": " + s.title(n, 0) + ". Tap for today's instructions.";
        } else if (kind == EVE) {
            if (s.isDone(n, "e")) return;
            k = "e"; title = "Evening habit";
            body = "Day " + n + ": " + s.title(n, 1) + ". Done it? Tap Mark done.";
        } else return;
        ensureChannel(c);
        int id = 1000 + n * 10 + (k.equals("w") ? 1 : 2);
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CH)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(openApp(c, k.equals("w") ? "workout" : "today", id))
                .addAction(0, k.equals("w") ? "I did it" : "Mark done", donePI(c, ACTION_DONE, n, k, id));
        if (k.equals("w")) b.addAction(0, "Start workout", openApp(c, "workout", id + 5000));
        try {
            ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id, b.build());
        } catch (SecurityException ignored) { }
    }

    static void cancelFor(Context c, int n, String k) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(1000 + n * 10 + (k.equals("w") ? 1 : 2));
    }
}
