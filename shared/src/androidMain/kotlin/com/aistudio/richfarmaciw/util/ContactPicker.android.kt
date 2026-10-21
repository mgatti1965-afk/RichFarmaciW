package com.aistudio.richfarmaciw.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidContactPickerLauncher(
    private val launchPicker: () -> Unit
) : ContactPickerLauncher {
    override val isAvailable: Boolean = true
    override fun launch() {
        launchPicker()
    }
}

@Composable
actual fun rememberContactPicker(
    onContactPicked: (ContactInfo) -> Unit
): ContactPickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                val contactInfo = queryContactInfo(context, uri)
                if (contactInfo != null) {
                    onContactPicked(contactInfo)
                }
            }
        }
    }

    return remember(launcher) {
        AndroidContactPickerLauncher {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            launcher.launch(intent)
        }
    }
}

private fun queryContactInfo(context: Context, uri: Uri): ContactInfo? {
    var name: String? = null
    var phone: String? = null

    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex)
                } else {
                    val altNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    if (altNameIndex >= 0) name = cursor.getString(altNameIndex)
                }

                val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (phoneIndex >= 0) {
                    phone = cursor.getString(phoneIndex)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (name.isNullOrBlank() && phone.isNullOrBlank()) {
        try {
            context.contentResolver.query(uri, arrayOf(ContactsContract.Contacts.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    name = cursor.getString(0)
                }
            }
        } catch (_: Exception) {}
    }

    return if (!name.isNullOrBlank() || !phone.isNullOrBlank()) {
        ContactInfo(
            name = name.orEmpty(),
            phoneOrEmail = phone.orEmpty()
        )
    } else null
}
