package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyBroadcast
import com.example.data.model.HazardType
import com.example.data.model.PeerNode
import com.example.data.model.ThreatLevel
import com.example.ui.NomadViewModel
import com.example.ui.components.CarrierPigeonHopDialog
import com.example.ui.components.HazardMapView
import com.example.ui.components.MeshRadarCanvas
import com.example.ui.components.PeerDetailDialog
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderWarm
import com.example.ui.theme.ImmersiveOrange
import com.example.ui.theme.ImmersiveOrangeBright
import com.example.ui.theme.ImmersiveRed
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceElevated
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GhostRelayScreen(
    viewModel: NomadViewModel,
    broadcasts: List<EmergencyBroadcast>,
    peers: List<PeerNode>,
    isGhostSyncActive: Boolean,
    relayedCount: Int,
    hopsCount: Int,
    onNavigateToBarter: () -> Unit
) {
    var selectedBroadcastForHops by remember { mutableStateOf<EmergencyBroadcast?>(null) }
    var selectedPeerForDetail by remember { mutableStateOf<PeerNode?>(null) }
    var showBroadcastComposer by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(ImmersiveBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Mesh Telemetry Header Bar
                MeshTelemetryCard(
                    localCallsign = viewModel.localCallsign,
                    isGhostSyncActive = isGhostSyncActive,
                    relayedCount = relayedCount,
                    hopsCount = hopsCount,
                    discoveredPeersCount = peers.size,
                    onToggleSync = { viewModel.toggleGhostSync(it) }
                )
            }

            item {
                // Interactive Mesh Radar Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SILENT PROXIMITY RADAR",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    color = ImmersiveAccent,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = "BLE & Wi-Fi Direct Handshake (45m range)",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        MeshRadarCanvas(
                            peers = peers,
                            onPeerClick = { selectedPeerForDetail = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Nearby peer blip chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            peers.take(3).forEach { peer ->
                                Card(
                                    modifier = Modifier
                                        .clickable { selectedPeerForDetail = peer }
                                        .testTag("peer_chip_${peer.nodeId}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (peer.hasProximityBarterMatch) ImmersiveSurfaceElevated else ImmersiveSurfaceVariant
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (peer.hasProximityBarterMatch) ImmersiveBorderWarm else ImmersiveBorder
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = peer.callsign.take(14),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = if (peer.hasProximityBarterMatch) ImmersiveAccent else ImmersiveOrangeBright
                                        )
                                        Text(
                                            text = "~${"%.0f".format(peer.distanceEstimateMeters)}m (${peer.rssi}dBm)",
                                            fontSize = 9.sp,
                                            color = TextMuted,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Offline Hazard Topography Map
                HazardMapView(
                    broadcasts = broadcasts,
                    onBroadcastSelected = { selectedBroadcastForHops = it }
                )
            }

            item {
                // Emergency Broadcasts Feed Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EMERGENCY BROADCAST FEED",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveAccent,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Asynchronously swapped via physical couriers",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(ImmersiveSurfaceElevated, RoundedCornerShape(50))
                            .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${broadcasts.size} CACHED",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersiveOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            items(broadcasts, key = { it.id }) { item ->
                EmergencyBroadcastCard(
                    broadcast = item,
                    onInspectHops = { selectedBroadcastForHops = item }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to post SOS or emergency broadcast
        FloatingActionButton(
            onClick = { showBroadcastComposer = true },
            containerColor = ImmersiveAccent,
            contentColor = ImmersiveSurface,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("compose_broadcast_fab")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Broadcast SOS / Hazard")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // Inspection Dialogs
    selectedBroadcastForHops?.let { broadcast ->
        CarrierPigeonHopDialog(
            broadcast = broadcast,
            onDismiss = { selectedBroadcastForHops = null }
        )
    }

    selectedPeerForDetail?.let { peer ->
        PeerDetailDialog(
            peer = peer,
            onDismiss = { selectedPeerForDetail = null },
            onInitiateBarterProposal = {
                selectedPeerForDetail = null
                onNavigateToBarter()
            }
        )
    }

    if (showBroadcastComposer) {
        BroadcastComposerDialog(
            onDismiss = { showBroadcastComposer = false },
            onPublish = { content, threat, hazard, location ->
                viewModel.publishBroadcast(content, threat, hazard, location)
                showBroadcastComposer = false
            }
        )
    }
}

@Composable
private fun MeshTelemetryCard(
    localCallsign: String,
    isGhostSyncActive: Boolean,
    relayedCount: Int,
    hopsCount: Int,
    discoveredPeersCount: Int,
    onToggleSync: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mesh_telemetry_card"),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        shape = RoundedCornerShape(32.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row with Title & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ghost Sync Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier
                        .background(ImmersiveSurfaceElevated, RoundedCornerShape(50))
                        .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isGhostSyncActive) ImmersiveOrange else TextMuted, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGhostSyncActive) "BLE SCANNING" else "RADIO SILENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (isGhostSyncActive) ImmersiveAccent else TextMuted,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pulse beacon + Courier Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular pulsating beacon matching design
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(2.dp, ImmersiveAccent.copy(alpha = 0.25f), CircleShape)
                        .padding(8.dp)
                        .border(1.dp, ImmersiveAccent.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ImmersiveAccent, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$discoveredPeersCount Couriers in range",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Last relay: $relayedCount packets · $hopsCount total hops",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar track with glowing accent fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(ImmersiveBorder, RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (isGhostSyncActive) 0.72f else 0.1f)
                                .height(6.dp)
                                .background(ImmersiveAccent, RoundedCornerShape(50))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom control row: Node ID & Ghost Sync switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ImmersiveSurfaceVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LOCAL CALLSIGN",
                        fontSize = 9.sp,
                        color = TextMuted,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = localCallsign,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveAccent
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isGhostSyncActive) "Sync Active" else "Sync Paused",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = isGhostSyncActive,
                        onCheckedChange = onToggleSync,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ImmersiveSurface,
                            checkedTrackColor = ImmersiveAccent,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = ImmersiveBorder
                        ),
                        modifier = Modifier.testTag("ghost_sync_toggle")
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun EmergencyBroadcastCard(
    broadcast: EmergencyBroadcast,
    onInspectHops: () -> Unit
) {
    val (threatColor, threatLabel) = when (broadcast.threatLevel) {
        ThreatLevel.CRITICAL -> ImmersiveRed to "CRITICAL FLASH"
        ThreatLevel.WARNING -> ImmersiveOrange to "WARNING"
        ThreatLevel.INFO -> ImmersiveAccent to "INFO UPDATE"
    }

    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(broadcast.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("broadcast_card_${broadcast.id}"),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(ImmersiveSurfaceElevated, RoundedCornerShape(50))
                            .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = threatLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = threatColor,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = broadcast.originCallsign,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = broadcast.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp
            )

            if (broadcast.locationDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "LOC: ${broadcast.locationDescription}",
                    fontSize = 11.sp,
                    color = ImmersiveAccent,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ImmersiveAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Encrypted • ${broadcast.hopsCount} carrier hops",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onInspectHops,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersiveSurfaceElevated,
                        contentColor = ImmersiveAccent
                    ),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderWarm),
                    modifier = Modifier.testTag("inspect_hops_${broadcast.id}")
                ) {
                    Icon(imageVector = Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Carrier Trail", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BroadcastComposerDialog(
    onDismiss: () -> Unit,
    onPublish: (content: String, threat: ThreatLevel, hazard: HazardType, location: String) -> Unit
) {
    var content by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    var location by remember { mutableStateOf(com.example.ui.getCurrentLocationString(context)) }
    var selectedThreat by remember { mutableStateOf(ThreatLevel.WARNING) }
    var selectedHazard by remember { mutableStateOf(HazardType.SAFE_HAVEN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "COMPOSE ENCRYPTED BROADCAST",
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = ImmersiveAccent,
                letterSpacing = 0.5.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("broadcast_composer_form")) {
                Text(
                    text = "This emergency message will silently swap to every phone crossing your path.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Survival Alert / Hazard Intel") },
                    placeholder = { Text("e.g. Clean drinking spring located behind timber mill.") },
                    modifier = Modifier.fillMaxWidth().testTag("broadcast_content_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveAccent,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedLabelColor = ImmersiveAccent
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location Landmark / Coordinates") },
                    placeholder = { Text("e.g. Sector 4 East Bridge") },
                    modifier = Modifier.fillMaxWidth().testTag("broadcast_location_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveAccent,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedLabelColor = ImmersiveAccent
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "THREAT SEVERITY",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThreatLevel.values().forEach { level ->
                        val isSelected = selectedThreat == level
                        OutlinedButton(
                            onClick = { selectedThreat = level },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) ImmersiveSurfaceElevated else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ImmersiveAccent else ImmersiveBorder
                            )
                        ) {
                            Text(
                                text = level.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ImmersiveAccent else TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onPublish(content, selectedThreat, selectedHazard, location)
                    }
                },
                enabled = content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImmersiveAccent,
                    contentColor = ImmersiveSurface
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("submit_broadcast_button")
            ) {
                Text("Transmit to Mesh", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = ImmersiveSurfaceVariant,
        shape = RoundedCornerShape(24.dp)
    )
}
