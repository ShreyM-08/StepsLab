
package com.example.steplab

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {

    private lateinit var status: TextView

    private val permissions =
        setOf(HealthPermission.getReadPermission(StepsRecord::class))

    private val permissionLauncher =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) { granted ->
            if (granted.containsAll(permissions)) {
                readSteps()
            } else {
                status.text = "Step-reading permission was not granted."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
            gravity = Gravity.TOP
        }

        val title = TextView(this).apply {
            text = "StepLab"
            textSize = 28f
        }

        val subtitle = TextView(this).apply {
            text = "Health Connect step-record inspector"
            textSize = 16f
            setPadding(0, 12, 0, 28)
        }

        status = TextView(this).apply {
            textSize = 15f
            text = "Ready to check Health Connect."
            setPadding(0, 12, 0, 28)
        }

        val button = Button(this).apply {
            text = "Check recent step records"
            setOnClickListener { checkHealthConnect() }
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(button)
        layout.addView(status)

        setContentView(layout)
    }

    private fun checkHealthConnect() {
        val sdkStatus = HealthConnectClient.getSdkStatus(this)
        status.text = "Button tapped! Checking Health Connect..."

        if (sdkStatus != HealthConnectClient.SDK_AVAILABLE) {
            status.text =
                "Health Connect is unavailable or needs an update. SDK status: $sdkStatus"
            return
        }

        val client = HealthConnectClient.getOrCreate(this)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val granted = client.permissionController.getGrantedPermissions()

                if (!granted.containsAll(permissions)) {
                    permissionLauncher.launch(permissions)
                } else {
                    readSteps()
                }
            } catch (e: Exception) {
                status.text = "Could not check permissions: ${e.message}"
            }
        }
    }

    private fun readSteps() {
        val client = HealthConnectClient.getOrCreate(this)
        status.text = "Reading the last 7 days…"

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val end = Instant.now()
                val start = end.minus(7, ChronoUnit.DAYS)

                val response = withContext(Dispatchers.IO) {
                    client.readRecords(
                        ReadRecordsRequest(
                            recordType = StepsRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(start, end)
                        )
                    )
                }

                val total = response.records.sumOf { it.count }

                val details = response.records
                    .sortedByDescending { it.startTime }
                    .take(12)
                    .joinToString(separator = "\n") {
                        val date = it.startTime
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        val source = it.metadata.dataOrigin.packageName

                        "• $date — ${it.count} steps\n  Source: $source"
                    }

                status.text =
                    "Records found: ${response.records.size}\n" +
                    "Total in returned records: $total\n\n" +
                    if (details.isBlank()) {
                        "No step records found in the last 7 days."
                    } else {
                        details
                    }

            } catch (e: Exception) {
                status.text = "Read failed: ${e.message}"
            }
        }
    }
}
