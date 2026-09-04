package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ResourceCategory(val displayName: String, val iconKey: String) {
    MEDICAL("Medical Supplies", "medical"),
    WATER("Clean Water", "water"),
    RATIONS("Rations / MREs", "food"),
    FUEL("Fuel / Gasoline", "fuel"),
    BATTERIES("Power / Solar / Batteries", "power"),
    COMMS("Radio / Comms", "radio"),
    TOOLS("Tools & Hardware", "tools"),
    AMMO("Defense & Security", "shield")
}

@Entity(tableName = "barter_items")
data class BarterItem(
    @PrimaryKey
    val id: String,
    val isOffering: Boolean, // true = HAVE (Possess), false = NEED (Desperately need)
    val category: ResourceCategory,
    val title: String,
    val quantity: String,
    val condition: String, // e.g. "Factory Sealed", "Good", "Used"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val active: Boolean = true
)
