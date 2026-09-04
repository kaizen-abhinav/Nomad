package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyBroadcast
import com.example.data.model.HazardType
import com.example.data.model.ThreatLevel
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderWarm
import com.example.ui.theme.ImmersiveOrange
import com.example.ui.theme.ImmersiveRed
import com.example.ui.theme.ImmersiveSurfaceElevated
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HazardMapView(
    broadcasts: List<EmergencyBroadcast>,
    onBroadcastSelected: (EmergencyBroadcast) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHazard by remember { mutableStateOf(broadcasts.firstOrNull()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ImmersiveSurfaceVariant, RoundedCornerShape(20.dp))
            .border(1.dp, ImmersiveBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OFFLINE HAZARD TOPOGRAPHY",
                fontSize = 11.sp,
                color = ImmersiveAccent,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Box(
                modifier = Modifier
                    .background(ImmersiveSurfaceElevated, RoundedCornerShape(50))
                    .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "COURIER-SYNCED",
                    fontSize = 10.sp,
                    color = ImmersiveOrange,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid canvas map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .background(ImmersiveBg, RoundedCornerShape(16.dp))
                .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(16.dp))
                .testTag("hazard_grid_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                val gridSpacing = 28.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = ImmersiveBorderWarm.copy(alpha = 0.25f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 0.8f
                    )
                    x += gridSpacing
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = ImmersiveBorderWarm.copy(alpha = 0.25f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.8f
                    )
                    y += gridSpacing
                }

                // Center Node marker (Handset location)
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(
                    color = ImmersiveAccent.copy(alpha = 0.2f),
                    radius = 24.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = ImmersiveAccent,
                    radius = 4.5.dp.toPx(),
                    center = center
                )

                // Plot hazard points based on pseudo-spatial coordinates
                broadcasts.forEach { item ->
                    val hash = item.id.hashCode()
                    val posX = ((hash % 1000).toDouble() / 1000.0).toFloat().let {
                        val norm = (it + 1f) % 1f
                        norm * (size.width - 60.dp.toPx()) + 30.dp.toPx()
                    }
                    val posY = (((hash / 1000) % 1000).toDouble() / 1000.0).toFloat().let {
                        val norm = (it + 1f) % 1f
                        norm * (size.height - 40.dp.toPx()) + 20.dp.toPx()
                    }

                    val color = when (item.threatLevel) {
                        ThreatLevel.CRITICAL -> ImmersiveRed
                        ThreatLevel.WARNING -> ImmersiveOrange
                        ThreatLevel.INFO -> ImmersiveAccent
                    }

                    // Hazard radius circle
                    drawCircle(
                        color = color.copy(alpha = 0.18f),
                        radius = 18.dp.toPx(),
                        center = Offset(posX, posY)
                    )
                    drawCircle(
                        color = color,
                        radius = 4.dp.toPx(),
                        center = Offset(posX, posY)
                    )
                }
            }

            // Legend tags
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LegendBadge("CRITICAL", ImmersiveRed)
                LegendBadge("WARNING", ImmersiveOrange)
                LegendBadge("SAFE POINT", ImmersiveAccent)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected or latest hazard brief card
        val displayItem = selectedHazard ?: broadcasts.firstOrNull()
        if (displayItem != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBroadcastSelected(displayItem) },
                colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceElevated),
                border = BorderStroke(1.dp, ImmersiveBorderWarm),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (displayItem.hazardType) {
                        HazardType.CLEAN_WATER_POINT -> Icons.Default.WaterDrop
                        HazardType.SAFE_HAVEN -> Icons.Default.Shield
                        HazardType.FLOODED_PASS, HazardType.QUARANTINE_ZONE, HazardType.RADIATION_FALLOUT -> Icons.Default.Dangerous
                        else -> Icons.Default.Warning
                    }
                    val iconColor = when (displayItem.threatLevel) {
                        ThreatLevel.CRITICAL -> ImmersiveRed
                        ThreatLevel.WARNING -> ImmersiveOrange
                        ThreatLevel.INFO -> ImmersiveAccent
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayItem.locationDescription.ifEmpty { displayItem.originCallsign },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = displayItem.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${displayItem.hopsCount} HOPS",
                            fontSize = 11.sp,
                            color = ImmersiveOrange,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "RELAYED",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendBadge(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.85f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
