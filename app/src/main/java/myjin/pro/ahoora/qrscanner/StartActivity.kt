package myjin.pro.ahoora.qrscanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewPager
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_start.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class StartActivity : AppCompatActivity(),  ViewPager.OnPageChangeListener {


    private val rwRequest = 1080
    private val REQUEST_CAMERA = 1081
    private val cameraPermissions = arrayOf(Manifest.permission.CAMERA)

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {


    }

    override fun onPageSelected(position: Int) {
        if (position==0){
            EventBus.getDefault().post(VisibilityQrEvent(1))
        }else  if (position==1){

            if (checkStoragePermissions()){
                EventBus.getDefault().post(VisibilityQrEvent(0))
            }
        }
    }





    private fun checkStoragePermissions(): Boolean {

        val camerap = ActivityCompat.checkSelfPermission(this@StartActivity, Manifest.permission.CAMERA)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (camerap == PackageManager.PERMISSION_GRANTED ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                        this@StartActivity,
                        cameraPermissions,
                        REQUEST_CAMERA
                )
                false
            }
        } else {
            // API < 23
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        vp_mainContainer.adapter = PagerAdapter(supportFragmentManager)

        vp_mainContainer.addOnPageChangeListener(this)
        vp_mainContainer.offscreenPageLimit = 2
        tbl_main.setupWithViewPager(vp_mainContainer)

        tbl_main.getTabAt(0)?.setIcon(R.drawable.ic_create)
        tbl_main.getTabAt(1)?.setIcon(R.drawable.ic_scan_qr)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == rwRequest) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                EventBus.getDefault().post(VisibilityQrEvent(2))
            } else {
                Toast.makeText(this@StartActivity, "اجازه دسترسی به حافظه داده نشد", Toast.LENGTH_LONG).show()
            }
        }else if (requestCode == REQUEST_CAMERA){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                EventBus.getDefault().post(VisibilityQrEvent(0))
            } else {
                Toast.makeText(this@StartActivity, "اجازه دسترسی به دوربین داده نشد", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}
