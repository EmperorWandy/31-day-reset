/* 31-Day Reset. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
package io.github.emperorwandy.reset31

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Length
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId

/**
 * Health Connect: adds each finished guided workout (and its GPS distance) to Health Connect,
 * so it shows in Samsung Health, Google Fit, Fitbit and other apps the person allows.
 * The app only writes. It never reads anything from Health Connect.
 *
 * The web app sends its finished workouts in the "hc" list of the plan it passes across
 * (see SyncActivity). Each entry is [id, start ms, end ms, type, title, km].
 */
object HealthSync {
    private const val PREFS = "reset31"
    private const val PROVIDER = "com.google.android.apps.healthdata"
    private const val MIN_SDK = 28 // Health Connect needs Android 9 or newer
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun perms(): Set<String> = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class)
    )

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun sdk(c: Context): Int =
        if (Build.VERSION.SDK_INT < MIN_SDK) HealthConnectClient.SDK_UNAVAILABLE
        else HealthConnectClient.getSdkStatus(c.applicationContext, PROVIDER)

    /**
     * For the web app's Phone check: "x" not possible on this phone, "u" Health Connect needs
     * installing or updating, "0" ready but not connected yet, "1" connected.
     */
    @JvmStatic
    fun status(c: Context): String = try {
        when (sdk(c)) {
            HealthConnectClient.SDK_AVAILABLE -> if (prefs(c).getBoolean("hcOk", false)) "1" else "0"
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "u"
            else -> "x"
        }
    } catch (e: Exception) { "x" }

    @JvmStatic
    fun available(c: Context): Boolean = try { sdk(c) == HealthConnectClient.SDK_AVAILABLE } catch (e: Exception) { false }

    /** The system screen where the person allows 31-Day Reset to add workouts. */
    @JvmStatic
    fun permissionIntent(c: Context): Intent =
        PermissionController.createRequestPermissionResultContract(PROVIDER).createIntent(c, perms())

    /** Reads the answer from the permission screen, remembers it, and adds any new workouts. */
    @JvmStatic
    fun onPermissionResult(c: Context, resultCode: Int, data: Intent?) {
        try {
            val granted = PermissionController.createRequestPermissionResultContract(PROVIDER).parseResult(resultCode, data)
            val ok = granted.contains(HealthPermission.getWritePermission(ExerciseSessionRecord::class))
            if (ok) prefs(c).edit().putBoolean("hcOk", true).apply()
        } catch (e: Exception) { }
        refreshAndWrite(c)
    }

    /** Opens Health Connect in the Play Store so the person can install or update it. */
    @JvmStatic
    fun installIntent(): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PROVIDER&url=healthconnect%3A%2F%2Fonboarding"))
            .setPackage("com.android.vending")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Checks whether the person has allowed access, remembers it, then adds any new workouts. */
    @JvmStatic
    fun refreshAndWrite(c: Context) {
        val app = c.applicationContext
        if (!available(app)) {
            prefs(app).edit().putBoolean("hcOk", false).apply()
            return
        }
        scope.launch {
            try {
                val client = HealthConnectClient.getOrCreate(app, PROVIDER)
                val granted = client.permissionController.getGrantedPermissions()
                val ok = granted.contains(HealthPermission.getWritePermission(ExerciseSessionRecord::class))
                prefs(app).edit().putBoolean("hcOk", ok).apply()
                if (ok) write(app, client, granted.contains(HealthPermission.getWritePermission(DistanceRecord::class)))
            } catch (e: Exception) {
                // Health Connect can be busy or missing; we simply try again at the next sync
            }
        }
    }

    private suspend fun write(c: Context, client: HealthConnectClient, distanceOk: Boolean) {
        val p = prefs(c)
        val cfg = try { JSONObject(p.getString("cfg", "{}") ?: "{}") } catch (e: Exception) { JSONObject() }
        val list = cfg.optJSONArray("hc") ?: return
        val done = HashSet(p.getStringSet("hcDone", emptySet()) ?: emptySet())
        val records = ArrayList<Record>()
        val added = ArrayList<String>()
        val zone = ZoneId.systemDefault().rules
        val device = Device(type = Device.TYPE_PHONE, manufacturer = Build.MANUFACTURER, model = Build.MODEL)
        for (i in 0 until list.length()) {
            val a = list.optJSONArray(i) ?: continue
            val id = a.optString(0, "")
            val t0 = a.optLong(1, 0L)
            val t1 = a.optLong(2, 0L)
            if (id.isEmpty() || t0 <= 0L || t1 <= t0) continue
            val key = "$id:$t0"
            if (done.contains(key)) continue
            val start = Instant.ofEpochMilli(t0)
            val end = Instant.ofEpochMilli(t1)
            // clientRecordId means a workout sent twice replaces itself instead of appearing twice
            records.add(
                ExerciseSessionRecord(
                    startTime = start,
                    startZoneOffset = zone.getOffset(start),
                    endTime = end,
                    endZoneOffset = zone.getOffset(end),
                    metadata = Metadata.activelyRecorded(device = device, clientRecordId = "reset31-$id", clientRecordVersion = t0),
                    exerciseType = typeFor(a.optString(3, "")),
                    title = a.optString(4, "").take(60).ifEmpty { null }
                )
            )
            val km = a.optDouble(5, 0.0)
            if (distanceOk && km > 0.0 && km < 200.0) {
                records.add(
                    DistanceRecord(
                        startTime = start,
                        startZoneOffset = zone.getOffset(start),
                        endTime = end,
                        endZoneOffset = zone.getOffset(end),
                        distance = Length.kilometers(km),
                        metadata = Metadata.activelyRecorded(device = device, clientRecordId = "reset31-$id-km", clientRecordVersion = t0)
                    )
                )
            }
            added.add(key)
        }
        if (records.isEmpty()) return
        client.insertRecords(records)
        done.addAll(added)
        // keep the memory small: 31 days times a few rounds is plenty
        val keep = if (done.size > 300) HashSet(done.toList().takeLast(300)) else done
        p.edit().putStringSet("hcDone", keep).apply()
    }

    private fun typeFor(t: String): Int = when (t) {
        "str" -> ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING
        "walk" -> ExerciseSessionRecord.EXERCISE_TYPE_WALKING
        "run" -> ExerciseSessionRecord.EXERCISE_TYPE_RUNNING
        "dance" -> ExerciseSessionRecord.EXERCISE_TYPE_DANCING
        "hiit" -> ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING
        "cal" -> ExerciseSessionRecord.EXERCISE_TYPE_CALISTHENICS
        "stretch" -> ExerciseSessionRecord.EXERCISE_TYPE_STRETCHING
        else -> ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT
    }
}
