package com.university.network.composenetwork.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.university.network.composenetwork.data.UserPreferences
import com.university.network.composenetwork.data.UserPreferencesRepository
import com.university.network.composenetwork.network.LoginRepository
import com.university.network.composenetwork.network.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val userPreferences = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { preferences ->
                // 仅在首次加载时设置值，或当记住凭据为true时
                _uiState.update { currentState ->
                    // 如果当前UI状态中的字段为空或首选项中记住凭据为true，则使用首选项值
                    currentState.copy(
                        studentId = if (currentState.studentId.isBlank() || preferences.rememberCredentials)
                            preferences.studentId else currentState.studentId,
                        password = if (currentState.password.isBlank() || preferences.rememberCredentials)
                            preferences.password else currentState.password,
                        rememberCredentials = preferences.rememberCredentials
                    )
                }
            }
        }
    }

    fun onStudentIdChange(studentId: String) {
        _uiState.update { it.copy(studentId = studentId) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onRememberCredentialsChange(remember: Boolean) {
        _uiState.update { it.copy(rememberCredentials = remember) }

        // 如果用户选择记住凭据，立即保存当前输入的账号密码
        viewModelScope.launch {
            val currentState = _uiState.value
            if (remember) {
                // 保存当前输入的账号和密码
                userPreferencesRepository.saveCredentials(
                    currentState.studentId,
                    currentState.password,
                    remember
                )
            } else {
                // 只更新标志，不清除当前数据
                userPreferencesRepository.updateRememberCredentials(remember)
            }
        }
    }

    fun login() {
        val studentId = _uiState.value.studentId
        val password = _uiState.value.password
        val rememberCredentials = _uiState.value.rememberCredentials

        if (studentId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入学号") }
            return
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入密码") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = loginRepository.login(studentId, password)

            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is LoginResult.Success -> {
                    _uiState.update { it.copy(isSuccess = true, errorMessage = null) }
                    if (rememberCredentials) {
                        userPreferencesRepository.saveCredentials(studentId, password, true)
                    } else {
                        userPreferencesRepository.clearCredentials()
                    }
                }
                is LoginResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class LoginUiState(
    val studentId: String = "",
    val password: String = "",
    val rememberCredentials: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)