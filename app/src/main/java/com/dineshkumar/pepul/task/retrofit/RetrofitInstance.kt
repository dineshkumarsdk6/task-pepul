package com.dineshkumar.pepul.task.retrofit

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    var BASE_URL = "https://nextgenerationsocialnetwork.com/user_details/"

    fun getRetrofitInstance(): Retrofit? {
        val client = OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES).addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                            .build()
                    chain.proceed(newRequest)
                }.build()
        val gson = GsonBuilder()
                .setLenient()
                .create()
        var retrofit: Retrofit? = null
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .client(client)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
        }
        return retrofit
    }

}