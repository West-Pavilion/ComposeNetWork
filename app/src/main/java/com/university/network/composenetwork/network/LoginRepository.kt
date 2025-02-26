package com.university.network.composenetwork.network

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson
) {
    private val TAG = "LoginRepository"

    suspend fun login(studentId: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            // 添加移动运营商后缀
            val usernameWithSuffix = "$studentId@cmcc"

            val response = apiService.login(username = usernameWithSuffix, password = password)

            if (response.isSuccessful) {
                val responseBody = response.body() ?: ""
                Log.d(TAG, "原始响应: $responseBody")

                // 解析JSONP响应，提取JSON部分
                val jsonContent = extractJsonFromJsonp(responseBody)
                Log.d(TAG, "提取的JSON: $jsonContent")

                if (jsonContent != null) {
                    try {
                        // 解析JSON获取result字段
                        val loginResponse = gson.fromJson(jsonContent, LoginResponse::class.java)
                        if (loginResponse.result == 1) {
                            return@withContext LoginResult.Success
                        } else {
                            return@withContext LoginResult.Error("登录失败，请检查账号和密码")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "JSON解析错误", e)
                        return@withContext LoginResult.Error("解析响应失败: ${e.message}")
                    }
                } else {
                    Log.e(TAG, "无法从JSONP提取JSON内容: $responseBody")
                    return@withContext LoginResult.Error("无法解析服务器响应")
                }
            } else {
                Log.e(TAG, "请求失败: ${response.code()}")
                return@withContext LoginResult.Error("网络请求失败：${response.code()}")
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "连接超时", e)
            LoginResult.Error("连接校园网超时，请检查网络连接")
        } catch (e: UnknownHostException) {
            Log.e(TAG, "无法解析主机名", e)
            LoginResult.Error("无法连接到认证服务器，请确保已连接校园WiFi")
        } catch (e: Exception) {
            Log.e(TAG, "登录出错", e)
            LoginResult.Error("登录过程中出现错误：${e.message}")
        }
    }

    /**
     * 从JSONP响应中提取JSON部分
     * 例如：从 dr1003({"result":1,...}) 提取出 {"result":1,...}
     */
    private fun extractJsonFromJsonp(jsonp: String): String? {
        // 匹配第一个左括号和最后一个右括号之间的内容
        val regex = "\\((.*)\\)".toRegex()
        val matchResult = regex.find(jsonp)
        return matchResult?.groupValues?.getOrNull(1)
    }
}

// 添加响应数据类
data class LoginResponse(
    val result: Int = 0,
    val aolno: Int = 0,
    val v46ip: String? = null,
    val uid: String? = null,
    // 其他字段可以根据需要添加
)

sealed class LoginResult {
    object Success : LoginResult()
    data class Error(val message: String) : LoginResult()
}