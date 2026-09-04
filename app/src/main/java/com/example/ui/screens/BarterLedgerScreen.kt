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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.BarterItem
import com.example.data.model.PeerNode
import com.example.data.model.ResourceCategory
import com.example.ui.NomadViewModel
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

@Composable
fun BarterLedgerScreen(
    viewModel: NomadViewModel,
    offeredItems: List<BarterItem>,
    neededItems: List<BarterItem>,
    discoveredPeers: List<PeerNode>
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var selectedPeerForTrade by remember { mutableStateOf<PeerNode?>(null) }
    var tradeSuccessMessage by remember { mutableStateOf<String?>(null) }

    val matchingPeers = discoveredPeers.filter { it.hasProximityBarterMatch }

    Box(modifier = Modifier.fillMaxSize().background(ImmersiveBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                // Proximity Match Banner (if any peers match right now)
                if (matchingPeers.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("proximity_matches_container"),
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
                                        imageVector = Icons.Default.Handshake,
                                        contentDescription = null,
                                        tint = ImmersiveAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "TRADE OPPORTUNITY (${matchingPeers.size})",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        color = ImmersiveAccent,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Text(
                                    text = "PROXIMITY MATCH",
                                    fontSize = 9.sp,
                                    color = ImmersiveOrange,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Surviving nodes within physical walk range have ledger matches with your inventory:",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            matchingPeers.forEach { peer ->
                                MatchPeerItem(
                                    peer = peer,
                                    onInitiate = { selectedPeerForTrade = peer }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            item {
                // Tab Header: HAVE vs NEED
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = ImmersiveBg,
                    contentColor = TextPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = ImmersiveAccent,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ImmersiveBorder, RoundedCornerShape(16.dp))
                        .testTag("barter_tabs")
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "HAVE (POSSESS)",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    color = if (selectedTabIndex == 0) ImmersiveAccent else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${offeredItems.size})",
                                    fontSize = 11.sp,
                                    color = if (selectedTabIndex == 0) ImmersiveAccent else TextMuted
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NEED (SEEKING)",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    color = if (selectedTabIndex == 1) ImmersiveAccent else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${neededItems.size})",
                                    fontSize = 11.sp,
                                    color = if (selectedTabIndex == 1) ImmersiveAccent else TextMuted
                                )
                            }
                        }
                    )
                }
            }

            // Item lists based on tab
            val currentList = if (selectedTabIndex == 0) offeredItems else neededItems
            if (currentList.isEmpty()) {
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
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (selectedTabIndex == 0) "No Offered Resources Listed" else "No Urgent Needs Listed",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the button below to add resources to your offline survival ledger.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(currentList, key = { it.id }) { item ->
                    BarterItemCard(
                        item = item,
                        onDelete = { viewModel.removeBarterItem(item) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to add Barter Item
        FloatingActionButton(
            onClick = { showAddItemDialog = true },
            containerColor = ImmersiveAccent,
            contentColor = ImmersiveSurface,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_barter_item_fab")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Barter Item")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedTabIndex == 0) "List HAVE" else "Add NEED",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }

    if (showAddItemDialog) {
        AddBarterItemDialog(
            isOffering = selectedTabIndex == 0,
            onDismiss = { showAddItemDialog = false },
            onSave = { isOffering, cat, title, qty, cond, notes ->
                viewModel.addBarterItem(isOffering, cat, title, qty, cond, notes)
                showAddItemDialog = false
            }
        )
    }

    selectedPeerForTrade?.let { peer ->
        TradeProposalDialog(
            peer = peer,
            onDismiss = { selectedPeerForTrade = null },
            onConfirmTrade = {
                selectedPeerForTrade = null
                tradeSuccessMessage = "Trade rendezvous sent via BLE Ghost handshake to ${peer.callsign}!"
            }
        )
    }

    tradeSuccessMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { tradeSuccessMessage = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Handshake, contentDescription = null, tint = ImmersiveAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TRADE PROPOSAL SENT", color = ImmersiveAccent, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                }
            },
            text = { Text(msg, color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = { tradeSuccessMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveAccent, contentColor = ImmersiveSurface),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ImmersiveSurface
        )
    }
}

@Composable
private fun MatchPeerItem(
    peer: PeerNode,
    onInitiate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderWarm),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${peer.callsign} (~${"%.1f".format(peer.distanceEstimateMeters)}m away)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = ImmersiveAccent,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "THEY HAVE: ${peer.matchedNeed ?: "Resource"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "THEY NEED: ${peer.matchedOffer ?: "Item"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onInitiate,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveAccent, contentColor = ImmersiveSurface),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("initiate_trade_${peer.nodeId}")
            ) {
                Text("Trade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BarterItemCard(
    item: BarterItem,
    onDelete: () -> Unit
) {
    val categoryIcon = when (item.category) {
        ResourceCategory.MEDICAL -> Icons.Default.MedicalServices
        ResourceCategory.WATER -> Icons.Default.WaterDrop
        ResourceCategory.RATIONS -> Icons.Default.Restaurant
        ResourceCategory.FUEL -> Icons.Default.LocalGasStation
        ResourceCategory.BATTERIES -> Icons.Default.BatteryChargingFull
        ResourceCategory.COMMS -> Icons.Default.Radio
        ResourceCategory.TOOLS -> Icons.Default.Build
        ResourceCategory.AMMO -> Icons.Default.Security
    }

    val accentColor = if (item.isOffering) ImmersiveAccent else ImmersiveOrange

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("barter_item_${item.id}"),
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
                            .background(ImmersiveSurfaceElevated, RoundedCornerShape(12.dp))
                            .border(1.dp, ImmersiveBorderWarm, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${item.category.displayName} • ${item.quantity}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_item_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Item",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (item.notes.isNotBlank() || item.condition.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ImmersiveSurfaceElevated, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (item.condition.isNotBlank()) {
                        Text(
                            text = "COND: ${item.condition}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                    if (item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            fontSize = 10.sp,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddBarterItemDialog(
    isOffering: Boolean,
    onDismiss: () -> Unit,
    onSave: (isOffering: Boolean, cat: ResourceCategory, title: String, qty: String, cond: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Good") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ResourceCategory.MEDICAL) }
    var offeringState by remember { mutableStateOf(isOffering) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (offeringState) "LIST RESOURCE YOU POSSESS (HAVE)" else "LIST DESPERATE RESOURCE (NEED)",
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = if (offeringState) ImmersiveAccent else ImmersiveOrange,
                letterSpacing = 0.5.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("add_barter_item_form")) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Resource Item Name") },
                    placeholder = { Text(if (offeringState) "e.g. Sterile Burn Dressing" else "e.g. 5L Gasoline") },
                    modifier = Modifier.fillMaxWidth().testTag("barter_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (offeringState) ImmersiveAccent else ImmersiveOrange,
                        unfocusedBorderColor = ImmersiveBorder,
                        focusedLabelColor = if (offeringState) ImmersiveAccent else ImmersiveOrange
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        placeholder = { Text("e.g. 2 packs") },
                        modifier = Modifier.weight(1f).testTag("barter_qty_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (offeringState) ImmersiveAccent else ImmersiveOrange,
                            unfocusedBorderColor = ImmersiveBorder
                        )
                    )

                    OutlinedTextField(
                        value = condition,
                        onValueChange = { condition = it },
                        label = { Text("Condition") },
                        placeholder = { Text("e.g. Sealed") },
                        modifier = Modifier.weight(1f).testTag("barter_condition_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (offeringState) ImmersiveAccent else ImmersiveOrange,
                            unfocusedBorderColor = ImmersiveBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Specifications") },
                    placeholder = { Text("e.g. Factory sealed, willing to trade for clean water") },
                    modifier = Modifier.fillMaxWidth().testTag("barter_notes_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (offeringState) ImmersiveAccent else ImmersiveOrange,
                        unfocusedBorderColor = ImmersiveBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "CATEGORY",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ResourceCategory.values().take(4).forEach { cat ->
                        CategoryChip(cat, selectedCategory == cat) { selectedCategory = cat }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ResourceCategory.values().drop(4).take(4).forEach { cat ->
                        CategoryChip(cat, selectedCategory == cat) { selectedCategory = cat }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(offeringState, selectedCategory, title, quantity.ifBlank { "1 unit" }, condition, notes)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (offeringState) ImmersiveAccent else ImmersiveOrange,
                    contentColor = ImmersiveSurface
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("save_barter_item_button")
            ) {
                Text("Save to Ledger", fontWeight = FontWeight.Bold)
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
private fun CategoryChip(cat: ResourceCategory, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(if (isSelected) ImmersiveSurfaceElevated else Color.Transparent, RoundedCornerShape(50))
            .border(1.dp, if (isSelected) ImmersiveAccent else ImmersiveBorderWarm, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = cat.displayName.take(8),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) ImmersiveAccent else TextSecondary
        )
    }
}

@Composable
fun TradeProposalDialog(
    peer: PeerNode,
    onDismiss: () -> Unit,
    onConfirmTrade: () -> Unit
) {
    var rendezvousNote by remember { mutableStateOf("Rendezvous at neutral landmark: Water Tower Gate in 15 mins.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "INITIATE PROXIMITY TRADE",
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                color = ImmersiveAccent,
                letterSpacing = 0.5.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().testTag("trade_proposal_form")) {
                Text(
                    text = "Propose an offline exchange with ${peer.callsign} (~${"%.1f".format(peer.distanceEstimateMeters)}m away).",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = ImmersiveSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorderWarm),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "EXCHANGE SUMMARY",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = ImmersiveAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• You give: ${peer.matchedOffer ?: "Item"}", fontSize = 12.sp, color = TextPrimary)
                        Text("• You receive: ${peer.matchedNeed ?: "Resource"}", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rendezvousNote,
                    onValueChange = { rendezvousNote = it },
                    label = { Text("Rendezvous Point & Signal Instruction") },
                    modifier = Modifier.fillMaxWidth(),
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
                onClick = onConfirmTrade,
                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveAccent, contentColor = ImmersiveSurface),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("confirm_trade_button")
            ) {
                Text("Transmit Trade Signal", fontWeight = FontWeight.Bold)
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
