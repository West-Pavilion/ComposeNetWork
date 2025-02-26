package com.university.network.composenetwork.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val studentIdKey = stringPreferencesKey("student_id")
    private val passwordKey = stringPreferencesKey("password")
    private val rememberCredentialsKey = stringPreferencesKey("remember_credentials")

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                studentId = preferences[studentIdKey] ?: "",
                password = preferences[passwordKey] ?: "",
                rememberCredentials = preferences[rememberCredentialsKey]?.toBoolean() ?: false
            )
        }

    suspend fun updateStudentId(studentId: String) {
        context.dataStore.edit { preferences ->
            preferences[studentIdKey] = studentId
        }
    }

    suspend fun updatePassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[passwordKey] = password
        }
    }

    suspend fun updateRememberCredentials(remember: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[rememberCredentialsKey] = remember.toString()

            // 如果取消记住凭据，不要立即清除数据
            // 只有在登录成功后或明确清除时才清除
        }
    }

    suspend fun saveCredentials(studentId: String, password: String, remember: Boolean) {
        context.dataStore.edit { preferences ->
            // 无论remember标志如何，都保存当前凭据到首选项
            // (这样我们可以保持UI状态的一致性)
            preferences[studentIdKey] = studentId
            preferences[passwordKey] = password
            preferences[rememberCredentialsKey] = remember.toString()
        }
    }

    // 专门用于清除凭据的方法，只在登出或用户明确要求时调用
    suspend fun clearCredentials() {
        context.dataStore.edit { preferences ->
            preferences.remove(studentIdKey)
            preferences.remove(passwordKey)
            preferences[rememberCredentialsKey] = false.toString()
        }
    }
}

data class UserPreferences(
    val studentId: String = "",
    val password: String = "",
    val rememberCredentials: Boolean = false
)