package dev.hasali.zapp.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PackageSource::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun packageSourceDao(): PackageSourceDao
}