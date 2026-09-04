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
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
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
import com.example.data.model.DigitalDeadDrop
import com.example.data.model.ScavengeStatus
import com.example.ui.NomadViewModel
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderWarm
import com.example.ui.theme.ImmersiveOrange
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
fun DeadDropsScreen(
    viewModel: NomadViewModel,
    deadDrops: List<DigitalDeadDrop>
) {
    var showCreateDropDialog by remember { mutableStateOf(false) }
    var inspectedDrop by remember { mutableStateOf<DigitalDeadDrop?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(ImmersiveBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // NFC Hardware & Status Banner
                NfcStatusBanner()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PASSIVE NFC DEAD DROP REGISTRY",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveAccent,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Physical tags glued to walls, trees, signs for offline cache intel",
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
                            text = "${deadDrops.size} TAGS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ImmersiveOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (deadDrops.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contactless,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Dead Drops In Local Registry",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap a physical NFC tag or create a new survival stash note below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(deadDrops, key = { it.id }) { drop ->
                    DeadDropCard(
                        drop = drop,
                        onInspect = { inspectedDrop = drop }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FAB to create new Dead Drop
        FloatingActionButton(
            onClick = { showCreateDropDialog = true },
            containerColor = ImmersiveAccent,
            contentColor = ImmersiveSurface,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("create_dead_drop_fab")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Dead Drop")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stash Intel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showCreateDropDialog) {
        CreateDeadDropDialog(
            onDismiss = { showCreateDropDialog = false },
            onSave = { title, hint, intel, pass, status, coords ->
                viewModel.addDeadDrop(title, hint, intel, pass, status, coords)
                showCreateDropDialog = false
            }
        )
    }

    inspectedDrop?.let { drop ->
        DeadDropDetailDialog(
            drop = drop,
            onDismiss = { inspectedDrop = null }
        )
    }
}

@Composable
private fun NfcStatusBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nfc_status_banner"),
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(ImmersiveAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "NFC DROP SENSOR: READY",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Hold handset back against physical tag to read/write",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeadDropCard(
    drop: DigitalDeadDrop,
    onInspect: () -> Unit
) {
    val (statusColor, statusLabel) = when (drop.scavengeStatus) {
        ScavengeStatus.VERIFIED_INTACT -> ImmersiveAccent to drop.scavengeStatus.label
        ScavengeStatus.DEPLETED -> TextMuted to drop.scavengeStatus.label
        ScavengeStatus.COMPROMISED -> ImmersiveRed to drop.scavengeStatus.label
        ScavengeStatus.UNKNOWN -> ImmersiveOrange to drop.scavengeStatus.label
    }

    val formattedDate = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(drop.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("dead_drop_card_${drop.id}"),
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
                            text = statusLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = drop.tagUid,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = drop.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = ImmersiveAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = drop.landmarkHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUTHOR: ${drop.authorCallsign}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                )

                Button(
                    onClick = onInspect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersiveSurfaceElevated,
                        contentColor = ImmersiveAccent
                    ),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderWarm),
                    modifier = Modifier.testTag("decrypt_intel_${drop.id}")
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decrypt Intel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateDeadDropDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, hint: String, intel: String, pass: String, status: ScavengeStatus, coords: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }
    var intel by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var coords by remember { mutableStateOf(com.example.ui.getCurrentLocationString(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "CREATE DIGITAL DEAD DROP",
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = ImmersiveAccent,
                letterSpacing = 0.5.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("create_dead_drop_form")) {
                Text(
                    text = "Leave encrypted survival intel tied to a physical NFC sticker on a tree, rock, or landmark.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Dead Drop Title") },
                    placeholder = { Text("e.g. Water Cache #4") },
                    modifier = Modifier.fillMaxWidth().testTag("dead_drop_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveAccent,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedLabelColor = ImmersiveAccent
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Physical Landmark Hint") },
                    placeholder = { Text("e.g. Under rusted iron ladder on water tower pier") },
                    modifier = Modifier.fillMaxWidth().testTag("dead_drop_hint_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveAccent,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedLabelColor = ImmersiveAccent
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = intel,
                    onValueChange = { intel = it },
                    label = { Text("Secret Intel / Cache Contents") },
                    placeholder = { Text("e.g. Lock combination 4192. 2 water filters inside.") },
                    modifier = Modifier.fillMaxWidth().testTag("dead_drop_intel_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveAccent,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedLabelColor = ImmersiveAccent
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Access Passcode / Decryption Key") },
                    placeholder = { Text("e.g. RANGER-KEY-1") },
                    modifier = Modifier.fillMaxWidth().testTag("dead_drop_pass_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ImmersiveAccent,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedLabelColor = ImmersiveAccent
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && intel.isNotBlank()) {
                        onSave(title, hint.ifBlank { "Physical Tag Location" }, intel, pass.ifBlank { "OPEN" }, ScavengeStatus.VERIFIED_INTACT, coords)
                    }
                },
                enabled = title.isNotBlank() && intel.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveAccent, contentColor = ImmersiveSurface),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("save_dead_drop_button")
            ) {
                Text("Arm & Save Drop", fontWeight = FontWeight.Bold)
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

@Composable
fun DeadDropDetailDialog(
    drop: DigitalDeadDrop,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Contactless,
                    contentDescription = null,
                    tint = ImmersiveAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = drop.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("dead_drop_detail_content")) {
                Text(
                    text = "PHYSICAL LOCATION HINT:",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveAccent,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = drop.landmarkHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderWarm),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ImmersiveAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DECRYPTED FIELD INTEL",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = drop.decryptedIntel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TAG UID", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        Text(drop.tagUid, fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PASSCODE", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        Text(drop.accessPasscode, fontSize = 11.sp, color = ImmersiveOrange, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveAccent, contentColor = ImmersiveSurface),
                shape = RoundedCornerShape(50)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = ImmersiveSurfaceVariant,
        shape = RoundedCornerShape(24.dp)
    )
}
