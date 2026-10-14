package com.example.fadeandfaith

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Small helper for photos. A picked photo is shrunk and compressed, then
 * saved as text inside the Firestore document it belongs to, so it works
 * without any extra Firebase service. This is the ONLY file that knows how
 * photos are stored, so it can later be swapped for Firebase Storage.
 */
object ImageStore {

    fun uriToBase64(
        context: Context,
        uri: Uri,
        maxSide: Int = 800,
        startQuality: Int = 75
    ): String? {

        return try {

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }

            var sample = 1
            while (bounds.outWidth / sample > maxSide * 2 ||
                bounds.outHeight / sample > maxSide * 2
            ) {
                sample *= 2
            }

            val options = BitmapFactory.Options().apply { inSampleSize = sample }

            var bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            val rotation = context.contentResolver.openInputStream(uri)?.use {
                when (
                    ExifInterface(it).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                ) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f

            val largest = maxOf(bitmap.width, bitmap.height)
            val matrix = Matrix()

            if (largest > maxSide) {
                val scale = maxSide.toFloat() / largest
                matrix.postScale(scale, scale)
            }

            if (rotation != 0f) {
                matrix.postRotate(rotation)
            }

            if (!matrix.isIdentity) {
                bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
            }

            var quality = startQuality
            var text: String

            do {
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                text = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                quality -= 10
            } while (text.length > 600_000 && quality > 20)

            text

        } catch (e: Exception) {
            null
        }
    }

    fun base64ToBitmap(text: String?): Bitmap? {

        if (text.isNullOrEmpty()) return null

        return try {
            val bytes = Base64.decode(text, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}