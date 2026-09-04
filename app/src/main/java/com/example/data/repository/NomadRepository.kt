package com.example.data.repository

import com.example.data.local.NomadDao
import com.example.data.model.BarterItem
import com.example.data.model.DigitalDeadDrop
import com.example.data.model.EmergencyBroadcast
import kotlinx.coroutines.flow.Flow

class NomadRepository(private val dao: NomadDao) {

    // Emergency Broadcasts
    val allBroadcasts: Flow<List<EmergencyBroadcast>> = dao.getAllBroadcasts()

    suspend fun insertBroadcast(broadcast: EmergencyBroadcast) {
        dao.insertBroadcast(broadcast)
    }

    suspend fun insertBroadcasts(broadcasts: List<EmergencyBroadcast>) {
        dao.insertBroadcasts(broadcasts)
    }

    suspend fun deleteBroadcast(id: String) {
        dao.deleteBroadcast(id)
    }

    // Barter Ledger
    val allBarterItems: Flow<List<BarterItem>> = dao.getAllBarterItems()
    val offeredItems: Flow<List<BarterItem>> = dao.getOfferedItems()
    val neededItems: Flow<List<BarterItem>> = dao.getNeededItems()

    suspend fun insertBarterItem(item: BarterItem) {
        dao.insertBarterItem(item)
    }

    suspend fun updateBarterItem(item: BarterItem) {
        dao.updateBarterItem(item)
    }

    suspend fun deleteBarterItem(item: BarterItem) {
        dao.deleteBarterItem(item)
    }

    // Digital Dead Drops
    val allDeadDrops: Flow<List<DigitalDeadDrop>> = dao.getAllDeadDrops()

    suspend fun getDeadDropByTagUid(tagUid: String): DigitalDeadDrop? {
        return dao.getDeadDropByTagUid(tagUid)
    }

    suspend fun insertDeadDrop(drop: DigitalDeadDrop) {
        dao.insertDeadDrop(drop)
    }

    suspend fun deleteDeadDrop(drop: DigitalDeadDrop) {
        dao.deleteDeadDrop(drop)
    }
}
