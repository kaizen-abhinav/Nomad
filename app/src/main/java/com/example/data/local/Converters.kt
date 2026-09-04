package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.HazardType
import com.example.data.model.ResourceCategory
import com.example.data.model.ScavengeStatus
import com.example.data.model.ThreatLevel

class Converters {
    @TypeConverter
    fun fromThreatLevel(value: ThreatLevel?): String = value?.name ?: ThreatLevel.INFO.name

    @TypeConverter
    fun toThreatLevel(value: String?): ThreatLevel =
        value?.let { runCatching { ThreatLevel.valueOf(it) }.getOrDefault(ThreatLevel.INFO) } ?: ThreatLevel.INFO

    @TypeConverter
    fun fromHazardType(value: HazardType?): String = value?.name ?: HazardType.GRID_BLACKOUT.name

    @TypeConverter
    fun toHazardType(value: String?): HazardType =
        value?.let { runCatching { HazardType.valueOf(it) }.getOrDefault(HazardType.GRID_BLACKOUT) } ?: HazardType.GRID_BLACKOUT

    @TypeConverter
    fun fromResourceCategory(value: ResourceCategory?): String = value?.name ?: ResourceCategory.RATIONS.name

    @TypeConverter
    fun toResourceCategory(value: String?): ResourceCategory =
        value?.let { runCatching { ResourceCategory.valueOf(it) }.getOrDefault(ResourceCategory.RATIONS) } ?: ResourceCategory.RATIONS

    @TypeConverter
    fun fromScavengeStatus(value: ScavengeStatus?): String = value?.name ?: ScavengeStatus.UNKNOWN.name

    @TypeConverter
    fun toScavengeStatus(value: String?): ScavengeStatus =
        value?.let { runCatching { ScavengeStatus.valueOf(it) }.getOrDefault(ScavengeStatus.UNKNOWN) } ?: ScavengeStatus.UNKNOWN
}
