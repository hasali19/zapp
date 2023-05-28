package dev.hasali.zapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackageSourceManifest(
    val version: Int,
    val name: String,
    val packages: List<Package>,
) {
    @Serializable
    data class Package(
        val name: String,
        val packageName: String,
        val icon: String?,
        val url: String?,
        val version: String?,
        val versionCode: Int,
        val files: List<File>,
    )

    @Serializable
    data class File(
        val name: String,
        val abi: Abi,
        val url: String,
    )

    @Serializable
    enum class Abi {
        @SerialName("arm64-v8a")
        Arm64V8a,

        @SerialName("armeabi-v7a")
        ArmeabiV7a,

        @SerialName("x86_64")
        X86_64;

        override fun toString() = when(this) {
            Arm64V8a -> "arm64-v8a"
            ArmeabiV7a -> "armeabi-v7a"
            X86_64 -> "x86_64"
        }
    }
}