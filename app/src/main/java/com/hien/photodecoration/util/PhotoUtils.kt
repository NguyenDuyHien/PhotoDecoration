package com.hien.photodecoration.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.hien.photodecoration.R
import com.hien.photodecoration.widget.MotionView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotoUtils private constructor(val activity: Activity) {
    var photoRatio: Float = 1.0f
    var width: Int = 0
    var height: Int = 0
    var photoUri: Uri = Uri.EMPTY
    var boundsWidth: Float = 0f

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

    fun savePhoto(context: Context, view: View, motionView: MotionView) {
        val finalBitmap = motionView.getFinalBitmap()
        finalBitmap?.let {
            val root = Environment.getExternalStorageDirectory().absolutePath
            val myDir = File("$root/PhotoDecoration")
            myDir.mkdirs()

            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fname = "Photo$timeStamp.jpg"
            val file = File(myDir, fname)
            if (file.exists()) file.delete()
            try {
                val out = FileOutputStream(file)
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
                Snackbar.make(view, context.resources.getString(R.string.photo_saved), 1000).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object : SingletonHolder<PhotoUtils, Activity>(::PhotoUtils)
}