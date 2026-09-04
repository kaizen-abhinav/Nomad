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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.PeerNode
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

@Composable
fun PeerDetailDialog(
    peer: PeerNode,
    onDismiss: () -> Unit,
    onInitiateBarterProposal: (PeerNode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = if (peer.hasProximityBarterMatch) ImmersiveRed else ImmersiveAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = peer.callsign,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${peer.connectionType} • RSSI ${peer.rssi} dBm (~${"%.1f".format(peer.distanceEstimateMeters)}m)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("peer_detail_dialog")
            ) {
                if (peer.hasProximityBarterMatch) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ImmersiveSurfaceElevated, RoundedCornerShape(14.dp))
                            .border(1.dp, ImmersiveRed, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = ImmersiveRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MUTUAL BARTER MATCH DETECTED!\nThey possess what you need and vice versa.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "RESOURCES OFFERED (HAVE)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveAccent,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (peer.peerOffers.isEmpty()) {
                    Text("No public offerings broadcasted.", fontSize = 12.sp, color = TextMuted)
                } else {
                    peer.peerOffers.forEach { item ->
                        Text("• $item", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "RESOURCES SOUGHT (NEED)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveOrange,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (peer.peerNeeds.isEmpty()) {
                    Text("No urgent resource needs listed.", fontSize = 12.sp, color = TextMuted)
                } else {
                    peer.peerNeeds.forEach { item ->
                        Text("• $item", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderWarm),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ImmersiveAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Courier packets swapped: ${peer.carriedPacketsCount} broadcasts",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onInitiateBarterProposal(peer) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImmersiveAccent,
                    contentColor = ImmersiveSurface
                ),
                shape = RoundedCornerShape(50)
            ) {
                Icon(imageVector = Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Initiate Barter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = ImmersiveSurfaceVariant,
        shape = RoundedCornerShape(24.dp)
    )
}
