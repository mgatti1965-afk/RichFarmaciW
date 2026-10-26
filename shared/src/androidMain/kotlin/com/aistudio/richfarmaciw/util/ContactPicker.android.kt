package com.aistudio.richfarmaciw.util

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
    val currentOnContactPicked by rememberUpdatedState(onContactPicked)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                val contactInfo = queryContactInfo(context, uri)
                if (contactInfo != null) {
                    currentOnContactPicked(contactInfo)
                } else {
                    Toast.makeText(context, "Nessuna anagrafica o numero trovato nel contatto selezionato", Toast.LENGTH_SHORT).show()
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

    val contentResolver = context.contentResolver

    try {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex("display_name")
                    .takeIf { it >= 0 } ?: cursor.getColumnIndex("display_name_alt")
                if (nameIdx >= 0) {
                    name = cursor.getString(nameIdx)
                }

                val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    .takeIf { it >= 0 } ?: cursor.getColumnIndex("data1")
                if (phoneIdx >= 0) {
                    phone = cursor.getString(phoneIdx)
                }

                if (phone.isNullOrBlank()) {
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (idIdx >= 0) {
                        val contactId = cursor.getString(idIdx)
                        if (!contactId.isNullOrBlank()) {
                            phone = queryPhoneNumberByContactId(contentResolver, contactId)
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    if (phone.isNullOrBlank()) {
        val lastSegment = uri.lastPathSegment
        if (!lastSegment.isNullOrBlank()) {
            phone = queryPhoneNumberByContactId(contentResolver, lastSegment)
        }
    }

    val cleanPhone = phone?.replace("\\s+".toRegex(), "") ?: ""

    return if (!name.isNullOrBlank() || cleanPhone.isNotBlank()) {
        ContactInfo(
            name = name.orEmpty().trim(),
            phoneOrEmail = cleanPhone
        )
    } else null
}

private fun queryPhoneNumberByContactId(contentResolver: ContentResolver, contactId: String): String? {
    try {
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )?.use { phoneCursor ->
            if (phoneCursor.moveToFirst()) {
                return phoneCursor.getString(0)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
