package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ScavengeStatus(val label: String) {
    VERIFIED_INTACT("Verified Intact"),
    DEPLETED("Depleted / Empty"),
    UNKNOWN("Unverified"),
    COMPROMISED("Caution / Hostile Presence")
}

@Entity(tableName = "digital_dead_drops")
data class DigitalDeadDrop(
    @PrimaryKey
    val id: String,
    val tagUid: String, // NFC Tag UID or virtual UID
    val title: String,
    val landmarkHint: String, // Physical location instruction (e.g., "Under rusty bridge pier #4")
    val encryptedPayload: String, // Ciphertext
    val decryptedIntel: String, // Decrypted content
    val accessPasscode: String, // Tactical key
    val scavengeStatus: ScavengeStatus,
    val coordinates: String, // e.g., "37.7749° N, 122.4194° W"
    val authorCallsign: String,
    val timestamp: Long = System.currentTimeMillis()
)
