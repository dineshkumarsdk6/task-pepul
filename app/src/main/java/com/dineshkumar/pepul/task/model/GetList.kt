package com.dineshkumar.pepul.task.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GetList {

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
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("file")
        @Expose
        var file: String? = null

        @SerializedName("file_type")
        @Expose
        var fileType: String? = null
    }
}