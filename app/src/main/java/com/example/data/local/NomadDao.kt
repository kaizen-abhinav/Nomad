package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BarterItem
import com.example.data.model.DigitalDeadDrop
import com.example.data.model.EmergencyBroadcast
import kotlinx.coroutines.flow.Flow

@Dao
interface NomadDao {

    // Emergency Broadcasts & Hazard Map updates (Ghost Relay)
    @Query("SELECT * FROM emergency_broadcasts ORDER BY timestamp DESC")
    fun getAllBroadcasts(): Flow<List<EmergencyBroadcast>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBroadcast(broadcast: EmergencyBroadcast)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBroadcasts(broadcasts: List<EmergencyBroadcast>)

    @Query("DELETE FROM emergency_broadcasts WHERE id = :id")
    suspend fun deleteBroadcast(id: String)

    // Barter Ledger (Have & Need)
    @Query("SELECT * FROM barter_items ORDER BY timestamp DESC")
    fun getAllBarterItems(): Flow<List<BarterItem>>

    @Query("SELECT * FROM barter_items WHERE isOffering = 1 ORDER BY timestamp DESC")
    fun getOfferedItems(): Flow<List<BarterItem>>

    @Query("SELECT * FROM barter_items WHERE isOffering = 0 ORDER BY timestamp DESC")
    fun getNeededItems(): Flow<List<BarterItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarterItem(item: BarterItem)

    @Update
    suspend fun updateBarterItem(item: BarterItem)

    @Delete
    suspend fun deleteBarterItem(item: BarterItem)

    // Digital Dead Drops (NFC)
    @Query("SELECT * FROM digital_dead_drops ORDER BY timestamp DESC")
    fun getAllDeadDrops(): Flow<List<DigitalDeadDrop>>

    @Query("SELECT * FROM digital_dead_drops WHERE tagUid = :tagUid LIMIT 1")
    suspend fun getDeadDropByTagUid(tagUid: String): DigitalDeadDrop?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeadDrop(drop: DigitalDeadDrop)

    @Delete
    suspend fun deleteDeadDrop(drop: DigitalDeadDrop)
}
