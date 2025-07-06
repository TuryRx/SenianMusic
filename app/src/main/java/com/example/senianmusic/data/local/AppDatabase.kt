package com.example.senianmusic.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.senianmusic.data.local.dao.SongDao
import com.example.senianmusic.data.local.model.OfflineSong

// 1. Anota la clase con @Database
@Database(
    entities = [OfflineSong::class], // 2. Lista todas tus entidades aquí
    version = 1, // 3. Incrementa la versión si cambias el esquema
    exportSchema = false // Puedes ponerlo a true para guardar el historial del esquema
)
abstract class AppDatabase : RoomDatabase() {

    // 4. Declara un método abstracto para cada DAO que tengas
    abstract fun songDao(): SongDao

    // 5. Crea un "singleton" para tener una sola instancia de la base de datos en toda la app
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia no es nula, la retorna.
            // Si es nula, crea la base de datos.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "senian_music_database" // Nombre del archivo de la base de datos
                ).build()
                INSTANCE = instance
                // retorna la instancia
                instance
            }
        }
    }
}