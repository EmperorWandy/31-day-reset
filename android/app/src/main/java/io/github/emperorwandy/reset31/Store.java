/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/** Small on-device store shared by reminders, the widget and the step counter. */
final class Store {
    private static final String P = "reset31";
    private final SharedPreferences sp;

    Store(Context c) { sp = c.getApplicationContext().getSharedPreferences(P, Context.MODE_PRIVATE); }

    static String ymd(Date d) { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d); }
    static String today() { return ymd(new Date()); }

    // ---------- plan settings sent by the web app ----------
    JSONObject cfg() {
        try { return new JSONObject(sp.getString("cfg", "{}")); } catch (Exception e) { return new JSONObject(); }
    }
    void saveCfg(JSONObject o) { sp.edit().putString("cfg", o.toString()).apply(); }
    boolean hasPlan() { return cfg().optString("s", "").length() == 10; }

    /** Day number (1 to 31) for a calendar day, or 0 / 32+ outside the plan. */
    int dayFor(Calendar when) {
        String s = cfg().optString("s", "");
        if (s.length() != 10) return 0;
        try {
            Calendar st = Calendar.getInstance();
            st.clear();
            st.set(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(5, 7)) - 1, Integer.parseInt(s.substring(8, 10)));
            Calendar d = Calendar.getInstance();
            d.clear();
            d.set(when.get(Calendar.YEAR), when.get(Calendar.MONTH), when.get(Calendar.DAY_OF_MONTH));
            long diff = d.getTimeInMillis() - st.getTimeInMillis();
            return (int) Math.floor(diff / 86400000.0 + 0.5) + 1;
        } catch (Exception e) { return 0; }
    }
    int dayToday() { return dayFor(Calendar.getInstance()); }

    String title(int n, int k) {
        JSONArray t = cfg().optJSONArray("t");
        if (t == null || n < 1 || n > t.length()) return k == 0 ? "Today's workout" : "Evening habit";
        JSONArray p = t.optJSONArray(n - 1);
        return p == null ? "" : p.optString(k, "");
    }
    boolean remindersOn() { return cfg().optInt("r", 0) == 1; }
    String workoutTime() { return cfg().optString("wt", "17:00"); }
    String eveningTime() { return cfg().optString("et", "21:00"); }
    String goalName() { return cfg().optString("g", ""); }

    // ---------- ticks: the web app is the main record; ticks made natively wait here ----------
    private Set<String> set(String key) { return new HashSet<>(sp.getStringSet(key, new HashSet<String>())); }

    boolean isDone(int n, String k) {
        String id = n + k;
        return set("webDone").contains(id) || set("pending").contains(id);
    }
    /** Tick from a notification or the widget. */
    void markDone(int n, String k) {
        Set<String> p = set("pending");
        p.add(n + k);
        sp.edit().putStringSet("pending", p).apply();
    }
    /** Full list from the web app, e.g. "1w,1e,2w". Pending ticks it now knows about are cleared. */
    void setWebDone(String list) {
        Set<String> w = new HashSet<>();
        for (String x : list.split(",")) if (x.trim().length() > 0) w.add(x.trim());
        Set<String> p = set("pending");
        p.removeAll(w);
        sp.edit().putStringSet("webDone", w).putStringSet("pending", p).apply();
    }
    String pendingList() {
        StringBuilder b = new StringBuilder();
        for (String x : set("pending")) { if (b.length() > 0) b.append(','); b.append(x); }
        return b.toString();
    }

    // ---------- steps ----------
    JSONObject steps() {
        try { return new JSONObject(sp.getString("steps", "{}")); } catch (Exception e) { return new JSONObject(); }
    }
    int stepsOn(String day) { return steps().optInt(day, -1); }

    /** Records a reading of the phone's step counter (total since the phone was switched on). */
    void addStepReading(float counter) {
        long c = (long) counter;
        long last = sp.getLong("lastC", -1);
        String lastDay = sp.getString("lastDay", "");
        String now = today();
        JSONObject st = steps();
        try {
            if (last < 0) {
                if (!st.has(now)) st.put(now, 0);
            } else {
                long delta = c - last;
                if (delta < 0) delta = c; // phone restarted, counter began again from zero
                String target = now;
                if (!lastDay.equals(now) && lastDay.length() == 10) {
                    // steps since the last reading: give them to yesterday if it is still very early
                    if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 1) target = lastDay;
                    else if (!st.has(now)) st.put(now, 0);
                }
                st.put(target, st.optInt(target, 0) + (int) delta);
            }
            // keep the last 40 days only
            Calendar cut = Calendar.getInstance();
            cut.add(Calendar.DAY_OF_MONTH, -40);
            String cutS = ymd(cut.getTime());
            Iterator<String> it = st.keys();
            Set<String> old = new HashSet<>();
            while (it.hasNext()) { String k = it.next(); if (k.compareTo(cutS) < 0) old.add(k); }
            for (String k : old) st.remove(k);
        } catch (Exception ignored) { }
        sp.edit().putLong("lastC", c).putString("lastDay", now).putString("steps", st.toString()).apply();
    }
    /** Last 31 days of steps as "2026-11-03:8200;2026-11-02:10400". */
    String stepsParam() {
        JSONObject st = steps();
        StringBuilder b = new StringBuilder();
        Iterator<String> it = st.keys();
        while (it.hasNext()) {
            String k = it.next();
            if (b.length() > 0) b.append(';');
            b.append(k).append(':').append(st.optInt(k, 0));
        }
        return b.toString();
    }

    boolean askedPerms() { return sp.getBoolean("asked", false); }
    void setAskedPerms() { sp.edit().putBoolean("asked", true).apply(); }
}
