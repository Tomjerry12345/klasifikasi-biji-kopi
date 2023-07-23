package com.klasifikasibijikopi

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.klasifikasibijikopi.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val getFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            moveToDetail(uri)
        }

    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    Log.println(Log.ASSERT, "uri-capture", "$uri")
                    moveToDetail(uri)
                }
            }
        }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "isGranted Succes", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this, "isGranted Error", Toast.LENGTH_LONG).show()
            }
        }

    private var latestTmpUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        activityResultLauncher.launch(CAMERA)

        binding.mbCamera.setOnClickListener {
            capturePhoto()
        }

        binding.mbGallery.setOnClickListener {
            getImageFromStorage()
        }

    }

    private fun capturePhoto() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun getImageFromStorage() {
        getFromGallery.launch("image/*")
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            applicationContext,
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private fun moveToDetail(uri: Uri?) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("image", uri)
        startActivity(intent)
    }

}