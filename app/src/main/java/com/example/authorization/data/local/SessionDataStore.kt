package com.example.authorization.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.authorization.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "auth_session")

object SessionDataStore {

    private lateinit var appContext: Context

    private val KEY_USER_ID = longPreferencesKey("user_id")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_COMPANY = stringPreferencesKey("company_name")

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun dataFlow(): Flow<User?> = appContext.sessionDataStore.data.map { prefs ->
        val id = prefs[KEY_USER_ID] ?: return@map null
        if (id <= 0) return@map null
        val email = prefs[KEY_EMAIL] ?: return@map null
        User(id = id, email = email, companyName = prefs[KEY_COMPANY] ?: "")
    }

    suspend fun get(): User? = dataFlow().first()

    fun observe(): Flow<User?> = dataFlow()

    suspend fun save(user: User) {
        appContext.sessionDataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_EMAIL] = user.email
            prefs[KEY_COMPANY] = user.companyName
        }
    }

    suspend fun clear() {
        appContext.sessionDataStore.edit { it.clear() }
    }
}