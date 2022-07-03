package com.dineshkumar.pepul.task.mvvm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dineshkumar.pepul.task.model.GetFileStatus
import com.dineshkumar.pepul.task.mvvm.MainRepository
import com.dineshkumar.pepul.task.model.GetList
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class MainViewModel constructor(private val repository: MainRepository)  : ViewModel() {

    val list = MutableLiveData<GetList>()
    val deleteStatus = MutableLiveData<GetFileStatus>()
    val addStatus = MutableLiveData<GetFileStatus>()
    val errorMessage = MutableLiveData<String>()
    var job: Job? = null

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

    fun getList(map: MutableMap<String, String> = HashMap()) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val response = repository.getList(map)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val result = gson.toJson(response.body())
                    println("-- result : $result")
                    list.postValue(response.body())
                } else {
                    errorMessage.postValue(response.message())
                    println("-- result error : ${response.message()}")
                }
            }
        }
    }

    fun addFile(fileType: RequestBody, map: MultipartBody.Part) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val response = repository.addFile(fileType, map)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val result = gson.toJson(response.body())
                    println("-- result : $result")
                    addStatus.postValue(response.body())
                } else {
                    errorMessage.postValue(response.message())
                    println("-- result error : ${response.message()}")
                }
            }
        }
    }

    fun deleteFile(map: MutableMap<String, String> = HashMap()) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val response = repository.deleteFile(map)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val gson = Gson()
                    val result = gson.toJson(response.body())
                    println("-- result : $result")
                    deleteStatus.postValue(response.body())
                } else {
                    errorMessage.postValue(response.message())
                    println("-- result error : ${response.message()}")
                }
            }
        }
    }
}