package com.hmman.photodecoration.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import java.io.IOException

class PhotoUtils private constructor(val activity: Activity) {
    var photoRatio: Float = 1.0f
    var width: Int = 0
    var height: Int = 0
    var photoUri: Uri = Uri.EMPTY

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg =
            Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    @Throws(IOException::class)
    fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap? {
        val ei = ExifInterface(getRealPathFromURI(selectedImage))
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(
                img,
                90
            )
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(
                img,
                180
            )
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(
                img,
                270
            )
            else -> img
        }
    }

    fun getRealPathFromURI(contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor = activity.managedQuery(contentUri, proj, null, null, null)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    companion object : SingletonHolder<PhotoUtils, Activity>(::PhotoUtils)
}