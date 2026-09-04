package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.PeerNode
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBorderWarm
import com.example.ui.theme.ImmersiveOrange
import com.example.ui.theme.ImmersiveRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MeshRadarCanvas(
    peers: List<PeerNode>,
    onPeerClick: (PeerNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(300.dp)
            .testTag("mesh_radar_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.width / 2 - 12.dp.toPx()

            // Circular Rings (10m, 25m, 50m)
            val ringRadii = listOf(maxRadius * 0.33f, maxRadius * 0.66f, maxRadius)
            ringRadii.forEachIndexed { index, radius ->
                drawCircle(
                    color = ImmersiveBorderWarm.copy(alpha = 0.6f),
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = if (index == 1) PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f) else null
                    )
                )
            }

            // Crosshair lines
            drawLine(
                color = ImmersiveBorderWarm.copy(alpha = 0.4f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = ImmersiveBorderWarm.copy(alpha = 0.4f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // Radar sweeping wedge
            rotate(degrees = sweepAngle, pivot = center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.7f to Color.Transparent,
                        1.0f to ImmersiveAccent.copy(alpha = 0.25f),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = true,
                    topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                    size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
                )
                // Leading beam
                drawLine(
                    color = ImmersiveAccent,
                    start = center,
                    end = Offset(center.x + maxRadius, center.y),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Center Node Core (Self - Nomad handset)
            drawCircle(
                color = ImmersiveAccent,
                radius = 7.dp.toPx() * pulseScale,
                center = center
            )
            drawCircle(
                color = ImmersiveAccent.copy(alpha = 0.35f),
                radius = 12.dp.toPx() * pulseScale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Draw discovered peer nodes on radar
            peers.forEachIndexed { idx, peer ->
                // Map distance to radar radius (normalized 0..45m)
                val normalizedDist = (peer.distanceEstimateMeters.coerceIn(2f, 45f) / 45f) * maxRadius
                // Deterministic angle based on hash of node id
                val angleRad = ((peer.nodeId.hashCode() % 360) * (PI / 180f)).toFloat()
                val peerX = center.x + normalizedDist * cos(angleRad)
                val peerY = center.y + normalizedDist * sin(angleRad)
                val peerOffset = Offset(peerX, peerY)

                val nodeColor = if (peer.hasProximityBarterMatch) {
                    ImmersiveRed // Glowing match
                } else {
                    ImmersiveOrange
                }

                // Halo ring if barter match
                if (peer.hasProximityBarterMatch) {
                    drawCircle(
                        color = ImmersiveRed.copy(alpha = 0.5f),
                        radius = 10.dp.toPx() * pulseScale,
                        center = peerOffset,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Peer Blip
                drawCircle(
                    color = nodeColor,
                    radius = if (peer.hasProximityBarterMatch) 6.dp.toPx() else 4.5.dp.toPx(),
                    center = peerOffset
                )
            }
        }
    }
}
