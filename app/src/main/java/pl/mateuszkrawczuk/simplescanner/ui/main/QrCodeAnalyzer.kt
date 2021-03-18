package pl.mateuszkrawczuk.simplescanner.ui.main

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import android.view.Surface.ROTATION_0
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage


class QrCodeAnalyzer(message: TextView) : ImageAnalysis.Analyzer {

    var textView: TextView = message

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(proxy_img: ImageProxy) {
        val mediaImage = proxy_img.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                ROTATION_0
            )

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_CODABAR
                )
                .build()

            val detector = BarcodeScanning.getClient(options)

            detector.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val valueType = barcode.valueType
                        // See API reference for complete list of supported types
                        when (valueType) {
                            Barcode.TYPE_TEXT -> {
                                val value = byteArrayToHexLineString(
                                    Base64.decode(barcode.rawValue, Base64.DEFAULT)
                                )
                                    .substring(4)
                                Log.i("Simple Scanner Mac ID", value)
                                textView.text = value
                            }
                        }
                    }
                    mediaImage.close()
                    proxy_img.close()
                    // Task completed successfully
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    mediaImage.close()
                    proxy_img.close()

                }
        }
    }

    private fun byteArrayToHexLineString(array: ByteArray): String {
        val sb = StringBuilder()
        for (b in array) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString().trim { it <= ' ' }
    }
}


