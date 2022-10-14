package com.example.txtreader

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.txtreader.databinding.ActivityMainBinding
import com.example.txtreader.dialogs.SaveFileDialog
import com.example.txtreader.utils.SoftKeyBoardUtil
import com.example.txtreader.utils.UriUtils
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.io.*


class MainActivity : AppCompatActivity(), SaveFileDialog.SaveButtonListener {
    private lateinit var binding: ActivityMainBinding
    private var isShowing = false //true if speed dial is showing
    private var isEditing = false //true if app is in editing mode
    private var isLongPressed = false //true if call longClick
    private var isNewFile = true //true if no file opened
    private var openedFilePath: String? = null
    private var filePathForSavingNewFile: String? =
        null //file path after create a new file with ACTION_CREATE_DOCUMENT

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        //OPEN file TEXT with this app:
        if (Intent.ACTION_VIEW == intent.action && intent.type != null) {
            handleViewText()
        }

        binding.appCompatEditText.apply {
            isEnabled = false
        }

        hideEFABs()

        setOnClick()

        setOnLongClick()

        setOnTouch()
    }

    private fun handleViewText() {
        val uri = intent.data
        openedFilePath = UriUtils.getPathFromUri(this, uri)

        binding.appCompatEditText.setText(getTextFromDocument(uri!!))
    }


    //onClick Action:
    private fun setOnClick() {
        binding.apply {
            floatingActionButton.setOnClickListener {
                floatingActionButtonClicked("Click")
            }

            extendedFloatingActionOpenButton.setOnClickListener {
                openFileButtonClicked()
            }

            extendedFloatingActionSaveButton.setOnClickListener {
                saveFileButtonClicked()
            }

            extendedFloatingActionEditButton.setOnClickListener {
                editModeButtonClicked()
            }

            extendedFloatingActionReadButton.setOnClickListener {
                readModeButtonClicked()
            }
        }
    }

    private fun readModeButtonClicked() {
        binding.appCompatEditText.isEnabled = false
        isEditing = false
        isShowing = false
        hideEFABs()
    }

    private fun editModeButtonClicked() {
        binding.appCompatEditText.apply {
            isEnabled = true
            requestFocus()
            setSelection(this.length()) //move cursor to end of the document
        }

        isEditing = true
        isShowing = false
        hideEFABs()

        SoftKeyBoardUtil.showSoftKeyboard(this)
    }

    private fun saveFileButtonClicked() {
        if (Permission.isGrantedManageExternalStoragePermission())
            if (!isNewFile) {
                openedFilePath?.let { updateDocument(it) }
            } else {
                SaveFileDialog(this).show(supportFragmentManager, "Save File Dialog")
            }
        else
            Permission.grantManageExternalStorePermission(this)
    }

    private fun openFileButtonClicked() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION and Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        intent.type = "text/plain"

        setTextToEditTextView.launch(intent)

        if (isEditing) {
            readModeButtonClicked()
            showEFABs()
        }
    }

    private fun floatingActionButtonClicked(author: String) {
        //EFABs - Extended Floating Action Button
        Log.d("btn:", "Call by $author")
        if (!isShowing) {
            showEFABs()
        } else {
            hideEFABs()
        }
    }

    //onLongClick Action:
    private fun setOnLongClick() {
        binding.apply {
            floatingActionButton.setOnLongClickListener {
                floatingActionButtonLongClicked()
            }
        }
    }

    private fun floatingActionButtonLongClicked(): Boolean {
        Log.d("long press:", "Call")
        floatingActionButtonClicked("LongClick")

        isLongPressed = true

        return true //return true để tránh gọi cả onCLick() nếu nhấn giữ nút mà không drag
    }

    //onTouch Action:
    @SuppressLint("ClickableViewAccessibility")
    private fun setOnTouch() {
        binding.floatingActionButton.apply {
            //Cần return false để tránh bị nhầm với onClick và onLongClick
            setOnTouchListener { _, event ->
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()

                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        val childCount = binding.myLayout.childCount
                        //cần kiểm tra xem đấy có phải là sự kiện LongPress không

                        for (i in 0..childCount) {
                            val current = binding.myLayout.getChildAt(i)
                            if (current is ExtendedFloatingActionButton) {
                                current.isPressed = isViewInBounds(current, x, y)
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        //Log.d("isShowing:", "$isShowing")

                        val childCount = binding.myLayout.childCount
                        //cần kiểm tra xem đấy có phải là sự kiện LongPress không
                        if (isLongPressed && isShowing) {
                            for (i in 0..childCount) {
                                val current = binding.myLayout.getChildAt(i)
                                if (current is ExtendedFloatingActionButton) {
                                    if (isViewInBounds(current, x, y)) {
                                        current.isPressed = true
                                        current.performClick()
                                        current.isPressed = false
                                    }
                                }
                            }

                            isLongPressed = false
                        }
                    }
                    else -> {
                        super.onTouchEvent(event)
                    }

                }

                false
            }
        }
    }

    //Check if view child contain cursor:
    private var outRect: Rect = Rect()
    private var location = IntArray(2)
    private fun isViewInBounds(view: View, x: Int, y: Int): Boolean {
        view.getDrawingRect(outRect)
        view.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(x, y)
    }

    //Show and hide EFABs:
    private fun hideEFABs() {
        binding.apply {
            extendedFloatingActionOpenButton.apply {
                shrink()
                hide()
            }

            extendedFloatingActionSaveButton.apply {
                shrink()
                hide()
            }

            extendedFloatingActionEditButton.apply {
                shrink()
                hide()
            }

            extendedFloatingActionReadButton.apply {
                shrink()
                hide()
            }

            isShowing = false
        }
    }

    private fun showEFABs() {
        binding.apply {
            extendedFloatingActionOpenButton.apply {
                show()
                extend()
            }

            extendedFloatingActionSaveButton.apply {
                show()
                extend()
            }

            if (isEditing)
                extendedFloatingActionReadButton.apply {
                    show()
                    extend()
                }
            else
                extendedFloatingActionEditButton.apply {
                    show()
                    extend()
                }

            isShowing = true
        }
    }

    //Activity Result API:
    private val setTextToEditTextView =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val uri = intent?.data
                openedFilePath = UriUtils.getPathFromUri(this, uri)
                binding.appCompatEditText.setText(getTextFromDocument(uri!!))
            }
        }

    private val writeTextToNewFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val uri = intent?.data

                filePathForSavingNewFile = UriUtils.getPathFromUri(this, uri)

                filePathForSavingNewFile?.let {
                    updateDocument(it)
                }
            }
        }


    //Action after button clicked:
    private fun getTextFromDocument(uri: Uri): String {
        val stringBuilder = StringBuilder()

        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }

            this.isNewFile = false
        }
        return stringBuilder.toString()
    }

    private fun updateDocument(path: String) {
        Log.i("Tag", "uri: $path")
        Log.i("Tag", "path: $path")
        try {
            val writer = FileOutputStream(File(path))
            writer.write(binding.appCompatEditText.text.toString().toByteArray())
            writer.close()

            Toast.makeText(this, "Save Successfully!", Toast.LENGTH_LONG).show()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun createNewDocument(documentName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TITLE, documentName)
        writeTextToNewFile.launch(intent)
    }
}