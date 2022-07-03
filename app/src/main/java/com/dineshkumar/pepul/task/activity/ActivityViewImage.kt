package com.dineshkumar.pepul.task.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dineshkumar.pepul.task.R
import com.dineshkumar.pepul.task.databinding.LayoutViewImageBinding

class ActivityViewImage : AppCompatActivity() {
    lateinit var binding: LayoutViewImageBinding
    var imageUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutViewImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUrl = intent.extras!!.getString("url")!!
        println("---url $imageUrl")

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "" + imageUrl

        Glide.with(this@ActivityViewImage)
            .load(imageUrl)
            .placeholder(R.drawable.image_preview)
            .error(R.drawable.image_preview)
            .transition(DrawableTransitionOptions.withCrossFade()) //Optional
            .skipMemoryCache(false) //No memory cache
            .diskCacheStrategy(DiskCacheStrategy.ALL) //No disk cache
            .into(binding.imageView)

    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

}