package com.university.network.composenetwork.network

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class StringConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        return if (type === String::class.java) {
            Converter<ResponseBody, String> { body -> body.string() }
        } else {
            null
        }
    }
}