package dev.hasali.zapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import java.io.IOException
import java.io.InputStream

class AppInstaller(private val context: Context) {
    fun install(inputStream: InputStream) {
        var session: PackageInstaller.Session? = null
        try {
            val installer = context.packageManager.packageInstaller
            val params =
                PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

            val sessionId = installer.createSession(params)

            session = installer.openSession(sessionId)
            session.openWrite("package", 0, -1).use { output ->
                inputStream.copyTo(output)
                session.fsync(output)
            }

            var flags = PendingIntent.FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = flags or PendingIntent.FLAG_MUTABLE
            }

            val intent = Intent(context, InstallReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 3439, intent, flags)
            val receiver = pendingIntent.intentSender

            session.commit(receiver)
            session.close()
        } catch (e: IOException) {
            throw RuntimeException("Couldn't install package", e)
        } catch (e: RuntimeException) {
            session?.abandon()
            throw e
        }
    }
}