package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.MenuBook
import com.example.ui.screens.SurvivalGuideScreen

import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Contactless
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.NomadViewModel
import com.example.ui.components.TactileAlertBanner
import com.example.ui.screens.BarterLedgerScreen
import com.example.ui.screens.DeadDropsScreen
import com.example.ui.screens.GhostRelayScreen
import com.example.ui.screens.ZeroStateBeamerScreen
import com.example.ui.theme.ImmersiveAccent
import com.example.ui.theme.ImmersiveBg
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveBorderWarm
import com.example.ui.theme.ImmersiveOrange
import com.example.ui.theme.ImmersiveOrangeBright
import com.example.ui.theme.ImmersiveSurface
import com.example.ui.theme.ImmersiveSurfaceElevated
import com.example.ui.theme.ImmersiveSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class NomadNavigationTab(
    val title: String,
    val iconFilled: androidx.compose.ui.graphics.vector.ImageVector,
    val iconOutlined: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
) {
    GHOST_RELAY("Ghost Relay", Icons.Filled.Sensors, Icons.Outlined.Sensors, "nav_ghost_relay"),
    BARTER_LEDGER("Barter", Icons.Filled.CompareArrows, Icons.Outlined.CompareArrows, "nav_barter_ledger"),
    DEAD_DROPS("Dead Drops", Icons.Filled.Contactless, Icons.Outlined.Contactless, "nav_dead_drops"),
    BEAMER("Beamer", Icons.Filled.Share, Icons.Outlined.Share, "nav_beamer"),
    GUIDE("Guide", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_guide")
}

class MainActivity : ComponentActivity() {

    private val viewModel: NomadViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val bluetoothScanGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions[Manifest.permission.BLUETOOTH_SCAN] ?: false
        } else true

        if (fineLocationGranted || bluetoothScanGranted) {
            viewModel.meshManager.startBleScanningIfAllowed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestMeshPermissions()

        setContent {
            MyApplicationTheme {
                NomadApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.nfcManager.enableReaderMode(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel.nfcManager.disableReaderMode(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle NFC NDEF Discovery
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action || NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null && rawMessages.isNotEmpty()) {
                val message = rawMessages[0] as NdefMessage
                if (message.records.isNotEmpty()) {
                    val record = message.records[0]
                    val payload = record.payload
                    val langCodeLen = payload[0].toInt() and 0x3F
                    val text = String(payload, langCodeLen + 1, payload.size - langCodeLen - 1, Charsets.UTF_8)
                    val drop = viewModel.nfcManager.parseTextToDeadDrop(text, "NFC-INTENT-TAG")
                    viewModel.processTagTap(drop)
                }
            }
        }
    }

    private fun checkAndRequestMeshPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}

@Composable
fun NomadApp(viewModel: NomadViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    val broadcasts by viewModel.allBroadcasts.collectAsStateWithLifecycle()
    val offeredItems by viewModel.offeredItems.collectAsStateWithLifecycle()
    val neededItems by viewModel.neededItems.collectAsStateWithLifecycle()
    val deadDrops by viewModel.allDeadDrops.collectAsStateWithLifecycle()
    val peers by viewModel.discoveredPeers.collectAsStateWithLifecycle()
    val isGhostSyncActive by viewModel.isGhostSyncActive.collectAsStateWithLifecycle()
    val relayedCount by viewModel.relayedBroadcastsCount.collectAsStateWithLifecycle()
    val hopsCount by viewModel.carrierPigeonHopsTotal.collectAsStateWithLifecycle()
    val activeProximityAlert by viewModel.activeProximityAlert.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val preparedApk by viewModel.preparedApkFile.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val matchingPeersCount = peers.count { it.hasProximityBarterMatch }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ImmersiveBg,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TacticalTopAppBar(
                localCallsign = viewModel.localCallsign,
                isGhostSyncActive = isGhostSyncActive,
                activeAlert = activeProximityAlert,
                onDismissAlert = { viewModel.dismissProximityAlert() },
                onViewMatch = {
                    viewModel.dismissProximityAlert()
                    selectedTab = NomadNavigationTab.BARTER_LEDGER.ordinal
                }
            )
        },
        bottomBar = {
            NomadBottomNavigationBar(
                selectedTabIndex = selectedTab,
                matchingPeersCount = matchingPeersCount,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (NomadNavigationTab.values()[selectedTab]) {
                NomadNavigationTab.GHOST_RELAY -> {
                    GhostRelayScreen(
                        viewModel = viewModel,
                        broadcasts = broadcasts,
                        peers = peers,
                        isGhostSyncActive = isGhostSyncActive,
                        relayedCount = relayedCount,
                        hopsCount = hopsCount,
                        onNavigateToBarter = { selectedTab = NomadNavigationTab.BARTER_LEDGER.ordinal }
                    )
                }
                NomadNavigationTab.BARTER_LEDGER -> {
                    BarterLedgerScreen(
                        viewModel = viewModel,
                        offeredItems = offeredItems,
                        neededItems = neededItems,
                        discoveredPeers = peers
                    )
                }
                NomadNavigationTab.DEAD_DROPS -> {
                    DeadDropsScreen(
                        viewModel = viewModel,
                        deadDrops = deadDrops
                    )
                }
                NomadNavigationTab.BEAMER -> {
                    ZeroStateBeamerScreen(
                        viewModel = viewModel,
                        preparedApkFile = preparedApk
                    )
                }
                NomadNavigationTab.GUIDE -> {
                    SurvivalGuideScreen()
                }
            }
        }
    }
}

@Composable
private fun TacticalTopAppBar(
    localCallsign: String,
    isGhostSyncActive: Boolean,
    activeAlert: com.example.mesh.ProximityAlertEvent?,
    onDismissAlert: () -> Unit,
    onViewMatch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ImmersiveBg)
            .border(1.dp, ImmersiveBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NOMAD NODE v2.4",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = ImmersiveAccent,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(if (isGhostSyncActive) ImmersiveOrange else TextMuted, CircleShape)
                            .border(2.dp, if (isGhostSyncActive) ImmersiveOrange.copy(alpha = 0.4f) else Color.Transparent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGhostSyncActive) "Mesh: Active" else "Mesh: Silent",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        color = TextPrimary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "LOCAL ID",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = localCallsign,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = ImmersiveAccent
                )
            }
        }

        // Proximity alert banner (if survivor nearby matches ledger)
        TactileAlertBanner(
            alert = activeAlert,
            onDismiss = onDismissAlert,
            onViewMatch = onViewMatch
        )
    }
}

@Composable
private fun NomadBottomNavigationBar(
    selectedTabIndex: Int,
    matchingPeersCount: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ImmersiveBorder)
            .testTag("nomad_bottom_navigation"),
        containerColor = ImmersiveSurface,
        windowInsets = WindowInsets.navigationBars
    ) {
        NomadNavigationTab.values().forEachIndexed { index, tab ->
            val isSelected = selectedTabIndex == index

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    if (tab == NomadNavigationTab.BARTER_LEDGER && matchingPeersCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = ImmersiveAccent,
                                    contentColor = ImmersiveSurface
                                ) {
                                    Text(
                                        text = "$matchingPeersCount",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.iconFilled else tab.iconOutlined,
                                contentDescription = tab.title
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) tab.iconFilled else tab.iconOutlined,
                            contentDescription = tab.title
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ImmersiveAccent,
                    selectedTextColor = ImmersiveAccent,
                    indicatorColor = ImmersiveBorderWarm,
                    unselectedIconColor = TextSecondary.copy(alpha = 0.7f),
                    unselectedTextColor = TextSecondary.copy(alpha = 0.7f)
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}

@Composable
fun NomadPreview() {
    NomadBottomNavigationBar(
        selectedTabIndex = 0,
        matchingPeersCount = 2,
        onTabSelected = {}
    )
}
