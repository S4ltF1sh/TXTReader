package com.example.txtreader.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.txtreader.MainActivity
import com.example.txtreader.R

class SaveFileDialog(context: Context) : DialogFragment() {

    interface SaveButtonListener {
        fun createNewDocument(documentName: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(R.layout.textfield_for_dialog)
                .setTitle("Do you want to save your texts?")
                .setPositiveButton("SAVE", DialogInterface.OnClickListener(saveButtonClicked))
                .setNegativeButton("CANCEL", DialogInterface.OnClickListener(cancelButtonClicked))
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private val cancelButtonClicked =
        { dialog: DialogInterface, _: Int -> dialog.dismiss() }

    private val saveButtonClicked =
        { _: DialogInterface, _: Int ->

            val saveButtonListener: SaveButtonListener = context as MainActivity

            val documentName = dialog?.findViewById<EditText>(R.id.edtFileName)

            saveButtonListener.createNewDocument(
                documentName?.text.toString()
            )
        }
}