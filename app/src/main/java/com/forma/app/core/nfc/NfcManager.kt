package com.forma.app.core.nfc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NfcManager @Inject constructor() {

    fun isNfcAvailable(context: Context): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter != null
    }

    fun isNfcEnabled(context: Context): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(context)
        return adapter?.isEnabled == true
    }

    fun createHabitUri(habitId: String, startFocus: Boolean = false): String {
        return if (startFocus) {
            "forma://focus/$habitId"
        } else {
            "forma://habit/$habitId"
        }
    }

    fun parseHabitUri(uriString: String?): Pair<String, Boolean>? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "forma" || uri.scheme == "habitflow") {
                val host = uri.host ?: ""
                val id = uri.lastPathSegment ?: ""
                if (id.isNotBlank()) {
                    Pair(id, host == "focus")
                } else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun writeHabitTag(tag: Tag, habitId: String, startFocus: Boolean = false): Result<Unit> {
        return try {
            val uriString = createHabitUri(habitId, startFocus)
            val uriRecord = NdefRecord.createUri(Uri.parse(uriString))
            val appRecord = NdefRecord.createApplicationRecord("com.forma.app")
            val message = NdefMessage(arrayOf(uriRecord, appRecord))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    return Result.failure(IllegalStateException("Tag is read-only"))
                }
                if (ndef.maxSize < message.byteArrayLength) {
                    return Result.failure(IllegalStateException("Tag capacity too small"))
                }
                ndef.writeNdefMessage(message)
                ndef.close()
                Result.success(Unit)
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable != null) {
                    formatable.connect()
                    formatable.format(message)
                    formatable.close()
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalStateException("Tag does not support NDEF formatting"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
