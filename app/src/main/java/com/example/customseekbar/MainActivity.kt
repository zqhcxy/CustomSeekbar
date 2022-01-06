package com.example.customseekbar

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.customseekbar.weight.CenterStartSeekbar
import com.example.customseekbar.weight.SizeChangeSeekBar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val centerSeekbar = findViewById<CenterStartSeekbar>(R.id.custom_center_seekbar)
        centerSeekbar.mProgressColor = Color.GREEN
        centerSeekbar.mThumbColor = Color.BLUE
//        centerSeekbar.isCenterSeekbar = false// 普通seekbar

        val sizeSeekbar = findViewById<SizeChangeSeekBar>(R.id.custom_size_seekbar)
        sizeSeekbar.thumbColor = Color.YELLOW



        centerSeekbar.seekbarInterface = object : CenterStartSeekbar.CenterSeekbarInterface {
            override fun onProgressChanged(
                seekbar: CenterStartSeekbar,
                progress: Int,
                formUser: Boolean
            ) {
                Log.i(
                    "MainActivity",
                    "CenterStartSeekbar onProgressChanged: $progress formUser: $formUser"
                )
            }

            override fun onStartTrackingTouch(seekbar: CenterStartSeekbar) {
                Log.i("MainActivity", "CenterStartSeekbar onStartTrackingTouch")
            }

            override fun onStopTrackingTouch(seekbar: CenterStartSeekbar) {
                Log.i("MainActivity", "CenterStartSeekbar onStopTrackingTouch")
            }

        }

        sizeSeekbar.seekbarInterface = object : SizeChangeSeekBar.CenterSeekbarInterface {
            override fun onProgressChanged(
                seekbar: SizeChangeSeekBar,
                progress: Int,
                formUser: Boolean
            ) {
                Log.i(
                    "MainActivity",
                    "CenterStartSeekbar onProgressChanged: $progress formUser: $formUser"
                )
            }

            override fun onStartTrackingTouch(seekbar: SizeChangeSeekBar) {
                Log.i("MainActivity", "CenterStartSeekbar onStartTrackingTouch")
            }

            override fun onStopTrackingTouch(seekbar: SizeChangeSeekBar) {
                Log.i("MainActivity", "CenterStartSeekbar onStopTrackingTouch")
            }

        }
    }
}