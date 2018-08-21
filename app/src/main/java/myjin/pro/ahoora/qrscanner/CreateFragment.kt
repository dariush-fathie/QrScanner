package myjin.pro.ahoora.qrscanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.Color.WHITE
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidmads.library.qrgenearator.QRGSaver
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.android.synthetic.main.fragment_create.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.nio.charset.Charset
import java.util.*

class CreateFragment : Fragment(), View.OnClickListener {

    var savePath = Environment.getExternalStorageDirectory().path + "/QRCode/"
    lateinit var bitmap: Bitmap
    lateinit var qrgEncoder: QRGEncoder

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.start -> {

                GenerateClick()
            }
            R.id.save -> {

                if (checkStoragePermissions()) {
                    save()
                }
            }
            R.id.iv_mic -> {

                SpeechToText()
            }
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as StartActivity).start.setOnClickListener(this)
        (activity as StartActivity).save.setOnClickListener(this)
        (activity as StartActivity).iv_mic.setOnClickListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create, container, false)
    }

    @Subscribe
    fun decode(e: DecodeQrEvent) {

        (activity as StartActivity).edt_value.setText(e.value)
        if (e.type.equals("QR_CODE")) {
            GenerateClick()
        }else{

            (activity as StartActivity).QR_Image.setImageResource(R.drawable.bgt)
        }
        (activity as StartActivity).vp_mainContainer.setCurrentItem(0)


    }

    fun GenerateClick() {
        val view = (activity as StartActivity).getCurrentFocus()
        if (view != null) {
            val imm = (activity as StartActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }
            val qrCodeData = (activity as StartActivity).edt_value.text.toString().trim { it <= ' ' }

            if (qrCodeData.length > 0) {
                generate(qrCodeData)
            } else {
                edt_value.error = "متنی موجود نیست"
            }

    }


    private fun createQr(qrCodeData:String){

        if (qrCodeData.length > 0) {
            val manager = (activity as StartActivity).getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = manager.defaultDisplay
            val point = Point()
            display.getSize(point)
            val width = point.x
            val height = point.y
            var smallerDimension = if (width < height) width else height
            smallerDimension = smallerDimension * 3 / 4

            qrgEncoder = QRGEncoder(
                    qrCodeData, null,
                    QRGContents.Type.TEXT,
                    smallerDimension)
            try {
                bitmap = qrgEncoder.encodeAsBitmap()
                val overlay = BitmapFactory.decodeResource(resources, R.drawable.jin_logo)
                bitmap = mergeBitmaps(overlay, bitmap)
                QR_Image.setImageBitmap(bitmap)
            } catch (e: WriterException) {
                Log.v("qrError", e.toString())
            }

        } else {
            edt_value.error = "Required"
        }
    }

    private fun generate(qrCodeData:String){
        val hintsMap = HashMap<EncodeHintType, Any>()

        val manager = (activity as StartActivity).getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width = point.x
        val height = point.y
        var smallerDimension = if (width < height) width else height
        smallerDimension = smallerDimension * 3 / 4
        hintsMap[EncodeHintType.CHARACTER_SET] = "utf-8"
        hintsMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.Q
        hintsMap[EncodeHintType.MARGIN] = 2


       try {
            val matrix = QRCodeWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, smallerDimension, smallerDimension, hintsMap)
           val width = matrix.width
           val height = matrix.height
           val pixels = IntArray(width * height)
           // All are 0, or black, by default
           for (y in 0 until height) {
               val offset = y * width
               for (x in 0 until width) {
                   //pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                   pixels[offset + x] = if (matrix.get(x, y))
                       ResourcesCompat.getColor(resources, R.color.colorB, null)
                   else
                       WHITE
               }
           }

           bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
           bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
           //setting bitmap to image view

           val overlay = BitmapFactory.decodeResource(resources, R.drawable.jin_logo)
           bitmap = mergeBitmaps(overlay, bitmap)
           (activity as StartActivity).QR_Image.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
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

    private val rwRequest = 1080
    private val rwPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun checkStoragePermissions(): Boolean {
        val write = ActivityCompat.checkSelfPermission((activity as StartActivity), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val read = ActivityCompat.checkSelfPermission((activity as StartActivity), Manifest.permission.READ_EXTERNAL_STORAGE)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(
                        (activity as StartActivity),
                        rwPermissions,
                        rwRequest
                )
                false
            }
        } else {
            true
        }
    }

    private fun save() {
        val save: Boolean
        val result: String
        try {
            save = QRGSaver.save(savePath, (activity as StartActivity).edt_value.text.toString().trim { it <= ' ' }, bitmap, QRGContents.ImageType.IMAGE_JPEG)
            result = if (save) "در مسیر " + savePath + " ذخیره شد " else "ذخیره نشد"
            Toast.makeText((activity as StartActivity), result, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscribe
    fun saveS(e: VisibilityQrEvent) {
        if (e.type == 2)
            save()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)

        super.onStop()

    }


    private fun SpeechToText(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa_IR")

        Log.e("Locale.getDefault()", Locale.getDefault().toString())
        if (intent.resolveActivity((activity as StartActivity).packageManager) != null) {
            startActivityForResult(intent, 10)
        } else {
            Toast.makeText((activity as StartActivity), "دستگاه شما از زبان مورد نظر پشتیبانی نمی کند", Toast.LENGTH_LONG).show()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){

            10->{
                if (resultCode== Activity.RESULT_OK&& data!=null){
                    val res=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    edt_value.setText(res.get(0))
                }
            }
        }
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
