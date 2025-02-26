package com.university.network.composenetwork.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/drcom/login")
    suspend fun login(
        @Query("callback") callback: String = "dr1003",
        @Query("DDDDD") username: String,
        @Query("upass") password: String,
        @Query("0MKKey") key: String = "123456",
        @Query("R1") r1: Int = 0,
        @Query("R2") r2: String = "",
        @Query("R3") r3: Int = 0,
        @Query("R6") r6: Int = 0,
        @Query("para") para: String = "00",
        @Query("v6ip") v6ip: String = "",
        @Query("terminal_type") terminalType: Int = 1,
        @Query("lang") lang: String = "zh-cn",
        @Query("jsVersion") jsVersion: String = "4.2",
        @Query("v") v: String = "2023"
    ): Response<String> // 返回类型改为String
}