/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import java.text.NumberFormat;
import java.util.Locale;

/** Home screen widget: today's day, both tasks with tick buttons, and today's steps. */
public class TodayWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
    }

    @Override
    public void onEnabled(Context c) {
        Reminders.scheduleAll(c);
        Steps.read(c, () -> updateAll(c));
    }

    static void updateAll(Context ctx) {
        Context c = ctx.getApplicationContext();
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, TodayWidget.class));
        if (ids == null || ids.length == 0) return;
        RemoteViews v = build(c);
        for (int id : ids) m.updateAppWidget(id, v);
    }

    static RemoteViews build(Context c) {
        Store s = new Store(c);
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_today);
        v.setOnClickPendingIntent(R.id.wHead, Reminders.openApp(c, "today", 7001));
        int n = s.dayToday();

        int st = s.stepsOn(Store.today());
        if (Steps.available(c) && Steps.allowed(c) && st >= 0) {
            v.setViewVisibility(R.id.wSteps, View.VISIBLE);
            v.setTextViewText(R.id.wSteps, NumberFormat.getIntegerInstance(Locale.UK).format(st) + " steps today");
        } else v.setViewVisibility(R.id.wSteps, View.GONE);

        if (!s.hasPlan() || n < 1 || n > 31) {
            v.setTextViewText(R.id.wDay, !s.hasPlan() ? "31-Day Reset" : (n < 1 ? "Starts soon" : "Plan complete!"));
            v.setTextViewText(R.id.wGoal, !s.hasPlan() ? "Open the app to set up your plan"
                    : (n < 1 ? "Day 1 is coming up. Get ready!" : "Open the app for your results"));
            v.setViewVisibility(R.id.wRowW, View.GONE);
            v.setViewVisibility(R.id.wRowE, View.GONE);
            return v;
        }
        v.setTextViewText(R.id.wDay, "Day " + n + " of 31");
        v.setTextViewText(R.id.wGoal, s.goalName());
        row(c, v, s, n, "w", R.id.wRowW, R.id.wW, R.id.wWs, s.title(n, 0), 7100);
        row(c, v, s, n, "e", R.id.wRowE, R.id.wE, R.id.wEs, s.title(n, 1), 7200);
        return v;
    }

    private static void row(Context c, RemoteViews v, Store s, int n, String k, int rowId, int textId, int stateId,
                            String title, int req) {
        v.setViewVisibility(rowId, View.VISIBLE);
        v.setTextViewText(textId, title);
        boolean done = s.isDone(n, k);
        v.setTextViewText(stateId, done ? "✓ Done" : "Tick");
        v.setInt(stateId, "setBackgroundResource", done ? R.drawable.widget_pill_done : R.drawable.widget_pill);
        // tapping the task opens the app; tapping the pill ticks it off
        v.setOnClickPendingIntent(rowId, Reminders.openApp(c, "today", req + n));
        if (done) v.setOnClickPendingIntent(stateId, Reminders.openApp(c, "today", req + 50 + n));
        else v.setOnClickPendingIntent(stateId, Reminders.donePI(c, Reminders.ACTION_TICK, n, k, req + 50 + n));
    }
}
