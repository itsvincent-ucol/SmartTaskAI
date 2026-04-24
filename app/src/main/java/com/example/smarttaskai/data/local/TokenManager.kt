package com.example.smarttaskai.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Membuat  DataStore bernama "user_prefs"
private val Context.dataStore by preferencesDataStore(name = "user_prefs")


class TokenManager(private val context: Context) {
    companion object {
        // Kunci JWT yang didapat setelah user selesai login "jwt_token"
        private val JWT_TOKEN_KEY = stringPreferencesKey("jwt_token")
    }

    // Fungsi menyimpan token dan dipanggil AuthViewModel saat sukses login
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    // Fungsi menghapus token
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN_KEY)
        }
    }
}