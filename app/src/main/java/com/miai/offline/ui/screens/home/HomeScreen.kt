package com.miai.offline.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.miai.offline.data.model.DeviceProfile
import com.miai.offline.device.DeviceAnalyzer
import com.miai.offline.ui.components.MiAiBrandMark
import com.miai.offline.ui.theme.*

@Composable
fun HomeScreen(onBrowseModels: () -> Unit) {
    val context = LocalContext.current
    var deviceProfile by remember { mutableStateOf<DeviceProfile?>(null) }

    LaunchedEffect(Unit) {
        deviceProfile = DeviceAnalyzer(context).analyze()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(20.dp)
    ) {
        MiAiBrandMark()

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Aapka offline AI assistant",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Text(
            text = "Urdu • English • Hindi • Arabic — bilkul offline",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        deviceProfile?.let { profile ->
            DeviceStatusCard(profile)
        } ?: run {
            CircularProgressIndicator(color = MiPink, modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBrowseModels,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MiPink)
        ) {
            Text("Models Browse Karein", color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Aage aane wala: AI Call Assistant, Image Generation, File Processing",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun DeviceStatusCard(profile: DeviceProfile) {
    val analyzer = DeviceAnalyzer(LocalContext.current)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Device Status", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            StatusRow("RAM", "${profile.availableRamMb} MB free / ${profile.totalRamMb} MB total")
            StatusRow("Internal Storage", analyzer.formatStorageSize(profile.availableInternalStorageMb) + " free")

            if (profile.hasSdCard) {
                StatusRow("SD Card", analyzer.formatStorageSize(profile.availableSdCardStorageMb ?: 0) + " free")
            } else {
                StatusRow("SD Card", "Nahi mila")
            }

            StatusRow("Processor", "${profile.cpuCoreCount} cores (${profile.cpuAbi})")

            Spacer(modifier = Modifier.height(10.dp))

            val tierLabel = when (profile.performanceTier()) {
                com.miai.offline.data.model.DeviceTier.HIGH -> "Strong device — bade models bhi chalenge"
                com.miai.offline.data.model.DeviceTier.MEDIUM -> "Mid-range device — medium size models best"
                com.miai.offline.data.model.DeviceTier.LOW -> "Light device — chote models recommend honge"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MiBrandGradient, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(tierLabel, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
    }
}
