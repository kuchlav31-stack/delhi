package com.dark.delhi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

private fun uriToCompressedByteArray(context: android.content.Context, uri: android.net.Uri): ByteArray {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    val outputStream = ByteArrayOutputStream()

    // Quality 75 dating apps ke liye perfect hai (Size chota, Quality mast)
    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
    return outputStream.toByteArray()
}