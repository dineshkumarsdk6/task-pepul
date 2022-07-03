package com.dineshkumar.pepul.task.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GetFileStatus {

    @SerializedName("statusCode")
    @Expose
    val statusCode: Int? = null

    @SerializedName("message")
    @Expose
    val message: String? = null

    @SerializedName("data")
    @Expose
    val data: List<Datum>? = null

    class Datum {
        @SerializedName("result")
        @Expose
        var result: String? = null

        @SerializedName("file_type")
        @Expose
        var fileType: Int? = null
    }
}