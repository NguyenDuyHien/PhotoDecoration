package com.hmman.photodecoration.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hmman.photodecoration.R
import com.hmman.photodecoration.adapter.StickerAdapter
import com.hmman.photodecoration.adapter.ToolsAdapter
import com.hmman.photodecoration.model.Font
import com.hmman.photodecoration.model.Layer
import com.hmman.photodecoration.model.TextLayer
import com.hmman.photodecoration.util.FontProvider
import com.hmman.photodecoration.widget.MotionView
import com.hmman.photodecoration.widget.entity.ImageEntity
import com.hmman.photodecoration.widget.entity.MotionEntity
import com.hmman.photodecoration.widget.entity.TextEntity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity(),
    ToolsAdapter.OnItemSelected,
    MotionView.MotionViewCallback,
    StickerAdapter.onStickerSelected {

    private lateinit var fontProvider: FontProvider
    private val PICK_IMAGE = 100
    private val CAMERA_REQUEST = 111
    var imageUri: Uri? = null
    private lateinit var stickerDialog: DialogSticker

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListener()
        eventActionTools()
        stickerDialog = DialogSticker(this, this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initListener() {
        fontProvider = FontProvider(resources)
        motionView.setMotionViewCallback(this)


        showTools()

        btnGallery.setOnClickListener{
            openGallery()
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
                    containerResult.width,
                    containerResult.height,
                    Bitmap.Config.ARGB_8888
                )
            val canvas = Canvas(bitmap)
            containerResult.draw(canvas)
            showDialog(bitmap)
        }

        editText.setOnKeyListener(View.OnKeyListener { v, i, keyEvent ->
            if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER) && !editText.text.isNullOrEmpty()) {
                hideKeyboard(v)
                val textLayer = addText(editText.text.toString())
                val textEntity =
                    TextEntity(textLayer, motionView.width, motionView.height, fontProvider)
                motionView.addEntityAndPosition(textEntity)

                val center: PointF = textEntity.absoluteCenter()
                center.y = center.y * 0.5f
                textEntity.moveCenterTo(center)

                motionView.invalidate()

                startTextEntityEditing()
                editText.visibility = EditText.INVISIBLE
                editText.setText("")
                return@OnKeyListener true
            }
            false
        })

    }

    private fun openCamera(){
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
    }

    private fun openGallery() {
        val gallery =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            data?.let { data ->
                imageUri = data.data
                // imageView.setImageURI(imageUri);
                try { //c1
                    val inputStream =
                        contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    addImage(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST){
            data?.let {
                val photo = data?.extras!!.get("data") as Bitmap?
                var bitmap = BitmapDrawable(photo)
                motionView.setBackgroundDrawable(bitmap)
            }
        }
    }

    private fun hideKeyboard(v: View) {
        val inputMethodManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.applicationWindowToken, 0)
    }

    private fun addImage(imgResId: Bitmap) {
//        val editingImage = AppCompatImageView(this)
//        editingImage.setImageResource(imgResId)
//        motionView.addView(editingImage)
        imgEdit.setImageBitmap(imgResId)

    }

    private fun showTools() {
        val toolsAdapter = ToolsAdapter(this)
        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTools.layoutManager = llmTools
        rvTools.adapter = toolsAdapter
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onToolSelected(toolType: ToolsAdapter.ToolType) {
        when (toolType) {
            ToolsAdapter.ToolType.TEXT -> {
                editText.visibility = EditText.VISIBLE
//                showEditText("")
            }
            ToolsAdapter.ToolType.STICKER -> {
                stickerDialog.show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addText(text: String): TextLayer {
        val textLayer = createTextLayer(text)!!
        val textEntity =
            TextEntity(textLayer, motionView.width, motionView.height, fontProvider)

        val center: PointF = textEntity.absoluteCenter()
        center.y = center.y * 0.5f
        textEntity.moveCenterTo(center)

        motionView.invalidate()

        startTextEntityEditing()
        return textLayer
    }

    private fun addSticker(stickerResId: Int) {
        motionView.post {
            val layer = Layer()
            val pica = BitmapFactory.decodeResource(resources, stickerResId)
            val entity =
                ImageEntity(layer, pica, motionView.width, motionView.height)
            motionView.addEntityAndPosition(entity)
        }
    }

    private fun startTextEntityEditing() {
//        val textEntity: TextEntity = currentTextEntity()!!
//        if (textEntity != null) {
//            val fragment: TextEditorDialogFragment =
//                TextEditorDialogFragment.getInstance(textEntity.getLayer().getText())
//            fragment.show(fragmentManager, TextEditorDialogFragment::class.java.getName())
//        }
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
        data.putParcelable("bitmap", bitmap)
        val newFragment = PreviewDialogFragment()
        newFragment.arguments = data
        newFragment.show(fragmentManager, "dialog")
    }

    private fun showEditText(text: String){
        val fragmentManager = supportFragmentManager
        val data = Bundle()
        data.putString("content", text)
        val editFragment = EditDialog()
        editFragment.arguments = data
        editFragment.show(fragmentManager, "edit")
    }

    private fun createTextLayer(text: String): TextLayer? {
        val textLayer = TextLayer()
        var font = Font()
        font.color = Color.DKGRAY
        font.size = TextLayer.Limits.INITIAL_FONT_SIZE
        font.typeface = fontProvider.getDefaultFontName()
        textLayer.font = font
        textLayer.text = text
        return textLayer
    }

    private fun eventActionTools(){
        btnBringToFront.setOnClickListener({
            bringToFront(motionView.selectedEntity!!)
        })
        btnMoveToBack.setOnClickListener({
            moveToBack()
        })
        btnDelete.setOnClickListener({
            deleteEntity()
        })
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
    }

    override fun onEntityDoubleTap(entity: MotionEntity?) {
        //Nothing
    }

    override fun onEntityUnselected() {
        actionTool.visibility = View.INVISIBLE
    }

    override fun onStickerSelected(sticker: Int) {
        addSticker(sticker)
//        stickerDialog.dismiss()
    }
}