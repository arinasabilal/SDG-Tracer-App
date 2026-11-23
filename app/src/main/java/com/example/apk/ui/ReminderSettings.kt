package com.example.apk.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.apk.notif.ReminderPrefs
import com.example.apk.notif.ReminderScheduler
import com.example.apk.notif.ReminderTimes
import com.example.apk.ui.theme.LuPrimary
import com.example.apk.ui.theme.LuTextDark
import com.example.apk.ui.theme.LuTextGray

// Mengembalikan fungsi yang meminta izin notifikasi (hanya Android 13 ke atas, dan hanya kalau belum diberi).
@Composable
fun rememberNotificationPermissionRequester(): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    return remember(launcher) {
        val request: () -> Unit = {
            if (Build.VERSION.SDK_INT >= 33) {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        request
    }
}

// Baris tambahan di bawah saklar Smart Reminder: jam berangkat + ajakan mengizinkan alarm tepat waktu.
@Composable
fun ReminderExtras(enabled: Boolean) {
    val context = LocalContext.current
    var depart by remember { mutableIntStateOf(ReminderPrefs.depart(context)) }
    var showDialog by remember { mutableStateOf(false) }
    val canExact = ReminderScheduler.canExact(context)

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Jam berangkat", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LuTextDark)
                Text(
                    "Pengingat cek listrik sebelum keluar kos/rumah",
                    fontSize = 12.sp,
                    color = LuTextGray
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(ReminderTimes.fmt(depart), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LuPrimary)
        }

        if (enabled && !canExact && Build.VERSION.SDK_INT >= 31) {
            Text(
                "Izinkan alarm tepat waktu supaya pengingat tidak meleset",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = LuPrimary,
                modifier = Modifier
                    .clickable {
                        val i = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        try {
                            context.startActivity(i)
                        } catch (_: Exception) {
                            // Perangkat tanpa layar pengaturan ini: abaikan.
                        }
                    }
                    .padding(bottom = 6.dp)
            )
        }
    }

    if (showDialog) {
        TimeStepperDialog(
            title = "Jam berangkat",
            initial = depart,
            onDismiss = { showDialog = false },
            onSave = {
                depart = it
                ReminderPrefs.setDepart(context, it)
                ReminderScheduler.rescheduleAll(context)
                showDialog = false
            }
        )
    }
}

@Composable
private fun TimeStepperDialog(
    title: String,
    initial: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initial / 60) }
    var minute by remember { mutableIntStateOf(initial % 60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = LuTextDark) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Stepper(
                    label = "Jam",
                    value = "%02d".format(hour),
                    onMinus = { hour = (hour + 23) % 24 },
                    onPlus = { hour = (hour + 1) % 24 }
                )
                Stepper(
                    label = "Menit",
                    value = "%02d".format(minute),
                    onMinus = { minute = (minute + 55) % 60 },
                    onPlus = { minute = (minute + 5) % 60 }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(hour * 60 + minute) }) {
                Text("Simpan", fontWeight = FontWeight.Bold, color = LuPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = LuTextGray) }
        }
    )
}

@Composable
private fun Stepper(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = LuTextGray)
        TextButton(onClick = onPlus) { Text("+", fontSize = 22.sp, color = LuPrimary) }
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LuTextDark)
        TextButton(onClick = onMinus) { Text("−", fontSize = 22.sp, color = LuPrimary) }
    }
}
