package com.hmman.photodecoration.ui

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hmman.photodecoration.R
import com.hmman.photodecoration.adapter.StickerAdapter
import com.hmman.photodecoration.adapter.ToolsAdapter
import com.hmman.photodecoration.model.Font
import com.hmman.photodecoration.model.Layer
import com.hmman.photodecoration.model.TextLayer
import com.hmman.photodecoration.ui.dialog.DialogSticker
import com.hmman.photodecoration.ui.dialog.EditDialogFragment
import com.hmman.photodecoration.ui.dialog.PreviewDialogFragment
import com.hmman.photodecoration.util.AnimUtil
import com.hmman.photodecoration.util.Constants
import com.hmman.photodecoration.util.FontProvider
import com.hmman.photodecoration.util.PhotoUtils
import com.hmman.photodecoration.widget.MotionView
import com.hmman.photodecoration.widget.entity.ImageEntity
import com.hmman.photodecoration.widget.entity.MotionEntity
import com.hmman.photodecoration.widget.entity.TextEntity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class MainActivity : AppCompatActivity(),
    ToolsAdapter.OnItemSelected,
    MotionView.MotionViewCallback,
    StickerAdapter.onStickerSelected {

    private lateinit var fontProvider: FontProvider
    private val PICK_IMAGE = 100
    private val CAMERA_REQUEST = 111
    private val PERMISSION_REQUEST_CODE = 999
    private val REQUEST_PERMISSION_SETTING = 888
    var isGallery = false
    var imageUri: Uri? = null
    private lateinit var stickerDialog: DialogSticker
    private lateinit var toolsAdapter: ToolsAdapter

    private lateinit var currentPhotoPath: String

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PhotoUtils.getInstance(this)
        initListener()
        eventActionTools()
        editTextToolEvent()
        enableEditMode(false)
        stickerDialog = DialogSticker(this, this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initListener() {
        fontProvider = FontProvider(resources)
        motionView.setMotionViewCallback(this)

        showTools()

        btnGallery.setOnClickListener{
            isGallery = true
            if (isStoragePermissionGranted()) {
                openGallery()
            }
            !isGallery
        }

        lnAddImage.setOnClickListener {
            isGallery = true
            if (isStoragePermissionGranted()) {
                openGallery()
            }
            !isGallery
        }

        btnUndo.setOnClickListener {
            motionView.undo()
        }

        btnRedo.setOnClickListener {
            motionView.redo()
        }

        btnReset.setOnClickListener {
            motionView.reset()
        }

        btnCamera.setOnClickListener{
            openCamera()
        }

        btnPreview.setOnClickListener {
            motionView.unselectEntity()
            val bitmap =
                Bitmap.createBitmap(
                    resultContainer.width,
                    resultContainer.height,
                    Bitmap.Config.ARGB_8888
                )
            val canvas = Canvas(bitmap)
            resultContainer.draw(canvas)
            showDialog(bitmap)
        }

        btnSave.setOnClickListener {
            savePhoto()
        }
    }

    private fun isStoragePermissionGranted() : Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
                false
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (isGallery){
                openGallery()
                !isGallery
            }
        } else {
            Snackbar.make(mainLayout, resources.getString(R.string.permission_denied), 1500).show()
            if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage(resources.getString(R.string.permission_message))
                    .setCancelable(true)
                    .setPositiveButton(resources.getString(R.string.ok)) { _: DialogInterface, _: Int ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING)
                    }
                    .setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }

                val alert = dialogBuilder.create()
                alert.setTitle(resources.getString(R.string.permission_req))
                alert.show()
            }
        }
    }

    private fun openCamera(){
//        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(cameraIntent, CAMERA_REQUEST)
        dispatchTakePictureIntent()
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    private fun savePhoto() {
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
                finalBitmap.compress(Bitmap.CompressFormat.JPEG,100, out)
                out.flush()
                out.close()
                Snackbar.make(mainLayout, resources.getString(R.string.photo_saved), 1000).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun openGallery() {
        val gallery =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST)
                }
            }
        }
    }

//    private fun setPic() {
//        // Get the dimensions of the View
//        val targetW: Int = motionView.width
//        val targetH: Int = motionView.height
//
//        val bmOptions = BitmapFactory.Options().apply {
//            // Get the dimensions of the bitmap
//            inJustDecodeBounds = true
//
//            val photoW: Int = outWidth
//            val photoH: Int = outHeight
//
//            // Determine how much to scale down the image
//            val scaleFactor: Float = min(photoW * 1.0f / targetW, photoH*1.0f / targetH)
//
//            // Decode the image file into a Bitmap sized to fill the View
//            inJustDecodeBounds = false
//            inSampleSize = scaleFactor.toInt()
//            inPurgeable = true
//        }
//
//        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
//            setMotionViewSizeAndBackground(Uri.parse(currentPhotoPath),bitmap)
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                data?.let { d ->
                    imageUri = d.data
                    try {
                        val inputStream =
                            contentResolver.openInputStream(imageUri!!)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val preventRotateBitmap = PhotoUtils.getInstance(null).rotateImageIfRequired(bitmap, imageUri!!)
                        preventRotateBitmap?.let {
                            setMotionViewSizeAndBackground(imageUri, it)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            if (requestCode == CAMERA_REQUEST) {
//                galleryAddPic()
//                setPic()
//                data?.let { d ->
//                    val bitmap = d.extras!!.get("data") as Bitmap?
//                    try {
//                        if (bitmap != null) {

//                            setMotionViewSizeAndBackground(null, bitmap)
                BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->
                    setMotionViewSizeAndBackground(Uri.parse(currentPhotoPath),bitmap)
                }
//                            setMotionViewSizeAndBackground(null, bitmap)
//                        }
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                }
            }
        }
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                openGallery()
        }
    }

    private fun enableEditMode (show: Boolean){
        when (show){
            true -> {
//                btnPreview.visibility = View.VISIBLE
//                btnSave.visibility = View.VISIBLE
//                btnReset.visibility = View.VISIBLE
//                btnRedo.visibility = View.VISIBLE
//                btnUndo.visibility = View.VISIBLE
//                rvTools.apply {
//                    visibility = View.VISIBLE
//                    animation = AnimUtil.slideUp(this@MainActivity)
//                }
//                toolsAdapter.isEnable = true
//                lnAddImage.visibility = View.INVISIBLE

                btnPreview.isEnabled = true
                btnPreview.supportBackgroundTintList = ContextCompat.getColorStateList(this, R.color.lightBlue)
                btnSave.isEnabled = true
                btnSave.supportBackgroundTintList = ContextCompat.getColorStateList(this, R.color.lightBlue)
                btnReset.isEnabled = true
                btnRedo.isEnabled = true
                btnUndo.isEnabled = true
                toolsAdapter.isEnable = true
                rvTools.adapter = toolsAdapter
                lnAddImage.visibility = View.INVISIBLE
            }
            else -> {
//                btnPreview.visibility = View.GONE
//                btnSave.visibility = View.GONE
//                btnReset.visibility = View.GONE
//                btnRedo.visibility = View.GONE
//                btnUndo.visibility = View.GONE
//                rvTools.apply {
//                    visibility = View.GONE
//                    animation = AnimUtil.slideDown(this@MainActivity)
//                }
//                toolsAdapter.isEnable = false
//                lnAddImage.visibility = View.VISIBLE

                btnPreview.isEnabled = false
                btnPreview.supportBackgroundTintList = ContextCompat.getColorStateList(this, R.color.gray)
                btnSave.isEnabled = false
                btnSave.supportBackgroundTintList = ContextCompat.getColorStateList(this, R.color.gray)
                btnReset.isEnabled = false
                btnRedo.isEnabled = false
                btnUndo.isEnabled = false
                toolsAdapter.isEnable = false
                rvTools.adapter = toolsAdapter
                lnAddImage.visibility = View.VISIBLE
            }
        }
    }

    private fun setMotionViewSizeAndBackground(uri: Uri?, bitmap: Bitmap) {
        enableEditMode(true)

        val width = dummyView.width
        val height = dummyView.height
        val photoWidth = bitmap.width
        val photoHeight = bitmap.height

        val widthAspect = 1.0f * width / photoWidth
        val heightAspect = 1.0f * height / photoHeight
        val ratio = min(widthAspect, heightAspect)
        val newWidth = photoWidth * ratio
        val newHight = photoHeight * ratio

        // set motion view width, height
        val params = motionView.layoutParams
        params.height = newHight.toInt()
        params.width = newWidth.toInt()
        motionView.layoutParams = params

        // set motion view background
        val background = BitmapDrawable(resources, bitmap)
        motionView.background = background

        // redraw motion view
        motionView.reset()
        motionView.invalidate()

        // set photo info
        if (uri != null) PhotoUtils.getInstance(null).photoUri = uri
        PhotoUtils.getInstance(null).photoRatio = ratio
        PhotoUtils.getInstance(null).width = photoWidth
        PhotoUtils.getInstance(null).height = photoHeight
    }

    private fun showTools() {
        toolsAdapter = ToolsAdapter(this)
        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTools.layoutManager = llmTools
        rvTools.adapter = toolsAdapter
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onToolSelected(toolType: ToolsAdapter.ToolType) {
        when (toolType) {
            ToolsAdapter.ToolType.TEXT -> {
                showAddTextDialog()
            }
            ToolsAdapter.ToolType.STICKER -> {
                stickerDialog.show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addText(text: String, colorCode: Int): TextLayer {
        val textLayer = createTextLayer(text, colorCode)!!
        val textEntity =
            TextEntity(textLayer, motionView.width, motionView.height, fontProvider, BitmapFactory.decodeResource(resources, R.drawable.ic_delete))

        motionView.addEntityAndPosition(textEntity)

        val center: PointF = textEntity.absoluteCenter()
        center.y = center.y * 0.5f
        textEntity.moveCenterTo(center)

        motionView.invalidate()

        return textLayer
    }

    private fun addSticker(stickerResId: Int) {
        motionView.post {
            val layer = Layer()
            val sticker = BitmapFactory.decodeResource(resources, stickerResId)
            val entity =
                ImageEntity(layer, sticker, motionView.width, motionView.height, BitmapFactory.decodeResource(resources, R.drawable.ic_delete))
            motionView.addEntityAndPosition(entity)
        }
    }

    private fun showTextEditTool(show: Boolean){
        when (show){
            true -> {
                if (currentTextEntity() != null){
                    lnTextTool.visibility = View.VISIBLE
                }
                else {
                    lnTextTool.visibility = View.INVISIBLE
                }
            }
            else -> {
                lnTextTool.visibility = View.GONE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun editTextToolEvent(){
        btnDecrease.setOnClickListener({
            decreaseTextEntitySize()
        })
        btnIncrease.setOnClickListener({
            increaseTextEntitySize()
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun increaseTextEntitySize() {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            textEntity.getLayer().font!!.increaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textEntity.updateEntity(true)
            motionView.invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun decreaseTextEntitySize() {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            textEntity.getLayer().font!!.decreaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textEntity.updateEntity(true)
            motionView.invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startTextEntityEditing() {
        val textEntity= currentTextEntity()
        val editDialog = EditDialogFragment.show(this,
            textEntity!!.getLayer().text!!,
            textEntity.getLayer().font!!.color!!)
        editDialog.setOnDoneListener(object : EditDialogFragment.TextEditor {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onDone(text: String, colorCode: Int) {
                textEntity.getLayer().text = text
                textEntity.getLayer().font!!.color = colorCode
                textEntity.updateEntity(true)
                motionView.invalidate()
            }
        })
    }

    @Nullable
    private fun currentTextEntity(): TextEntity? {
        return if (motionView != null && motionView.selectedEntity is TextEntity) {
            motionView.selectedEntity as TextEntity?
        } else {
            null
        }
    }

    private fun showDialog(bitmap: Bitmap) {
        val fragmentManager = supportFragmentManager
        val data = Bundle()
        data.putParcelable(Constants.PREVIEW_BITMAP, bitmap)
        val newFragment =
            PreviewDialogFragment()
        newFragment.arguments = data
        newFragment.show(fragmentManager, Constants.PREVIEW_DIALOG_TAG)
    }

    private fun showAddTextDialog(){
        val editDialog: EditDialogFragment = EditDialogFragment.show(this)
        editDialog.setOnDoneListener(object : EditDialogFragment.TextEditor {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onDone(text: String, colorCode: Int) {
                addText(text, colorCode)
            }
        })
    }

    private fun createTextLayer(text: String, colorCode: Int): TextLayer? {
        val textLayer = TextLayer()
        val font = Font()
        font.color = colorCode
        font.size = TextLayer.Limits.INITIAL_FONT_SIZE
        font.typeface = fontProvider.getDefaultFontName()
        textLayer.font = font
        textLayer.text = text
        return textLayer
    }

    private fun eventActionTools(){
        btnBringToFront.setOnClickListener {
            bringToFront(motionView.selectedEntity!!)
        }
        btnMoveToBack.setOnClickListener {
            moveToBack()
        }
        btnDelete.setOnClickListener {
            deleteEntity()
        }
    }

    private fun deleteEntity(){
        motionView.deletedSelectedEntity()
    }

    private fun bringToFront(entity: MotionEntity){
        motionView.bringLayerToFront(entity)
    }

    private fun moveToBack(){
        motionView.moveSelectedBack()
    }

    override fun onEntitySelected(entity: MotionEntity?) {
        actionTool.visibility = View.VISIBLE
        showTextEditTool(true)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onEntityDoubleTap(entity: MotionEntity?) {
        if (currentTextEntity() != null){
            startTextEntityEditing()
        }
    }

    override fun onEntityUnselected() {
        actionTool.visibility = View.INVISIBLE
        showTextEditTool(false)
    }

    override fun onStickerSelected(sticker: Int) {
        addSticker(sticker)
        stickerDialog.dismiss()
    }
}