package com.saimanidevi.pomotimer.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.saimanidevi.pomotimer.utils.Constants.SESSION_KEY
import com.saimanidevi.pomotimer.utils.Constants.STATE_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val PREFERENCE_NAME = "pomo_preference"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)

class DataStoreRepository(private val context: Context) {
    private object PreferenceKeys {
        val sessionKey = intPreferencesKey(SESSION_KEY)
        val stateKey = stringPreferencesKey(STATE_KEY)
    }

    private val sessionKey = intPreferencesKey(SESSION_KEY)
    private val pomodoroKey = stringPreferencesKey(STATE_KEY)

    suspend fun saveToDataStore(session: Int, state: String) {
        Log.d("TAG", "REPO - saveToDataStore: CALLED ")
        context.dataStore.edit { preference ->
            preference[sessionKey] = session
            preference[pomodoroKey] = state
        }
    }

    val savedSessionFlow: Flow<Int> = context.dataStore.data
        .map { preference ->
            preference[sessionKey] ?: 1
        }

    val savedStateFlow: Flow<String> = context.dataStore.data
        .map { preference ->
            preference[pomodoroKey] ?: "pomodoro"
        }
}