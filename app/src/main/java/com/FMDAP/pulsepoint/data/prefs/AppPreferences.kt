package com.FMDAP.pulsepoint.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pulsepoint_prefs")

class AppPreferences(private val context: Context) {
    companion object {
        private val SERVER_URL      = stringPreferencesKey("server_url")
        private val API_KEY         = stringPreferencesKey("api_key")
        private val MANAGE_PASSWORD = stringPreferencesKey("manage_password")
    }

    val serverUrl: Flow<String>      = context.dataStore.data.map { it[SERVER_URL] ?: "" }
    val apiKey: Flow<String>         = context.dataStore.data.map { it[API_KEY] ?: "" }
    val managePassword: Flow<String> = context.dataStore.data.map { it[MANAGE_PASSWORD] ?: "" }

    suspend fun saveServerUrl(url: String)         = context.dataStore.edit { it[SERVER_URL] = url }
    suspend fun saveApiKey(key: String)            = context.dataStore.edit { it[API_KEY] = key }
    suspend fun saveManagePassword(pw: String)     = context.dataStore.edit { it[MANAGE_PASSWORD] = pw }
}
