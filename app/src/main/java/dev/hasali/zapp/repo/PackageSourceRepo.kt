package dev.hasali.zapp.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import dev.hasali.zapp.db.Database
import dev.hasali.zapp.db.PackageSource

class PackageSourceRepo(private val db: Database) {

    companion object {
        private val ZAPP_PACKAGE_SOURCE = PackageSource(
            id = -1,
            name = "Zapp",
            url = "https://github.com/hasali19/zapp/releases/download/latest/zapp.apk.json",
        )
    }

    fun getPackageSources(): LiveData<List<PackageSource>> {
        return db.packageSourceDao().getAll().map { listOf(ZAPP_PACKAGE_SOURCE) + it }
    }

    fun getPackageSource(id: Int): LiveData<PackageSource> =
        if (id == ZAPP_PACKAGE_SOURCE.id) {
            MutableLiveData(ZAPP_PACKAGE_SOURCE)
        } else {
            db.packageSourceDao().getById(id)
        }
}