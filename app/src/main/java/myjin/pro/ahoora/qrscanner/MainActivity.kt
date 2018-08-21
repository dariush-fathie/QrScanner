package myjin.pro.ahoora.qrscanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.widget.Toast

import com.google.zxing.WriterException

import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidmads.library.qrgenearator.QRGSaver
import kotlinx.android.synthetic.main.activity_main.*
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.BarcodeFormat
import java.util.Arrays.asList
import com.google.zxing.MultiFormatReader
import java.util.*


class MainActivity : AppCompatActivity() {

    internal var TAG = "GenerateQRCode"

    lateinit var inputValue: String
    var savePath = Environment.getExternalStorageDirectory().path + "/QRCode/"
    lateinit var bitmap: Bitmap
    lateinit var qrgEncoder: QRGEncoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        start.setOnClickListener {
            if(checkStoragePermissions()){
                createQr()
            }

        }

        save.setOnClickListener {
            val save: Boolean
            val result: String
            try {
                save = QRGSaver.save(savePath, edt_value.text.toString().trim { it <= ' ' }, bitmap, QRGContents.ImageType.IMAGE_JPEG)
                result = if (save) "Image Saved" else "Image Not Saved"
                Toast.makeText(applicationContext, result, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private val rwRequest = 1080
    private val rwPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun checkStoragePermissions(): Boolean {
        val write = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val read = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        rwPermissions,
                        rwRequest
                )
                false
            }
        } else {
            // API < 23
            true
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == rwRequest) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
              createQr()
            } else {
                Toast.makeText(this, "اجازه دسترسی به حافظه داده نشد", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun createQr(){
        inputValue = edt_value.text.toString().trim { it <= ' ' }
        if (inputValue.length > 0) {
            val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = manager.defaultDisplay
            val point = Point()
            display.getSize(point)
            val width = point.x
            val height = point.y
            var smallerDimension = if (width < height) width else height
            smallerDimension = smallerDimension * 3 / 4

            qrgEncoder = QRGEncoder(
                    inputValue, null,
                    QRGContents.Type.TEXT,
                    smallerDimension)
            try {
                bitmap = qrgEncoder.encodeAsBitmap()
                val overlay = BitmapFactory.decodeResource(resources, R.drawable.jin_logo)
                bitmap = mergeBitmaps(overlay, bitmap)
                QR_Image.setImageBitmap(bitmap)
            } catch (e: WriterException) {
                Log.v(TAG, e.toString())
            }

        } else {
            edt_value.error = "Required"
        }
    }

    fun mergeBitmaps(overlay: Bitmap, bitmap: Bitmap): Bitmap {

        val height = bitmap.height
        val width = bitmap.width

        val combined = Bitmap.createBitmap(width, height, bitmap.config)
        val canvas = Canvas(combined)
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height

        canvas.drawBitmap(bitmap, Matrix(), null)

        val centreX = (canvasWidth - overlay.width) / 2
        val centreY = (canvasHeight - overlay.height) / 2
        canvas.drawBitmap(overlay, centreX.toFloat(), centreY.toFloat(), null)

        return combined
    }

/*    private fun scanQr(){
        val mReader = MultiFormatReader()
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
        hints.put(DecodeHintType.TRY_HARDER, true)
// select your barcode formats here
        val formats = Arrays.asList(BarcodeFormat.QR_CODE)
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats)

        mReader.setHints(hints)

// your camera image here
        var bitmapI: Bitmap?
        val width = bitmapI!!.width
        val height = bitmapI.height
        val pixels = IntArray(width * height)
        bitmapI.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmapI.recycle()
        bitmapI = null
        val bb = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(width, height, pixels)))
        val result = mReader.decodeWithState(bb)
        val resultString = result.text
    }*/



}
