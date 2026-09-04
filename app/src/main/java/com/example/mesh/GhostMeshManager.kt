package com.example.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.BarterItem
import com.example.data.model.EmergencyBroadcast
import com.example.data.model.PeerNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class ProximityAlertEvent(
    val peerCallsign: String,
    val matchedOffer: String,
    val matchedNeed: String,
    val distanceMeters: Float
)

class GhostMeshManager(
    private val context: Context,
    val localCallsign: String = "NODE-NOMAD-${Random.nextInt(100, 999)}"
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _isGhostSyncActive = MutableStateFlow(true)
    val isGhostSyncActive: StateFlow<Boolean> = _isGhostSyncActive.asStateFlow()

    private val _discoveredPeers = MutableStateFlow<List<PeerNode>>(emptyList())
    val discoveredPeers: StateFlow<List<PeerNode>> = _discoveredPeers.asStateFlow()

    private val _proximityAlertEvents = MutableSharedFlow<ProximityAlertEvent>()
    val proximityAlertEvents: SharedFlow<ProximityAlertEvent> = _proximityAlertEvents.asSharedFlow()

    private val _relayedBroadcastsCount = MutableStateFlow(0)
    val relayedBroadcastsCount: StateFlow<Int> = _relayedBroadcastsCount.asStateFlow()

    private val _carrierPigeonHopsTotal = MutableStateFlow(0)
    val carrierPigeonHopsTotal: StateFlow<Int> = _carrierPigeonHopsTotal.asStateFlow()

    companion object {
        val NOMAD_SERVICE_UUID: ParcelUuid = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { handleRawBleDevice(it) }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { handleRawBleDevice(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            // Handled gracefully in offline survival mode
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
        }
    }

    init {
        startBleScanningIfAllowed()
        startBleAdvertisingIfAllowed()
    }

    @SuppressLint("MissingPermission")
    fun startBleScanningIfAllowed() {
        try {
            if (bluetoothAdapter?.isEnabled == true) {
                val scanner = bluetoothAdapter.bluetoothLeScanner
                val filter = ScanFilter.Builder().setServiceUuid(NOMAD_SERVICE_UUID).build()
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
                scanner?.startScan(listOf(filter), settings, scanCallback)
            }
        } catch (_: SecurityException) {
            // Permissions not yet granted
        }
    }

    @SuppressLint("MissingPermission")
    fun stopBleScanning() {
        try {
            val scanner = bluetoothAdapter?.bluetoothLeScanner
            scanner?.stopScan(scanCallback)
        } catch (_: SecurityException) {
            // Ignored
        }
    }

    @SuppressLint("MissingPermission")
    fun startBleAdvertisingIfAllowed() {
        try {
            if (bluetoothAdapter?.isEnabled == true) {
                val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
                
                val settings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setConnectable(false)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .build()

                val data = AdvertiseData.Builder()
                    .setIncludeDeviceName(false)
                    .addServiceUuid(NOMAD_SERVICE_UUID)
                    .addServiceData(NOMAD_SERVICE_UUID, localCallsign.toByteArray(Charsets.UTF_8))
                    .build()

                advertiser?.startAdvertising(settings, data, advertiseCallback)
            }
        } catch (_: SecurityException) {
        }
    }

    @SuppressLint("MissingPermission")
    fun stopBleAdvertising() {
        try {
            val advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
            advertiser?.stopAdvertising(advertiseCallback)
        } catch (_: SecurityException) {
        }
    }

    fun toggleGhostSync(enabled: Boolean) {
        _isGhostSyncActive.value = enabled
        if (enabled) {
            startBleScanningIfAllowed()
            startBleAdvertisingIfAllowed()
        } else {
            stopBleScanning()
            stopBleAdvertising()
        }
    }

    private fun handleRawBleDevice(result: ScanResult) {
        val device = result.device ?: return
        val id = device.address ?: UUID.randomUUID().toString()
        val rssi = result.rssi
        val distance = calculateDistanceMeters(rssi)
        
        // Extract callsign from service data if available
        val serviceDataBytes = result.scanRecord?.getServiceData(NOMAD_SERVICE_UUID)
        val callsign = if (serviceDataBytes != null) {
            String(serviceDataBytes, Charsets.UTF_8)
        } else {
            "RELAY-${id.takeLast(4)}"
        }

        updateOrAddPeer(
            PeerNode(
                nodeId = id,
                callsign = callsign,
                rssi = rssi,
                distanceEstimateMeters = distance,
                connectionType = "BLE Ghost Relay",
                carriedPacketsCount = 0 // Real packet exchange requires GATT connection, simplified for BLE beacon
            )
        )
    }

    fun updateOrAddPeer(peer: PeerNode) {
        val current = _discoveredPeers.value.toMutableList()
        val index = current.indexOfFirst { it.nodeId == peer.nodeId }
        if (index >= 0) {
            current[index] = peer
        } else {
            current.add(0, peer)
        }
        _discoveredPeers.value = current
    }

    fun checkBarterMatches(myHaveItems: List<BarterItem>, myNeedItems: List<BarterItem>) {
        if (!_isGhostSyncActive.value) return

        val myHavesTitles = myHaveItems.filter { it.active }.map { it.title.lowercase() }
        val myNeedsTitles = myNeedItems.filter { it.active }.map { it.title.lowercase() }

        val updatedPeers = _discoveredPeers.value.map { peer ->
            val matchedNeedFromPeer = peer.peerOffers.firstOrNull { peerOffer ->
                myNeedsTitles.any { myNeed -> peerOffer.lowercase().contains(myNeed) || myNeed.contains(peerOffer.lowercase()) }
            }
            val matchedOfferToPeer = peer.peerNeeds.firstOrNull { peerNeed ->
                myHavesTitles.any { myHave -> peerNeed.lowercase().contains(myHave) || myHave.contains(peerNeed.lowercase()) }
            }

            val hasMatch = matchedNeedFromPeer != null || matchedOfferToPeer != null

            val updated = peer.copy(
                hasProximityBarterMatch = hasMatch,
                matchedOffer = matchedOfferToPeer ?: peer.peerNeeds.firstOrNull(),
                matchedNeed = matchedNeedFromPeer ?: peer.peerOffers.firstOrNull()
            )

            if (hasMatch && !peer.hasProximityBarterMatch) {
                triggerTactileProximityAlert(peer.callsign, updated.matchedOffer ?: "Item", updated.matchedNeed ?: "Resource", peer.distanceEstimateMeters)
            }
            updated
        }
        _discoveredPeers.value = updatedPeers
    }

    private fun triggerTactileProximityAlert(callsign: String, offer: String, need: String, distance: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 120, 100, 200)
                val amplitudes = intArrayOf(0, 180, 0, 240)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 120, 100, 200), -1)
            }
        } catch (_: Exception) {}

        scope.launch {
            _proximityAlertEvents.emit(
                ProximityAlertEvent(
                    peerCallsign = callsign,
                    matchedOffer = offer,
                    matchedNeed = need,
                    distanceMeters = distance
                )
            )
        }
    }

    private fun calculateDistanceMeters(rssi: Int): Float {
        val txPower = -59
        if (rssi == 0) return -1.0f
        val ratio = (txPower - rssi) / (10 * 2.0)
        val distance = Math.pow(10.0, ratio).toFloat()
        return min(max(distance, 0.5f), 45.0f)
    }
}
