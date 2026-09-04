package com.comp7506.mywardrobe.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File.createTempFile("camera_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

/**
 * 将URI指向的图片复制到应用内部永久存储空间
 */
fun copyUriToInternalStorage(context: Context, uri: Uri, folderName: String = "wardrobe_items"): String? {
    return try {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
        val folder = File(context.filesDir, folderName).apply { mkdirs() }
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val destFile = File(folder, fileName)
        
        destFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        destFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 压缩图片
 * @param bitmap 原始Bitmap
 * @param maxWidth 最大宽度
 * @param maxHeight 最大高度
 * @param maxQuality 质量 0-100
 * @return 压缩后的Bitmap
 */
fun compressBitmap(
    bitmap: Bitmap,
    maxWidth: Int = 1024,
    maxHeight: Int = 1024,
    maxQuality: Int = 85
): Bitmap {
    // 计算压缩比例
    var width = bitmap.width
    var height = bitmap.height

    if (width > maxWidth || height > maxHeight) {
        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        width = (width * ratio).toInt()
        height = (height * ratio).toInt()
    }

    // 创建压缩后的Bitmap
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

/**
 * 保存Bitmap到文件
 * @param bitmap 要保存的Bitmap
 * @param file 目标文件
 * @param quality 质量 0-100
 */
fun saveBitmapToFile(bitmap: Bitmap, file: File, quality: Int = 85) {
    file.outputStream().use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    }
}
