package dev.hasali.zapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PackageSourceDao {
    @Query("SELECT * FROM PackageSource")
    fun getAll(): LiveData<List<PackageSource>>

    @Query("SELECT * FROM PackageSource WHERE id = :id")
    fun getById(id: Int): LiveData<PackageSource>

    @Insert
    fun insertAll(vararg sources: PackageSource)
}
