package com.mayowa.studytracker.data.tracking

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings

/**
 * PACKAGE_USAGE_STATS is a "special access" permission — it can't be
 * requested via the normal runtime-permission dialog. The user has to
 * grant it manually from Settings, so we check via AppOpsManager and
 * deep-link them to the right screen if it's missing.
 */
object UsagePermissionHelper {

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageAccessIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    // Some OEMs (Xiaomi/MIUI, Huawei) also require an explicit battery-optimization
    // exemption or the service gets killed within minutes — surface this in
    // onboarding as a second, clearly-explained step, not bundled with the first ask.
    fun requestIgnoreBatteryOptimizationsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }
}
