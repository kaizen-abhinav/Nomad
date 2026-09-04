package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ThreatLevel {
    INFO,
    WARNING,
    CRITICAL
}

enum class HazardType {
    QUARANTINE_ZONE,
    RADIATION_FALLOUT,
    FLOODED_PASS,
    SAFE_HAVEN,
    CLEAN_WATER_POINT,
    GRID_BLACKOUT,
    MILITIA_CHECKPOINT,
    MEDICAL_STATION
}

@Entity(tableName = "emergency_broadcasts")
data class EmergencyBroadcast(
    @PrimaryKey
    val id: String,
    val originCallsign: String,
    val content: String,
    val threatLevel: ThreatLevel,
    val hazardType: HazardType,
    val timestamp: Long,
    val hopsCount: Int,
    val carrierPigeonRoute: String, // e.g. "NODE-7 -> COURIER-41 -> YOU"
    val isEncrypted: Boolean = true,
    val signatureHash: String, // e.g. "SHA-e8f9c2..."
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationDescription: String = ""
)
