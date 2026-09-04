package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SurvivalGuideScreen() {
    Box(modifier = Modifier.fillMaxSize().background(ImmersiveBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "NOMAD NODE // OPERATIONS MANUAL",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveAccent,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Welcome to the Nomad survival node. This application is a fully decentralized, off-grid communication and logistics tool designed for operation when traditional infrastructure (cell towers, internet) has collapsed.",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                ManualSectionCard(
                    title = "1. GHOST RELAY (BLE MESH)",
                    icon = Icons.Default.Sensors,
                    content = "The Ghost Relay uses Bluetooth Low Energy (BLE) to create a decentralized mesh network. \n\n• Broadcasts: Emit encrypted short-burst messages that bounce between nearby nodes without ever touching the internet.\n• Passive Sync: Keep your Bluetooth on. Your device will silently exchange ledger updates and encrypted broadcasts with any verified survivor node you pass by.\n• Threat Levels: Tag your messages to warn others of hazards (Hostiles, Biohazard) or share safe haven intel."
                )
            }

            item {
                ManualSectionCard(
                    title = "2. BARTER LEDGER",
                    icon = Icons.Default.CompareArrows,
                    content = "A zero-trust inventory system for post-collapse trading.\n\n• List what you have (Offers) and what you need (Needs).\n• Proximity Matching: When Ghost Sync detects another node nearby whose Offers match your Needs (or vice versa), you'll receive a silent Proximity Alert.\n• Secure Trade: Items are categorized by priority (e.g., Medical, Ammo, Rations). Trade physically when safe to do so."
                )
            }

            item {
                ManualSectionCard(
                    title = "3. DEAD DROPS (NFC STASH)",
                    icon = Icons.Default.Contactless,
                    content = "Secure digital intel caching using physical NFC tags.\n\n• Stashing: Hide a physical NFC sticker in a real-world location. Use the app to write encrypted coordinates, passcodes, or supply caches to the tag.\n• Scavenging: Hold your handset's NFC reader (usually top-back) against a tag to extract the encrypted intel.\n• Only those with the Nomad Node app can read or write these encrypted payloads."
                )
            }

            item {
                ManualSectionCard(
                    title = "4. ZERO-STATE BEAMER",
                    icon = Icons.Default.Share,
                    content = "The network only works if others have the app. The Beamer allows you to share this very application with someone who doesn't have it, even with zero internet.\n\n• It extracts its own APK installer and transmits it directly via Bluetooth or Wi-Fi Direct to the other person's device."
                )
            }

            item {
                ManualSectionCard(
                    title = "5. OPSEC & BATTERY",
                    icon = Icons.Default.OfflineBolt,
                    content = "• The app uses Location Services solely to tag local drops; this data never leaves your device unless actively broadcast.\n• Ghost Sync runs efficiently in the background but requires constant Bluetooth scanning. To conserve battery, disable Ghost Sync from the top menu when securely holed up."
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "END OF FILE // STAY SAFE, GHOST",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ManualSectionCard(title: String, icon: ImageVector, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ImmersiveAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveAccent,
                    letterSpacing = 0.5.sp
                )
            }
            
            Text(
                text = content,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}
