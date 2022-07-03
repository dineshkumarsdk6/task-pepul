package com.dineshkumar.pepul.task.support

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.widget.Toast
import com.dineshkumar.pepul.task.databinding.LayoutLoadingBinding

object AppUtils {

    const val text_net_check = "Check your internet connection"

    lateinit var dialogLoading: Dialog

    fun dialogLoading(context: Context?, title: String?, boolean: Boolean): Dialog {
        dialogLoading = Dialog(
            context!!
        )

        val binding: LayoutLoadingBinding = LayoutLoadingBinding.inflate(LayoutInflater.from(context))

        dialogLoading.run { setContentView(binding.root) }
        if (dialogLoading.window != null) {
            dialogLoading.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        binding.title.text = title.toString()
        dialogLoading.setCancelable(boolean)
        dialogLoading.setCanceledOnTouchOutside(boolean)
        return dialogLoading
    }

    fun showToast(context: Context?, message: String) {
        Toast.makeText(context, "" + message, Toast.LENGTH_SHORT).show()
    }

    fun checkNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

}