package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NomadViewModel
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderWarm
import com.example.ui.theme.ImmersiveOrange
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceElevated
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun ZeroStateBeamerScreen(
    viewModel: NomadViewModel,
    preparedApkFile: File?
) {
    val context = LocalContext.current
    var beamLaunchedMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ImmersiveBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Zero-State Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("zero_state_beamer_card"),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderWarm),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = ImmersiveAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ZERO-STATE APK BEAMER",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveAccent,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "Storeless distribution without internet or cell towers",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(ImmersiveSurface, RoundedCornerShape(50))
                                .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = viewModel.apkBeamer.getApkSizeDisplay(preparedApkFile),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "When Google Play and app stores go dark, this phone can beam its own standalone Nomad installer binary directly to any neighboring Android smartphone via direct Bluetooth OPP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val intent = viewModel.createBluetoothBeamIntent()
                            if (intent != null) {
                                try {
                                    context.startActivity(intent)
                                    beamLaunchedMessage = "Bluetooth transfer triggered. Select recipient device on scan list."
                                } catch (e: Exception) {
                                    beamLaunchedMessage = "Launched: ${e.localizedMessage}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImmersiveAccent,
                            contentColor = ImmersiveSurface
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("beam_apk_bluetooth_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Beam Nomad APK via Bluetooth", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (beamLaunchedMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = beamLaunchedMessage!!,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersiveOrange
                        )
                    }
                }
            }
        }

        item {
            // Offline Wi-Fi Hotspot Direct Distribution Mode
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.WifiTethering, contentDescription = null, tint = ImmersiveAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OFFLINE HOTSPOT DIRECT PORTAL",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveAccent,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "To install Nomad onto an iPhone or non-Bluetooth device:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InstructionStep(1, "Turn on Portable Hotspot named 'Nomad-Mesh-Node'")
                    InstructionStep(2, "Have peer survivor connect to your Wi-Fi")
                    InstructionStep(3, "Direct peer to open browser to http://192.168.43.1/nomad.apk")

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stylized Offline QR Code Graphic for direct connection
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ImmersiveSurfaceElevated, RoundedCornerShape(16.dp))
                            .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TacticalQrCodeCanvas(modifier = Modifier.size(110.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "OFFLINE SURVIVAL INSTALLER BEACON",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        item {
            // Tactical Protocol Field Handbook
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CARRIER PIGEON MESH PROTOCOL",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveAccent,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ProtocolPoint(
                        title = "Asynchronous Data Courier",
                        description = "Data does not need constant cell signal. Packets ride along with people walking between districts and swap silently in passing."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProtocolPoint(
                        title = "Silent Proximity Barter Handshake",
                        description = "Keep your inventory updated. When crossing within 30m of another survivor with mutual needs, Nomad alerts you with silent vibration pulses."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ProtocolPoint(
                        title = "Physical NFC Dead Drop Deployment",
                        description = "Affix inexpensive NFC stickers behind landmark signs or under bridges. Tap your phone to write encrypted water coordinates and intel."
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun InstructionStep(stepNumber: Int, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(ImmersiveSurfaceElevated, RoundedCornerShape(50))
                .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$stepNumber",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ImmersiveAccent,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 11.sp, color = TextPrimary)
    }
}

@Composable
private fun ProtocolPoint(title: String, description: String) {
    Column {
        Text(
            text = "• $title",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = description,
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun TacticalQrCodeCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val moduleCount = 17
        val moduleSize = size.width / moduleCount

        // Clean dark background
        drawRect(color = ImmersiveBg)

        // Deterministic pseudo-QR pattern representing Nomad APK URI
        for (row in 0 until moduleCount) {
            for (col in 0 until moduleCount) {
                // Corner positioning squares
                val isCornerTL = (row < 4 && col < 4)
                val isCornerTR = (row < 4 && col >= moduleCount - 4)
                val isCornerBL = (row >= moduleCount - 4 && col < 4)

                val isFilled = when {
                    isCornerTL || isCornerTR || isCornerBL -> {
                        val innerR = if (isCornerBL) row - (moduleCount - 4) else row
                        val innerC = if (isCornerTR) col - (moduleCount - 4) else col
                        innerR == 0 || innerR == 3 || innerC == 0 || innerC == 3 || (innerR == 1 && innerC == 1) || (innerR == 2 && innerC == 2)
                    }
                    else -> ((row * 7 + col * 13 + (row xor col)) % 5) < 2
                }

                if (isFilled) {
                    drawRect(
                        color = ImmersiveAccent,
                        topLeft = Offset(col * moduleSize, row * moduleSize),
                        size = Size(moduleSize - 0.5f, moduleSize - 0.5f)
                    )
                }
            }
        }
    }
}
