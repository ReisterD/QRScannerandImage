package com.example.qrscanner

import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var textWifi : TextView
    lateinit var imageQR : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttons()

    }

    private fun buttons () {
        val imageQR : Button = findViewById(R.id.ImageBTN)
        val cameraQR : Button = findViewById(R.id.Camera)

        imageQR.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            qrImage.launch(intent)
        }

        cameraQR.setOnClickListener{
            qrScanCamera()
        }
    }

    private fun qrScanCamera() {
        val options = ScanOptions()
        options.setPrompt("Volume up to flash on")
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)
        options.captureActivity = CaptureAct::class.java
        barlaucher.launch(options)
    }

    var barlaucher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Result")
            builder.setMessage(result.contents)
            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialogInterface, i -> dialogInterface.dismiss() })
                .show()
        }
    }

    private val qrImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK
            && result.data != null
        ) {
            val photoUri: Uri? = result.data!!.data
            imageQR = findViewById(R.id.ImageQR)
            imageQR.isVisible = true
            imageQR.setImageURI(photoUri)
            val inputStream: InputStream? = photoUri?.let { contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap == null) {
                Log.e("TAG", "uri is not a bitmap," + photoUri.toString())
            }
            val width = bitmap!!.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            bitmap.recycle()
            val source = RGBLuminanceSource(width, height, pixels)
            val bBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            try {
                val result : com.google.zxing.Result = reader.decode(bBitmap)
                textWifi = findViewById(R.id.WifiInfo)
                textWifi.text = result.toString()
            } catch (e : NotFoundException) {
                Log.e("TAG", "decode exception", e);
            }
        }
    }

}