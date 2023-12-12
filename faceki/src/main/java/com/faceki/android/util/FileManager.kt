package com.faceki.android.util

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileManager {

    private var application: Application? = null

    fun initialize(application: Application) {
        this.application = application
    }


    private const val folderName = "FaceKiImages"

    // Ensure the folder exists before performing any operation
    private fun ensureFolderExists(): File {
        val folder = File(application!!.getExternalFilesDir(null), folderName)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return folder
    }

    // Method to save an image from InputStream to the folder
    suspend fun saveImage(inputStream: InputStream, fileName: String): File =
        withContext(Dispatchers.IO) {
            val folder = ensureFolderExists()
            val file = File(folder, fileName)

            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            return@withContext file
        }

    // Method to delete a specific file
    fun deleteSpecificFile(fileName: String): Boolean {
        val folder = ensureFolderExists()
        val file = File(folder, fileName)
        return if (file.exists()) file.delete() else false
    }

    // Method to delete all files in the folder
    fun deleteAllFiles(): Boolean {
        val folder = ensureFolderExists()
        return folder.listFiles()?.all { it.delete() } ?: false
    }

    // Method to save an image captured by CameraX to the folder
    suspend fun saveCapturedImage(imageBytes: ByteArray, fileName: String): File = withContext(
        Dispatchers.IO
    ) {
        val folder = ensureFolderExists()
        val file = File(folder, fileName)

        FileOutputStream(file).use { output ->
            output.write(imageBytes)
        }

        return@withContext file
    }

}