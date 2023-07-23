package com.klasifikasibijikopi

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.klasifikasibijikopi.databinding.ActivityDetailBinding
import com.klasifikasibijikopi.ml.BijiKopi
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val imageSize = 224

    private lateinit var labels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)

        labels =
            application.assets.open("biji_kopi.txt").bufferedReader().use { it.readText() }.split("\n")

        val image = intent.getParcelableExtra<Uri>("image")

        if (image == null) {
            moveToMain()
        } else {
            val bitmap = processImage(image)

            binding.showImage.setImageBitmap(bitmap)

            prediction(bitmap)
        }

        binding.mbKembali.setOnClickListener {
            moveToMain()
        }
    }

    private fun moveToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun prediction(bitmap: Bitmap) {
//        try {
        val resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, false)
        val model = BijiKopi.newInstance(this)
//        val tbuffer = TensorImage.fromBitmap(resized)
//        val byteBuffer = tbuffer.buffer

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)

//        val tensorImage = TensorImage(DataType.FLOAT32)
        val tensorImage = TensorImage.fromBitmap(resized)
//        tensorImage.load(resized)
        val byteBuffer = tensorImage.buffer

        inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//        val max = getMax(outputFeature0.floatArray)

        val min = outputFeature0.floatArray.indices.map { i: Int -> outputFeature0.floatArray[i] }
            .maxOrNull()

        var i = 0
        outputFeature0.floatArray.indices.map {
            Log.println(Log.ASSERT, "output", "${outputFeature0.floatArray[it]}")
            if (outputFeature0.floatArray[it] == min) i = it
        }

//        binding.txtPrediksi.text = labels[max]



//        binding.txtPrediksi.text = "${outputFeature0.floatArray[0]} = ${labels[0]} " +
//                "\n ${outputFeature0.floatArray[1]} = ${labels[1]} " +
//                "${labels[i]}"

        binding.mtvPrediksi.text = labels[i]

        val akurasi = outputFeature0.floatArray[i].toInt()
        if(akurasi >= 100) {
            binding.mtvAkurasi.text = "100%"
            binding.circularProgressBar.progress = 100F
        } else {
            binding.mtvAkurasi.text = "$akurasi %"
            binding.circularProgressBar.progress = akurasi.toFloat()
        }



//        binding.txtPrediksi1.text = min.toString() + "= " + i.toString()



//        Toast.makeText(this, "clasificatio : ${labels[max]}", Toast.LENGTH_LONG).show()

// Releases model resources if no longer used.
        model.close()
//        } catch (e: Exception) {
//            Log.println(Log.ASSERT, "error", e.localizedMessage)
//            Toast.makeText(this, "error => " + e.message, Toast.LENGTH_LONG).show()
//        }

    }

    //
    private fun processImage(uri: Uri?): Bitmap {
        return MediaStore.Images.Media.getBitmap(
            contentResolver,
            uri
        )
    }
}