package pt.ulisboa.tecnico.cmov.shopist

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias BarcodeListener = (barcode: String) -> Unit

class BarcodeScannerActivity : AppCompatActivity() {
    private var barcodes: MutableMap<String, Int> = mutableMapOf()
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val TAG = "${ShopIST.TAG}.BarcodeScanner"
        const val BARCODE = "$TAG.BARCODE"

        private const val MIN_TRIES = 3

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_view)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listener for inserting manually
        findViewById<FloatingActionButton>(R.id.insertManuallyBarcode).setOnClickListener {
            insertManually()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun insertManually() {
        val editText = EditText(this)
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(this)
            .setTitle(R.string.product_add_barcode)
            .setView(editText)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                val value = editText.text
                if (value.isEmpty()) {
                    Toast.makeText(this, R.string.barcode_not_empty, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    dialog.dismiss()
                    setResult(
                        RESULT_OK,
                        Intent().putExtra(BARCODE, value.toString())
                    )
                    finish()
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(
                        findViewById<PreviewView>(R.id.viewFinder).createSurfaceProvider()
                    )
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        storeBarcode(barcode)
                    })
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun storeBarcode(code: String) {
        val hasCode = code in barcodes
        var previousNumber = barcodes[code]
        if (hasCode) {
            barcodes[code] = previousNumber!! + 1
        } else {
            previousNumber = 0
            barcodes[code] = previousNumber + 1
        }

        previousNumber++
        Log.d(TAG, "Code $code with $previousNumber tries")

        if (hasCode && (previousNumber ) >= MIN_TRIES) {
            // Return this to activity who started this one
            setResult(
                RESULT_OK,
                Intent().putExtra(BARCODE, code)
            )
            finish()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    inner class BarcodeAnalyzer(private val listener: BarcodeListener) : ImageAnalysis.Analyzer {

        private val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS
            )
            .build()


        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(image: ImageProxy) {
            if (image.image == null) {
                return
            }

            val inputImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient(options)

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        listener(barcode.rawValue ?: "")
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Can't use barcode scanner: $it")
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }
}