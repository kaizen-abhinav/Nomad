package com.example.mesh

import android.app.Activity
import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import com.example.data.model.DigitalDeadDrop
import com.example.data.model.ScavengeStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.Charset
import java.util.UUID

sealed class NfcDropResult {
    data class ReadSuccess(val drop: DigitalDeadDrop) : NfcDropResult()
    data class WriteSuccess(val tagUid: String) : NfcDropResult()
    data class Error(val message: String) : NfcDropResult()
}

class NfcDeadDropManager(private val context: Context) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    private val _isNfcSupported = MutableStateFlow(nfcAdapter != null)
    val isNfcSupported: StateFlow<Boolean> = _isNfcSupported.asStateFlow()

    private val _isNfcEnabled = MutableStateFlow(nfcAdapter?.isEnabled == true)
    val isNfcEnabled: StateFlow<Boolean> = _isNfcEnabled.asStateFlow()

    private val _nfcEvents = MutableSharedFlow<NfcDropResult>()
    val nfcEvents: SharedFlow<NfcDropResult> = _nfcEvents.asSharedFlow()

    private var pendingDropToWrite: DigitalDeadDrop? = null

    fun enableReaderMode(activity: Activity) {
        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

        val options = Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

        adapter.enableReaderMode(
            activity,
            { tag -> handleNfcTagDiscovered(tag) },
            flags,
            options
        )
    }

    fun disableReaderMode(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }

    fun prepareWritePendingDrop(drop: DigitalDeadDrop) {
        pendingDropToWrite = drop
    }

    fun cancelPendingWrite() {
        pendingDropToWrite = null
    }

    private fun handleNfcTagDiscovered(tag: Tag) {
        val tagUidBytes = tag.id
        val tagUid = tagUidBytes?.joinToString(":") { "%02X".format(it) } ?: "UNKNOWN-TAG"

        // If user queued a write operation
        val dropToWrite = pendingDropToWrite
        if (dropToWrite != null) {
            writeDropToTag(tag, tagUid, dropToWrite)
            pendingDropToWrite = null
            return
        }

        // Otherwise read operation
        readDropFromTag(tag, tagUid)
    }

    private fun writeDropToTag(tag: Tag, tagUid: String, drop: DigitalDeadDrop) {
        try {
            val payload = encodeDeadDropToText(drop)
            val record = NdefRecord.createTextRecord("en", payload)
            val message = NdefMessage(arrayOf(record))

            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    _nfcEvents.tryEmit(NfcDropResult.Error("NFC tag is read-only."))
                    return
                }
                if (ndef.maxSize < message.byteArrayLength) {
                    _nfcEvents.tryEmit(NfcDropResult.Error("Tag capacity too small (${ndef.maxSize} bytes)."))
                    return
                }
                ndef.writeNdefMessage(message)
                ndef.close()
                _nfcEvents.tryEmit(NfcDropResult.WriteSuccess(tagUid))
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable != null) {
                    formatable.connect()
                    formatable.format(message)
                    formatable.close()
                    _nfcEvents.tryEmit(NfcDropResult.WriteSuccess(tagUid))
                } else {
                    _nfcEvents.tryEmit(NfcDropResult.Error("Tag does not support NDEF formatting."))
                }
            }
        } catch (e: Exception) {
            _nfcEvents.tryEmit(NfcDropResult.Error("Failed to write to NFC tag: ${e.localizedMessage}"))
        }
    }

    private fun readDropFromTag(tag: Tag, tagUid: String) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef == null) {
                _nfcEvents.tryEmit(NfcDropResult.Error("No NDEF payload found on this NFC tag."))
                return
            }
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()

            if (ndefMessage == null || ndefMessage.records.isEmpty()) {
                _nfcEvents.tryEmit(NfcDropResult.Error("Empty NFC tag discovered (UID: $tagUid)."))
                return
            }

            val record = ndefMessage.records[0]
            val textPayload = decodeTextRecord(record)
            val parsedDrop = parseTextToDeadDrop(textPayload, tagUid)

            _nfcEvents.tryEmit(NfcDropResult.ReadSuccess(parsedDrop))
        } catch (e: Exception) {
            _nfcEvents.tryEmit(NfcDropResult.Error("Error reading NFC tag: ${e.localizedMessage}"))
        }
    }

    private fun decodeTextRecord(record: NdefRecord): String {
        val payload = record.payload
        val languageCodeLength = payload[0].toInt() and 0x3F
        return String(
            payload,
            languageCodeLength + 1,
            payload.size - languageCodeLength - 1,
            Charset.forName("UTF-8")
        )
    }

    fun encodeDeadDropToText(drop: DigitalDeadDrop): String {
        // Tactical dead drop envelope format:
        // NOMAD_DROP|TITLE|HINT|CIPHERTEXT|PASSCODE|STATUS|COORDS|AUTHOR
        return "NOMAD_DROP|${drop.title}|${drop.landmarkHint}|${drop.encryptedPayload}|${drop.accessPasscode}|${drop.scavengeStatus.name}|${drop.coordinates}|${drop.authorCallsign}"
    }

    fun parseTextToDeadDrop(text: String, fallbackTagUid: String): DigitalDeadDrop {
        if (text.startsWith("NOMAD_DROP|")) {
            val parts = text.split("|")
            if (parts.size >= 8) {
                val status = runCatching { ScavengeStatus.valueOf(parts[5]) }.getOrDefault(ScavengeStatus.VERIFIED_INTACT)
                return DigitalDeadDrop(
                    id = "DROP-${UUID.randomUUID().toString().take(8)}",
                    tagUid = fallbackTagUid,
                    title = parts[1],
                    landmarkHint = parts[2],
                    encryptedPayload = parts[3],
                    decryptedIntel = parts[3], // Raw intel or ciphertext
                    accessPasscode = parts[4],
                    scavengeStatus = status,
                    coordinates = parts[6],
                    authorCallsign = parts[7],
                    timestamp = System.currentTimeMillis()
                )
            }
        }
        // Generic fallback if user tapped non-standard NFC tag
        return DigitalDeadDrop(
            id = "DROP-${UUID.randomUUID().toString().take(8)}",
            tagUid = fallbackTagUid,
            title = "Field Dead Drop #$fallbackTagUid",
            landmarkHint = "Tag affixed to physical marker",
            encryptedPayload = text,
            decryptedIntel = text,
            accessPasscode = "GHOST-OPEN",
            scavengeStatus = ScavengeStatus.VERIFIED_INTACT,
            coordinates = "37.7749° N, 122.4194° W",
            authorCallsign = "UNKNOWN-SURVIVOR",
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Simulated Field Tag Tap: Enables testing and offline training
     * without requiring real NFC physical stickers nearby.
     */
    fun processFieldNfcTap(sampleDrop: DigitalDeadDrop) {
        _nfcEvents.tryEmit(NfcDropResult.ReadSuccess(sampleDrop))
    }
}
