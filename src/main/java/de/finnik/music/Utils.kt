package de.finnik.music

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ThumbnailStore(application: Application) {
    val dir = File(application.filesDir, "thumbnails")

    init {
        if (!dir.isDirectory) {
            dir.mkdir()
        }
    }

    fun exists(id: String): Boolean {
        return File(dir, id).exists()
    }

    fun store(bitmap: Bitmap, id: String) {
        val file = File(dir, id)
        file.createNewFile()
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.WEBP, 90, fos)
        fos.close()
    }

    fun load(id: String): Bitmap {
        val file = File(dir, id)
        val fin = FileInputStream(file)
        return BitmapFactory.decodeStream(fin)
    }
}