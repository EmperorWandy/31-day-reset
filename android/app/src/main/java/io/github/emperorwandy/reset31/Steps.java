/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

/** Reads the phone's built-in step counter once. It uses almost no battery. */
final class Steps {
    private Steps() { }

    static boolean available(Context c) {
        SensorManager sm = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        return sm != null && sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null;
    }

    static boolean allowed(Context c) {
        return Build.VERSION.SDK_INT < 29
                || c.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }

    /** Takes one reading, saves it and then runs done (always, even without a sensor). */
    static void read(Context ctx, final Runnable done) {
        final Context c = ctx.getApplicationContext();
        final SensorManager sm = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        final Sensor s = sm == null ? null : sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (s == null || !allowed(c)) { if (done != null) done.run(); return; }
        final Handler h = new Handler(Looper.getMainLooper());
        final boolean[] finished = {false};
        final SensorEventListener[] l = new SensorEventListener[1];
        final Runnable finish = new Runnable() {
            @Override public void run() {
                if (finished[0]) return;
                finished[0] = true;
                try { sm.unregisterListener(l[0]); } catch (Exception ignored) { }
                if (done != null) done.run();
            }
        };
        l[0] = new SensorEventListener() {
            @Override public void onSensorChanged(SensorEvent e) {
                if (finished[0]) return;
                new Store(c).addStepReading(e.values[0]);
                h.post(finish);
            }
            @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };
        try {
            sm.registerListener(l[0], s, SensorManager.SENSOR_DELAY_NORMAL, h);
        } catch (Exception e) { finish.run(); return; }
        h.postDelayed(finish, 4000);
    }
}
