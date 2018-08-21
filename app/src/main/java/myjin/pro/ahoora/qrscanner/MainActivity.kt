package myjin.pro.ahoora.qrscanner

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidmads.library.qrgenearator.QRGEncoder
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        start.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa_IR")

            Log.e("Locale.getDefault()", Locale.getDefault().toString())
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 10)
            } else {
                Toast.makeText(this@MainActivity, "دستگاه شما از زبان مورد نظر پشتیبانی نمی کند", Toast.LENGTH_LONG).show()

            }


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

}
