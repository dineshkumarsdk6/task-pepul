package com.dineshkumar.pepul.task.retrofit

import com.dineshkumar.pepul.task.model.GetFileStatus
import com.dineshkumar.pepul.task.model.GetList
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface RetrofitApiInterFace {

    @POST("select.php")
    @FormUrlEncoded
    suspend fun getList(@FieldMap defaultData: MutableMap<String, String>): Response<GetList>

    @Multipart
    @POST("uploader.php")
    suspend fun addFile(
        @Part("file_type ") action: RequestBody?,
        @Part file: MultipartBody.Part?
    ): Response<GetFileStatus>

    @POST("delete.php")
    @FormUrlEncoded
    suspend fun deleteFile(@FieldMap defaultData: MutableMap<String, String>): Response<GetFileStatus>

}