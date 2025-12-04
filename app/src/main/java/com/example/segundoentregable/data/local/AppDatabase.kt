package com.example.segundoentregable.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.segundoentregable.data.local.dao.AtractivoDao
import com.example.segundoentregable.data.local.dao.FavoritoDao
import com.example.segundoentregable.data.local.dao.ReviewDao
import com.example.segundoentregable.data.local.dao.UserDao
import com.example.segundoentregable.data.local.entity.AtractivoEntity
import com.example.segundoentregable.data.local.entity.FavoritoEntity
import com.example.segundoentregable.data.local.entity.ReviewEntity
import com.example.segundoentregable.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        AtractivoEntity::class,
        ReviewEntity::class,
        FavoritoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun atractivoDao(): AtractivoDao
    abstract fun reviewDao(): ReviewDao
    abstract fun favoritoDao(): FavoritoDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { instance = it }
            }
        }
    }
}
