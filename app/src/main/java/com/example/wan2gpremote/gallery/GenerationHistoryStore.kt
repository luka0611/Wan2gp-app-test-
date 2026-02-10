package com.example.wan2gpremote.gallery

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.galleryDataStore by preferencesDataStore("gallery_history")

class GenerationHistoryStore(private val context: Context) {

    val records: Flow<List<GenerationRecord>> = context.galleryDataStore.data.map { prefs ->
        decodeRecords(prefs[HISTORY_KEY].orEmpty())
    }

    suspend fun addRecord(record: GenerationRecord) {
        context.galleryDataStore.edit { prefs ->
            val current = decodeRecords(prefs[HISTORY_KEY].orEmpty())
            prefs[HISTORY_KEY] = encodeRecords(listOf(record) + current)
        }
    }

    private fun encodeRecords(records: List<GenerationRecord>): String {
        val array = JSONArray()
        records.forEach { record ->
            array.put(
                JSONObject()
                    .put("id", record.id)
                    .put("model", record.model)
                    .put("mode", record.mode)
                    .put("prompt", record.prompt)
                    .put("outputType", record.outputType.name)
                    .put("localPathOrUri", record.localPathOrUri)
                    .put("createdAt", record.createdAt)
                    .put("serverJobId", record.serverJobId)
            )
        }
        return array.toString()
    }

    private fun decodeRecords(raw: String): List<GenerationRecord> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        GenerationRecord(
                            id = item.optString("id"),
                            model = item.optString("model"),
                            mode = item.optString("mode"),
                            prompt = item.optString("prompt"),
                            outputType = runCatching { OutputType.valueOf(item.optString("outputType")) }
                                .getOrDefault(OutputType.IMAGE),
                            localPathOrUri = item.optString("localPathOrUri"),
                            createdAt = item.optLong("createdAt"),
                            serverJobId = item.optString("serverJobId"),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private val HISTORY_KEY = stringPreferencesKey("generation_history_json")
    }
}
