package se.simmarken.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalePreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(
        dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(DATA_STORE_NAME)
        },
    )

    val mode: Flow<LanguageMode> = dataStore.data.map { preferences ->
        LanguageMode.fromStoredValue(preferences[LANGUAGE_MODE_KEY])
    }

    suspend fun setMode(mode: LanguageMode) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_MODE_KEY] = mode.name
        }
    }

    companion object {
        private const val DATA_STORE_NAME = "locale_preferences"
        private val LANGUAGE_MODE_KEY = stringPreferencesKey("language_mode")
    }
}
