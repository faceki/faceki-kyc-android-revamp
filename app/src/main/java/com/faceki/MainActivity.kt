package com.faceki

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.faceki.android.FaceKi

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.open_library).setOnClickListener {
            findViewById<TextView>(R.id.tv_result).text = null
            FaceKi.setCustomIcons(
                iconMap = hashMapOf(
                    FaceKi.IconElement.Logo to FaceKi.IconValue.Resource(R.drawable.ic_launcher_background)
                )
            )
            FaceKi.setCustomColors(
                colorMap = hashMapOf(
                    FaceKi.ColorElement.BackgroundColor to FaceKi.ColorValue.StringColor("#FFFFFF")
                )
            )
            FaceKi.startKycVerification(
                requestCode = REQUEST_CODE,
                context = this@MainActivity,
                clientId = TEST_CLIENT_ID,
                clientSecret = TEST_CLIENT_SECRET
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            val response = data?.getStringExtra(FaceKi.EXTRA_VERIFICATION_RESPONSE)
        }
    }

    companion object {
        private const val REQUEST_CODE = 290
        private const val TEST_CLIENT_ID = "hcpheirojikq71eqvb10pnmc4"
        private const val TEST_CLIENT_SECRET =
            "1410sub6osefuktl39jmi9m8600nmmkcfpk44vb1mj3ab1khu7ab"
    }

}