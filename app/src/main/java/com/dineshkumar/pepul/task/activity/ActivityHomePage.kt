package com.dineshkumar.pepul.task.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dineshkumar.pepul.task.R
import com.dineshkumar.pepul.task.databinding.LayoutHomeBinding
import com.dineshkumar.pepul.task.databinding.LayoutTaskDialogBinding
import com.dineshkumar.pepul.task.interfaces.onClick
import com.dineshkumar.pepul.task.model.GetList
import com.dineshkumar.pepul.task.mvvm.MainRepository
import com.dineshkumar.pepul.task.mvvm.MyViewModelFactory
import com.dineshkumar.pepul.task.adapter.MainAdapter
import com.dineshkumar.pepul.task.mvvm.MainViewModel
import com.dineshkumar.pepul.task.retrofit.RetrofitApiInterFace
import com.dineshkumar.pepul.task.retrofit.RetrofitInstance
import com.dineshkumar.pepul.task.support.AppUtils
import com.google.gson.Gson

class ActivityHomePage : AppCompatActivity(), onClick {
    private val TAG = ActivityHomePage::class.java.simpleName

    lateinit var binding: LayoutHomeBinding
    lateinit var viewModel: MainViewModel
    var list = mutableListOf<GetList>()
    var dataList = ArrayList<GetList.Datum>()
    lateinit var adapter: MainAdapter
    var load_flag = 0
    var exit_flag = 0
    var lastId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.app_name)

        initViewModel()

        if (AppUtils.checkNetworkAvailable(this@ActivityHomePage)) {
            binding.layoutMenu.visibility = View.VISIBLE
            binding.layoutWarning.visibility = View.GONE
            getList("")
        } else {
            binding.progressBar.visibility = View.GONE
            binding.layoutMenu.visibility = View.GONE
            binding.layoutWarning.visibility = View.VISIBLE
            binding.textWarning.text = AppUtils.text_net_check
        }

        initRecyclerView()

        binding.add.setOnClickListener {
            val intent = Intent(this@ActivityHomePage, ActivityAddFiles::class.java)
            activityResultLauncher.launch(intent)
        }

        binding.textRetry.setOnClickListener {
            if (AppUtils.checkNetworkAvailable(this@ActivityHomePage)) {
                binding.layoutMenu.visibility = View.VISIBLE
                binding.layoutWarning.visibility = View.GONE
                getList("")
            } else {
                AppUtils.showToast(this@ActivityHomePage, AppUtils.text_net_check)

            }
        }
    }

    private var activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                if (AppUtils.checkNetworkAvailable(this@ActivityHomePage)) {
                    binding.layoutMenu.visibility = View.VISIBLE
                    binding.layoutWarning.visibility = View.GONE
                    getList("")
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutMenu.visibility = View.GONE
                    binding.layoutWarning.visibility = View.VISIBLE
                    binding.textWarning.text = AppUtils.text_net_check
                }
            }
        }

    private fun getList(id: String) {
        lastId = id
        if (lastId == "") {
            binding.progressBar.visibility = View.VISIBLE
        }
        val map: MutableMap<String, String> = HashMap()
        map["lastFetchId"] = "" + lastId
        println("-- data input : $map")
        viewModel.getList(map)
    }

    private fun initViewModel() {
        val apiInterface = RetrofitInstance.getRetrofitInstance()!!.create(RetrofitApiInterFace::class.java)
        viewModel = ViewModelProvider(this, MyViewModelFactory(MainRepository(apiInterface))).get(
            MainViewModel::class.java
        )
        viewModel.list.observe(this, Observer {
            Log.d(TAG, "onCreate: $it")
            binding.progressBar.visibility = View.GONE

            val gson = Gson()
            val result = gson.toJson(it)
            println("-- data output : $result")

            if (it.message == "success"){
                if (lastId != "") {
                    if (dataList.size != 0) {
                        dataList.removeAt(dataList.size - 1)
                        adapter.notifyItemRemoved(
                            dataList.size
                        )
                    }
                } else {
                    list.clear()
                    dataList.clear()
                }
                list.addAll(listOf(it))
                println("-- output size : ${list.size}")

                for (i in list[0].data!!.indices) {
                    val data = GetList.Datum()
                    data.file = list[0].data!![i].file
                    data.fileType = list[0].data!![i].fileType
                    data.id = list[0].data!![i].id
                    dataList.add(data)
                }
                println("-- output size : ${dataList.size}")
                adapter.setList()
                load_flag = 0
            } else {
                if (dataList.size != 0) {
                    dataList.removeAt(dataList.size - 1)
                    adapter.notifyItemRemoved(
                        dataList.size
                    )
                }
                AppUtils.showToast(this@ActivityHomePage, "No data")
            }

        })
        viewModel.deleteStatus.observe(this, Observer {
            val gson = Gson()
            val result = gson.toJson(it)
            println("-- data output : $result")

            if (it.message == "success"){

            } else {
                AppUtils.showToast(this@ActivityHomePage, it.message!!)
            }
            AppUtils.dialogLoading.dismiss()
        })
        viewModel.errorMessage.observe(this, Observer {
            AppUtils.showToast(this@ActivityHomePage, it.toString())
        })
    }

    private fun initRecyclerView() {
        val layoutManager: GridLayoutManager = object : GridLayoutManager(this, 2) {
            override fun canScrollVertically(): Boolean {
                return true
            }
        }
        binding.recyclerview.layoutManager = layoutManager
        adapter = MainAdapter(this@ActivityHomePage, dataList)
        binding.recyclerview.adapter = adapter
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("-----end : ", "reached")
                    if (load_flag == 0) {
                        load_flag = 1
                        lastId = dataList[dataList.size - 1].id!!
                        val data = GetList.Datum()
                        data.file = ""
                        data.fileType = ""
                        data.id = "load"
                        dataList.add(data)

                        adapter.notifyItemInserted(dataList.size - 1)
                        binding.recyclerview.scrollToPosition(dataList.size - 1)

                        Handler(Looper.myLooper()!!).postDelayed({
                            if (AppUtils.checkNetworkAvailable(this@ActivityHomePage)) {
                                getList(lastId)
                            } else {
                                load_flag = 0
                                AppUtils.showToast(this@ActivityHomePage, AppUtils.text_net_check)
                                if (dataList.size != 0) {
                                    dataList.removeAt(dataList.size - 1)
                                    adapter.notifyItemRemoved(
                                        dataList.size
                                    )
                                }
                            }
                        }, 500)

                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolmenu_home, menu)
        val actionInfo = menu.findItem(R.id.action_info)
        actionInfo.isVisible = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {

        if (menuItem.itemId == R.id.action_info) {
            showTask()
        }
        if (menuItem.itemId == R.id.action_add) {
            val intent = Intent(this@ActivityHomePage, ActivityAddFiles::class.java)
            activityResultLauncher.launch(intent)
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun showTask() {
        val dialog = Dialog(
            this@ActivityHomePage,
            android.R.style.Theme_DeviceDefault_Dialog_MinWidth
        )
        val bindingDia: LayoutTaskDialogBinding =
            LayoutTaskDialogBinding.inflate(LayoutInflater.from(this@ActivityHomePage))
        dialog.setContentView(bindingDia.root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))

        bindingDia.run {
            textContent.text = "Dineshkumar Selvaraj\nEmail : dineshkumarsdk6@gmail.com" +
                    "\nMobile : 9942385589, 8838073806\nLocation : Tiruchengode, Namakkal\n\nTask Start : 03-07-2022\nTask End : 04-07-2022"
        }

        bindingDia.textYes.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onclick(deleteId: String) {
        println("-- delete id : $deleteId")
        if (AppUtils.checkNetworkAvailable(this@ActivityHomePage)) {
            AppUtils.dialogLoading(this@ActivityHomePage, "Loading...", false).show()
            val map: MutableMap<String, String> = HashMap()
            map["id"] = "" + deleteId
            println("-- data input : $map")
            viewModel.deleteFile(map)
        } else {
            AppUtils.showToast(this@ActivityHomePage, AppUtils.text_net_check)
        }
    }

    override fun onBackPressed() {

        if (exit_flag == 0){
            exit_flag = 1
            AppUtils.showToast(this@ActivityHomePage, "Press one more time to exit")
        } else {
            super.onBackPressed()
        }
    }
}