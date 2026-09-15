package com.mayowa.studytracker.data.tracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Only restart if the user previously granted usage access —
            // check before calling start(), otherwise this throws.
            if (UsagePermissionHelper.hasUsageAccess(context)) {
                TrackingService.start(context)
            }
        }
    }
}
