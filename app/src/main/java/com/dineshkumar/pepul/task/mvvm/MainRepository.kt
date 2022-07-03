package com.dineshkumar.pepul.task.mvvm

import com.dineshkumar.pepul.task.model.GetFileStatus
import com.dineshkumar.pepul.task.model.GetList
import com.dineshkumar.pepul.task.retrofit.RetrofitApiInterFace
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.util.*

class MainRepository(private val retrofitService: RetrofitApiInterFace) {

    suspend fun getList(map: MutableMap<String, String> = HashMap()) : Response<GetList> {
        return retrofitService.getList(map)
    }

    suspend fun addFile(fileType: RequestBody, file: MultipartBody.Part) : Response<GetFileStatus> {
        return retrofitService.addFile(fileType, file)
    }

    suspend fun deleteFile(map: MutableMap<String, String> = HashMap()) : Response<GetFileStatus> {
        return retrofitService.deleteFile(map)
    }

}