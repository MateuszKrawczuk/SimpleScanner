package pl.mateuszkrawczuk.simplescanner.ui.main

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata


class QrCodeAnalyzer(message: TextView) : ImageAnalysis.Analyzer {

    var textView: TextView = message

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(proxy_img: ImageProxy) {
        val mediaImage = proxy_img.image
        if (mediaImage != null) {
            val image = FirebaseVisionImage.fromMediaImage(
                mediaImage,
                FirebaseVisionImageMetadata.ROTATION_0
            )

            val options = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                    FirebaseVisionBarcode.FORMAT_DATA_MATRIX,
                    FirebaseVisionBarcode.FORMAT_QR_CODE,
                    FirebaseVisionBarcode.FORMAT_CODABAR
                )
                .build()

            val detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options)

            detector.detectInImage(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val valueType = barcode.valueType
                        // See API reference for complete list of supported types
                        when (valueType) {
                            FirebaseVisionBarcode.TYPE_TEXT -> {
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


