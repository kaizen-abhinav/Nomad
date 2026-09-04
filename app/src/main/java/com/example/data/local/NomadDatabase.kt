package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.BarterItem
import com.example.data.model.DigitalDeadDrop
import com.example.data.model.EmergencyBroadcast

@Database(
    entities = [
        EmergencyBroadcast::class,
        BarterItem::class,
        DigitalDeadDrop::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NomadDatabase : RoomDatabase() {
    abstract fun nomadDao(): NomadDao

    companion object {
        @Volatile
        private var INSTANCE: NomadDatabase? = null

        fun getDatabase(context: Context): NomadDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NomadDatabase::class.java,
                    "nomad_survival_node.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
