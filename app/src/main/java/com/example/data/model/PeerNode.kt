package com.example.data.model

data class PeerNode(
    val nodeId: String,
    val callsign: String,
    val rssi: Int, // Signal strength in dBm (-30 to -95)
    val distanceEstimateMeters: Float,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val connectionType: String = "BLE Ghost Relay", // "BLE Ghost Relay", "Wi-Fi Direct", "Carrier Contact"
    val carriedPacketsCount: Int = 3,
    val peerOffers: List<String> = emptyList(), // What peer has
    val peerNeeds: List<String> = emptyList(),   // What peer needs
    val hasProximityBarterMatch: Boolean = false,
    val matchedOffer: String? = null,
    val matchedNeed: String? = null
)
