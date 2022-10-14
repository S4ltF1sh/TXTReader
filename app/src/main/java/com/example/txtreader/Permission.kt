package com.example.txtreader

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION

class Permission {
    companion object {
        fun isGrantedManageExternalStoragePermission(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                false
            }
        }

        fun grantManageExternalStorePermission(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.startActivity(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        }
    }
}
