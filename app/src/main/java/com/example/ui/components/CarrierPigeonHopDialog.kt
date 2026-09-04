package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyBroadcast
import com.example.ui.theme.ImmersiveAccent
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
fun CarrierPigeonHopDialog(
    broadcast: EmergencyBroadcast,
    onDismiss: () -> Unit
) {
    val hops = broadcast.carrierPigeonRoute.split("->").map { it.trim() }
    val formattedDate = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(broadcast.timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = ImmersiveAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CARRIER PIGEON HOP RELAY",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveAccent,
                    letterSpacing = 0.5.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("carrier_hop_dialog_content")
            ) {
                Text(
                    text = "In a grid-down world, data physically travels inside surviving smartphones as people walk across towns and checkpoints.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderWarm),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "ORIGIN: ${broadcast.originCallsign}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "TIME: $formattedDate (${broadcast.hopsCount} physical hops)",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        if (broadcast.locationDescription.isNotBlank()) {
                            Text(
                                text = "LOCATION: ${broadcast.locationDescription}",
                                fontSize = 11.sp,
                                color = ImmersiveOrange
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PHYSICAL RELAY CHAIN",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = ImmersiveAccent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hop Nodes visualization
                hops.forEachIndexed { index, nodeName ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        val isFinalNode = index == hops.lastIndex
                        val isOrigin = index == 0
                        val nodeBadgeColor = when {
                            isOrigin -> ImmersiveRed
                            isFinalNode -> ImmersiveAccent
                            else -> ImmersiveOrange
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(ImmersiveSurfaceElevated, CircleShape)
                                .border(1.5.dp, nodeBadgeColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = nodeBadgeColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (isFinalNode) "THIS NODE (YOU)" else nodeName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isFinalNode) ImmersiveAccent else TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = when {
                                    isOrigin -> "Origin Broadcast Broadcasted"
                                    isFinalNode -> "Swapped via BLE Ghost Sync in Proximity"
                                    else -> "Physical Courier in Transit"
                                },
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    if (index < hops.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(start = 11.dp)
                                .height(12.dp)
                                .width(2.dp)
                                .background(ImmersiveBorderWarm)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ImmersiveSurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ImmersiveAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cryptographic Signature Verified: ${broadcast.signatureHash}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveAccent, contentColor = ImmersiveSurface)
            ) {
                Text("Acknowledge", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = ImmersiveSurfaceVariant,
        shape = RoundedCornerShape(24.dp)
    )
}
