package com.example.feawse.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupEntry(
    val file: File,
    val originalName: String,
    val timestamp: Long,
    val sizeBytes: Long
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    val sizeFormatted: String
        get() = "${sizeBytes / 1024} KB ($sizeBytes B)"
}

object BackupManager {

    private fun getBackupDir(context: Context): File {
        val dir = File(context.filesDir, "backups")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Creates an automatic timestamped backup for safety before opening or saving.
     */
    fun createBackup(context: Context, originalName: String, data: ByteArray): File? {
        return try {
            val dir = getBackupDir(context)
            val cleanName = originalName.replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(dir, "${cleanName}_${time}.bak")
            FileOutputStream(backupFile).use { it.write(data) }
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun listBackups(context: Context): List<BackupEntry> {
        val dir = getBackupDir(context)
        val files = dir.listFiles { f -> f.extension == "bak" } ?: return emptyList()
        return files.map { file ->
            val name = file.name
            val original = name.substringBeforeLast("_").substringBeforeLast(".bak")
            BackupEntry(
                file = file,
                originalName = original,
                timestamp = file.lastModified(),
                sizeBytes = file.length()
            )
        }.sortedByDescending { it.timestamp }
    }

    fun readBackupBytes(entry: BackupEntry): ByteArray? {
        return try {
            entry.file.readBytes()
        } catch (e: Exception) {
            null
        }
    }

    fun deleteBackup(entry: BackupEntry): Boolean {
        return entry.file.delete()
    }

    fun deleteAllBackups(context: Context): Int {
        val dir = getBackupDir(context)
        val files = dir.listFiles { f -> f.extension == "bak" } ?: return 0
        var count = 0
        for (f in files) {
            if (f.delete()) count++
        }
        return count
    }
}
