/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.trusted.TrustedWebActivityCallbackRemote;

import com.google.androidbrowserhelper.trusted.ExtraCommandHandler;

/**
 * Answers Chrome's "may this app use location?" question straight away.
 *
 * Inside the app, Chrome asks the app for location on the website's behalf. The location library
 * answers by opening a small permission screen every time, even when permission is already
 * granted. It opens that screen from the background, which Android 10 and newer block for apps
 * like this one, so Chrome never gets an answer and the page reports "location blocked".
 *
 * This handler runs first and replies with the app's real permission, without opening a screen.
 * Location updates themselves are still provided by the library (LocationDelegationExtraCommandHandler).
 * If permission is missing, the app asks for it from a visible screen instead (Phone check,
 * "Turn on reminders, steps and GPS", or "Allow location" in the GPS test).
 */
final class LocationPermissionHandler implements ExtraCommandHandler {
    // Names used by Chrome and the location library
    private static final String CHECK = "checkAndroidLocationPermission";
    private static final String RESULT = "locationPermissionResult";

    @NonNull
    @Override
    public Bundle handleExtraCommand(Context context, String commandName, Bundle args,
                                     @Nullable TrustedWebActivityCallbackRemote callback) {
        Bundle result = new Bundle();
        result.putBoolean(EXTRA_COMMAND_SUCCESS, false);
        if (!CHECK.equals(commandName) || callback == null) return result; // let the library handle it

        final boolean granted =
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        final Bundle answer = new Bundle();
        answer.putBoolean(RESULT, granted);
        // Reply the same way the library does: as a callback, from the main thread
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                callback.runExtraCallback(CHECK, answer);
            } catch (RemoteException ignored) {
                // Chrome went away; it will ask again next time
            }
        });
        result.putBoolean(EXTRA_COMMAND_SUCCESS, true);
        return result;
    }
}
