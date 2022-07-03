package com.dineshkumar.pepul.task.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dineshkumar.pepul.task.R
import com.dineshkumar.pepul.task.databinding.LayoutAddFilesBinding
import com.dineshkumar.pepul.task.databinding.LayoutInfoDialogBinding
import com.dineshkumar.pepul.task.mvvm.MainRepository
import com.dineshkumar.pepul.task.mvvm.MyViewModelFactory
import com.dineshkumar.pepul.task.mvvm.MainViewModel
import com.dineshkumar.pepul.task.retrofit.RetrofitApiInterFace
import com.dineshkumar.pepul.task.retrofit.RetrofitInstance
import com.dineshkumar.pepul.task.support.AppUtils
import com.dineshkumar.pepul.task.support.FilePath
import com.google.gson.Gson
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.*

class ActivityAddFiles : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    lateinit var binding: LayoutAddFilesBinding
    //file picker
    var dataList: ArrayList<Uri> = ArrayList()
    var MAX_ATTACHMENT_COUNT = 1
    var file_type = ""
    lateinit var viewModel: MainViewModel

    companion object {
        var arrayListClass = ArrayList<HashMap<String, Any>>()
        var filePaths: ArrayList<Uri> = ArrayList()
        var customListAdapter: RecyclerViewAdapter? = null

        fun deleteFile(context: Context, i: Int, file_name: String) {
            val dialog = Dialog(
                context,
                android.R.style.Theme_DeviceDefault_Dialog_MinWidth
            )
            val bindingDia: LayoutInfoDialogBinding =
                LayoutInfoDialogBinding.inflate(LayoutInflater.from(context))
            dialog.setContentView(bindingDia.root)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))

            bindingDia.run {
                textTitle.text = "Do you want to delete this file?"
                textYes.text = context.getString(R.string.text_yes)
                textNo.text = context.getString(R.string.text_no)
            }

            bindingDia.textYes.setOnClickListener {
                dialog.dismiss()
                delete(context, file_name)
                dialog.dismiss()
                arrayListClass.removeAt(i)
                filePaths.removeAt(i)
                customListAdapter!!.notifyItemRemoved(i)
            }
            bindingDia.textNo.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }

        private fun delete(context: Context, file_name: String) {
            if (EasyPermissions.hasPermissions(context, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
                try {
                    val file = File(
                        context.filesDir,
                        "Pepul/Files/$file_name"
                    )
                    if (file.exists()) {
                        file.delete()
                    }

                    val folder = File("" + context.filesDir + "/Pepul")
                    if (folder.exists()) {
                        folder.delete()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutAddFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arrayListClass.clear()
        filePaths.clear()

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Add Files"

        val apiInterface =
            RetrofitInstance.getRetrofitInstance()!!.create(RetrofitApiInterFace::class.java)
        viewModel = ViewModelProvider(this, MyViewModelFactory(MainRepository(apiInterface))).get(
            MainViewModel::class.java
        )
        viewModel.addStatus.observe(this, Observer {
            val gson = Gson()
            val result = gson.toJson(it)
            println("-- data output : $result")

            AppUtils.dialogLoading.dismiss()
            if (it.message == "success"){
                AppUtils.showToast(this@ActivityAddFiles, "File uploaded successfully")
                val intent = Intent()
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                AppUtils.showToast(this@ActivityAddFiles, "File upload failed")
            }

        })
        viewModel.errorMessage.observe(this, Observer {
            AppUtils.showToast(this@ActivityAddFiles, it.toString())
        })

        binding.cardUpload.setOnClickListener {
            if (AppUtils.checkNetworkAvailable(this@ActivityAddFiles)) {
                upload()
            } else {
                AppUtils.showToast(this@ActivityAddFiles,AppUtils.text_net_check )
            }
        }

        binding.recyclerView.isExpanded = true
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 1)
        customListAdapter = RecyclerViewAdapter(
            this@ActivityAddFiles,
            arrayListClass
        )
        binding.recyclerView.adapter = customListAdapter

        binding.cardPhoto.setOnClickListener {
            pickFile(0)
        }
        binding.cardVideo.setOnClickListener {
            pickFile(1)
        }
    }

    private fun upload() {
        if (arrayListClass.size == 0) {
            AppUtils.showToast(this@ActivityAddFiles, "Select file to upload")
            return
        }
        AppUtils.dialogLoading(this@ActivityAddFiles, "Loading...", false).show()

        val selectedFilePath: String = FilePath.getPath(this@ActivityAddFiles, filePaths[0])!!
        val uploadFile = File(selectedFilePath)

        // create RequestBody instance from file
        val requestFile = RequestBody.create(
            MediaType.parse(contentResolver.getType(filePaths[0])),
            uploadFile
        )

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("fileToUpload", uploadFile.name, requestFile)
        val fileType  = RequestBody.create(MultipartBody.FORM, "" + file_type)

        viewModel.addFile(fileType, body)

    }


    @AfterPermissionGranted(FilePickerConst.REQUEST_CODE_PHOTO)
    fun pickFile(i: Int) {
        if (EasyPermissions.hasPermissions(this, FilePickerConst.PERMISSIONS_FILE_PICKER)) {
            onPick(i)
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                this@ActivityAddFiles,
                "We require storage permission to select photo from gallery and camera.",
                FilePickerConst.REQUEST_CODE_PHOTO,
                FilePickerConst.PERMISSIONS_FILE_PICKER
            )
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun onPick(i: Int) {
        if (filePaths.size == MAX_ATTACHMENT_COUNT) {
            AppUtils.showToast(
                this@ActivityAddFiles,
                "Cannot select more than $MAX_ATTACHMENT_COUNT file"
            )
            return
        }

        if (i == 0) {
            FilePickerBuilder.instance
                .setMaxCount(1)
                .setSelectedFiles(filePaths) //this is optional
                .setActivityTheme(R.style.FilePickerTheme)
                .setActivityTitle("Select Image")
                .setImageSizeLimit(5)
                .setVideoSizeLimit(10)
                .setSpan(FilePickerConst.SPAN_TYPE.FOLDER_SPAN, 3)
                .setSpan(FilePickerConst.SPAN_TYPE.DETAIL_SPAN, 4)
                .enableVideoPicker(false)
                .enableCameraSupport(true)
                .showGifs(true)
                .showFolderView(true)
                .enableSelectAll(false)
                .enableImagePicker(true)
                .setCameraPlaceholder(R.drawable.image_camera)
                .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .pickPhoto(this, 100)
        }

        if (i == 1) {
            FilePickerBuilder.instance
                .setMaxCount(1)
                .setSelectedFiles(filePaths) //this is optional
                .setActivityTheme(R.style.FilePickerTheme)
                .setActivityTitle("Select Video")
                .setImageSizeLimit(5)
                .setVideoSizeLimit(200)
                .setSpan(FilePickerConst.SPAN_TYPE.FOLDER_SPAN, 3)
                .setSpan(FilePickerConst.SPAN_TYPE.DETAIL_SPAN, 4)
                .enableVideoPicker(true)
                .enableCameraSupport(true)
                .showGifs(false)
                .showFolderView(false)
                .enableSelectAll(false)
                .enableImagePicker(false)
                .enableDocSupport(false)
                .setCameraPlaceholder(R.drawable.image_camera)
                .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .pickPhoto(this, 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // onPickPhoto()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> if (resultCode == RESULT_OK && data != null) {
                dataList =
                    data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)!!
                if (dataList.size != 0) {
                    filePaths = ArrayList()
                    filePaths.addAll(dataList)
                    val file_name = ContentUriUtils.getFilePath(this, dataList[0]).toString().substring(
                        ContentUriUtils.getFilePath(
                            this,
                            dataList[0]
                        ).toString().lastIndexOf("/") + 1
                    ).trim()

                    println("--file uri : $dataList[0]")
                    println(
                        "--file path : " + ContentUriUtils.getFilePath(
                            this,
                            dataList[0]
                        )
                    )
                    println("--file name : $file_name")

                    val file = File(
                        ContentUriUtils.getFilePath(
                            this,
                            dataList[0]
                        )!!
                    )
                    val file_size: Int = java.lang.String.valueOf(file.length() / 1024).toInt()
                    println("--file_size : $file_size")
                    checkFile(file_size, file_name, "0")
                }
            }
            101 -> if (resultCode == RESULT_OK && data != null) {
                val dataList =
                    data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                if (dataList != null && dataList.size != 0) {
                    filePaths = ArrayList()
                    filePaths.addAll(dataList)
                    val file_name = ContentUriUtils.getFilePath(this, dataList[0]).toString().substring(
                        ContentUriUtils.getFilePath(
                            this,
                            dataList[0]
                        ).toString().lastIndexOf("/") + 1
                    ).trim()

                    println("--file uri : $dataList[0]")
                    println("--file path : " + ContentUriUtils.getFilePath(this, dataList[0]))
                    println("--file name : $file_name")
                    val file = File(
                        ContentUriUtils.getFilePath(
                            this,
                            dataList[0]
                        )!!
                    )
                    println("--file_length : ${file.length()}")

                    val file_size: Int = java.lang.String.valueOf(file.length() / 1024).toInt()
                    println("--file_size : $file_size")
                    checkFile(file_size, file_name, "1")

                }
            }
        }
    }

    private fun checkFile(fileSize: Int, fileName: String, type: String) {
        file_type = type
        var fileFormat = ""
        if (type == "0"){
            fileFormat = ".jpg"
        } else {
            fileFormat = ".mp4"
        }
        if (fileSize < 200000){
            if (fileName.endsWith(fileFormat)){
                getFilePathFromURI(this, filePaths[0], fileName)!!
                val temp = HashMap<String, Any>()
                temp["file_name"] = fileName
                arrayListClass.add(0, temp)
                customListAdapter!!.notifyDataSetChanged()
            } else {
                AppUtils.showToast(this@ActivityAddFiles, "Warning! $fileFormat format only acceptable")
                filePaths.clear()
            }
        } else {
            AppUtils.showToast(this@ActivityAddFiles, "Warning! maximum 200 MB allowed")
            filePaths.clear()
        }
    }

    private fun getFilePathFromURI(context: Context, contentUri: Uri?, name: String): String? {
        //copy file and send new file pathcontentUri
        // String fileName = "";
        val folder = File("$filesDir/Pepul")
        val directory =
            File("$filesDir/Pepul/Files")
        // have the object build the directory structure, if needed.
        if (!folder.exists()) {
            folder.mkdirs()
        }
        if (!directory.exists()) {
            directory.mkdirs()
        }
        if (!TextUtils.isEmpty(name)) {
            val copyFile = File(directory.toString() + File.separator + name)
            // create folder if not exists
            copy(context, contentUri, copyFile)
            return copyFile.absolutePath
        }
        return null
    }

    private fun copy(context: Context, srcUri: Uri?, dstFile: File?) {
        try {
            val inputStream = context.contentResolver.openInputStream(srcUri!!) ?: return
            val outputStream: OutputStream = FileOutputStream(dstFile)
            copystream(inputStream, outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    @Throws(java.lang.Exception::class, IOException::class)
    fun copystream(input: InputStream?, output: OutputStream?): Int {
        val BUFFER_SIZE = 1024 * 2
        val buffer = ByteArray(BUFFER_SIZE)
        val `in` = BufferedInputStream(input, BUFFER_SIZE)
        val out = BufferedOutputStream(output, BUFFER_SIZE)
        var count = 0
        var n = 0
        try {
            while (`in`.read(buffer, 0, BUFFER_SIZE).also { n = it } != -1) {
                out.write(buffer, 0, n)
                count += n
            }
            out.flush()
        } finally {
            try {
                out.close()
            } catch (e: IOException) {
                Log.e(e.message, e.toString())
            }
            try {
                `in`.close()
            } catch (e: IOException) {
                Log.e(e.message, e.toString())
            }
        }
        return count
    }

    class RecyclerViewAdapter(
        context: Context,
        arrayList: ArrayList<HashMap<String, Any>>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var arrayListAdapter: ArrayList<HashMap<String, Any>> = arrayList
        private var activity: Context = context
        private var VIEW_TYPE_NO_DATA = 0

        override fun getItemViewType(position: Int): Int {
            return VIEW_TYPE_NO_DATA
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View = LayoutInflater.from(activity)
                .inflate(R.layout.selected_file_show, parent, false)
            return DataViewHoldernew(view)
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            if (viewHolder is DataViewHoldernew) {
                val holder = viewHolder
                holder.file_text.text = "" + arrayListAdapter[position]["file_name"].toString()
                holder.file_image_delete.setOnClickListener {
                    deleteFile(
                        activity,
                        position,
                        arrayListAdapter[position]["file_name"].toString()
                    )
                }

                val file = File(
                    activity.filesDir
                        .toString() + "/Pepul/Files/" + arrayListAdapter[position]["file_name"].toString()
                )

                Glide.with(activity)
                    // .load(ContentUriUtils.getFilePath(activity, dataList[0]).toString())
                    .load(file.path)
                    .placeholder(R.drawable.image_camera)
                    .error(R.drawable.image_camera)
                    .transition(DrawableTransitionOptions.withCrossFade()) //Optional
                    .skipMemoryCache(false) //No memory cache
                    .diskCacheStrategy(DiskCacheStrategy.ALL) //No disk cache
                    .into(holder.file_image)
            }
        }

        override fun getItemCount(): Int {
            return arrayListAdapter.size
        }

        internal inner class DataViewHoldernew(view: View) :
            RecyclerView.ViewHolder(view) {
            var file_text: TextView
            var file_image: ImageView
            var file_image_delete: ImageView
            var file_view: ImageView? = null

            init {
                file_text = view.findViewById(R.id.file_text)
                file_image = view.findViewById(R.id.file_image)
                file_image_delete = view.findViewById(R.id.file_image_delete)
            }
        }
    }
}