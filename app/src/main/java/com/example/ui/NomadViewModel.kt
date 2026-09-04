package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.NomadDatabase
import com.example.data.model.BarterItem
import com.example.data.model.DigitalDeadDrop
import com.example.data.model.EmergencyBroadcast
import com.example.data.model.HazardType
import com.example.data.model.PeerNode
import com.example.data.model.ResourceCategory
import com.example.data.model.ScavengeStatus
import com.example.data.model.ThreatLevel
import com.example.data.repository.NomadRepository
import com.example.mesh.ApkBeamerManager
import com.example.mesh.GhostMeshManager
import com.example.mesh.NfcDeadDropManager
import com.example.mesh.NfcDropResult
import com.example.mesh.ProximityAlertEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class NomadViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NomadDatabase.getDatabase(application)
    private val repository = NomadRepository(database.nomadDao())

    val meshManager = GhostMeshManager(application)
    val nfcManager = NfcDeadDropManager(application)
    val apkBeamer = ApkBeamerManager(application)

    val localCallsign = meshManager.localCallsign

    // Reactive database flows
    val allBroadcasts: StateFlow<List<EmergencyBroadcast>> = repository.allBroadcasts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBarterItems: StateFlow<List<BarterItem>> = repository.allBarterItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offeredItems: StateFlow<List<BarterItem>> = repository.offeredItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val neededItems: StateFlow<List<BarterItem>> = repository.neededItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeadDrops: StateFlow<List<DigitalDeadDrop>> = repository.allDeadDrops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mesh status
    val discoveredPeers: StateFlow<List<PeerNode>> = meshManager.discoveredPeers
    val isGhostSyncActive: StateFlow<Boolean> = meshManager.isGhostSyncActive
    val relayedBroadcastsCount: StateFlow<Int> = meshManager.relayedBroadcastsCount
    val carrierPigeonHopsTotal: StateFlow<Int> = meshManager.carrierPigeonHopsTotal

    // Active Proximity Alert
    private val _activeProximityAlert = MutableStateFlow<ProximityAlertEvent?>(null)
    val activeProximityAlert: StateFlow<ProximityAlertEvent?> = _activeProximityAlert.asStateFlow()

    // Status / Feedback message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Cached APK file for Beamer
    private val _preparedApkFile = MutableStateFlow<File?>(null)
    val preparedApkFile: StateFlow<File?> = _preparedApkFile.asStateFlow()

    init {
        // Populate initial survival emergency broadcasts and barter items if first run
        viewModelScope.launch {
        }

        // Listen for proximity barter alerts
        viewModelScope.launch {
            meshManager.proximityAlertEvents.collect { alert ->
                _activeProximityAlert.value = alert
            }
        }

        // Listen for NFC events
        viewModelScope.launch {
            nfcManager.nfcEvents.collect { result ->
                when (result) {
                    is NfcDropResult.ReadSuccess -> {
                        repository.insertDeadDrop(result.drop)
                        _toastMessage.value = "NFC Dead Drop Read: ${result.drop.title}"
                    }
                    is NfcDropResult.WriteSuccess -> {
                        _toastMessage.value = "Dead Drop successfully written to NFC tag (${result.tagUid})"
                    }
                    is NfcDropResult.Error -> {
                        _toastMessage.value = "NFC Error: ${result.message}"
                    }
                }
            }
        }

        // Prepare APK in background for instantaneous zero-state beaming
        viewModelScope.launch {
            _preparedApkFile.value = apkBeamer.prepareNomadApkFile()
        }
    }

    fun dismissProximityAlert() {
        _activeProximityAlert.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun toggleGhostSync(enabled: Boolean) {
        meshManager.toggleGhostSync(enabled)
    }


    // Barter ledger methods
    fun addBarterItem(
        isOffering: Boolean,
        category: ResourceCategory,
        title: String,
        quantity: String,
        condition: String,
        notes: String
    ) {
        viewModelScope.launch {
            val item = BarterItem(
                id = "BARTER-${UUID.randomUUID().toString().take(8)}",
                isOffering = isOffering,
                category = category,
                title = title,
                quantity = quantity,
                condition = condition,
                notes = notes
            )
            repository.insertBarterItem(item)
            _toastMessage.value = if (isOffering) "Listed in HAVE inventory" else "Added to NEED requirements"
            meshManager.checkBarterMatches(offeredItems.value, neededItems.value)
        }
    }

    fun removeBarterItem(item: BarterItem) {
        viewModelScope.launch {
            repository.deleteBarterItem(item)
        }
    }

    // Broadcasts
    fun publishBroadcast(
        content: String,
        threatLevel: ThreatLevel,
        hazardType: HazardType,
        locationDescription: String
    ) {
        viewModelScope.launch {
            val broadcast = EmergencyBroadcast(
                id = "BC-${UUID.randomUUID().toString().take(8)}",
                originCallsign = localCallsign,
                content = content,
                threatLevel = threatLevel,
                hazardType = hazardType,
                timestamp = System.currentTimeMillis(),
                hopsCount = 1,
                carrierPigeonRoute = "$localCallsign -> BROADCAST",
                isEncrypted = true,
                signatureHash = "SHA256-${UUID.randomUUID().toString().take(6)}",
                locationDescription = locationDescription
            )
            repository.insertBroadcast(broadcast)
            _toastMessage.value = "Broadcast queued for background Ghost Sync couriers"
        }
    }

    // Digital Dead Drops
    fun addDeadDrop(
        title: String,
        landmarkHint: String,
        secretIntel: String,
        passcode: String,
        status: ScavengeStatus,
        coordinates: String
    ) {
        viewModelScope.launch {
            val drop = DigitalDeadDrop(
                id = "DROP-${UUID.randomUUID().toString().take(8)}",
                tagUid = "NFC-${UUID.randomUUID().toString().take(5).uppercase()}",
                title = title,
                landmarkHint = landmarkHint,
                encryptedPayload = "ENC[AES256]:$secretIntel",
                decryptedIntel = secretIntel,
                accessPasscode = passcode,
                scavengeStatus = status,
                coordinates = coordinates.ifBlank { "37.7749° N, 122.4194° W" },
                authorCallsign = localCallsign
            )
            repository.insertDeadDrop(drop)
            nfcManager.prepareWritePendingDrop(drop)
            _toastMessage.value = "Dead Drop created. Ready to write to NFC tag or field deploy."
        }
    }

    fun processTagTap(drop: DigitalDeadDrop) {
        nfcManager.processFieldNfcTap(drop)
    }

    fun createBluetoothBeamIntent(): Intent? {
        val file = _preparedApkFile.value ?: apkBeamer.prepareNomadApkFile()
        return if (file != null) {
            val uri = apkBeamer.getApkContentUri(file)
            apkBeamer.createBluetoothBeamIntent(uri)
        } else {
            null
        }
    }

}
