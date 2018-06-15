package com.shain.voicerecorder

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.shain.voicerecorder.recordview.*
import java.util.concurrent.TimeUnit

/**
 * Created by Shain on 15/06/2018.
 */

class MainActivity : AppCompatActivity() {

    private var mToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val recordView = findViewById<RecordView>(R.id.record_view)
        val recordButton = findViewById<RecordButton>(R.id.record_button)
        val btnChangeOnclick = findViewById<Button>(R.id.btn_change_onclick)

        //IMPORTANT
        recordButton.setRecordView(recordView)

        // if you want to click the button (in case if you want to make the record button a Send Button for example..)
        //recordButton.isListenForRecord = false

        btnChangeOnclick.setOnClickListener {
            if (recordButton.isListenForRecord) {
                recordButton.isListenForRecord = false
                Toast.makeText(this, "onClickEnabled", Toast.LENGTH_SHORT).show()
                btnChangeOnclick.text = "Change to touch"
            } else {
                recordButton.isListenForRecord = true
                Toast.makeText(this, "onClickDisabled", Toast.LENGTH_SHORT).show()
                btnChangeOnclick.text = "Change to click"
            }
        }


         //ListenForRecord must be false ,otherwise onClick will not be called
        recordButton.setOnRecordClickListener(object : OnRecordClickListener {
            override fun onClick(v: View) {
                Toast.makeText(this@MainActivity, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show()
                Log.d("RecordButton", "RECORD BUTTON CLICKED")
            }
        })


        //Cancel Bounds is when the Slide To Cancel text gets before the timer . default is 130
        recordView.cancelBounds = 130f

        recordView.setSmallMicColor(Color.parseColor("#c2185b"))

        //prevent recording under one Second
        recordView.isLessThanSecondAllowed = false

        recordView.setSlideToCancelText("Slide To Cancel")

        recordView.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0)


        recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                Log.d("RecordView", "onStart")
                showToast("OnStartRecord")

            }

            override fun onCancel() {
                showToast("onCancel")

                Log.d("RecordView", "onCancel")

            }

            override fun onFinish(recordTime: Long) {

                val time = getHumanTimeText(recordTime)
                showToast("onFinishRecord - Recorded Time is: ")
                Log.d("RecordView", "onFinish")

                Log.d("RecordTime", time)
            }

            override fun onLessThanSecond() {
                showToast("OnLessThanSecond")
                Log.d("RecordView", "onLessThanSecond")
            }
        })




        recordView.setOnBasketAnimationEndListener(object : OnBasketAnimationEnd {
            override fun onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished")
            }

        })

    }

    fun showToast(text: String) {
        if (mToast == null) mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        mToast?.setGravity(Gravity.CENTER, 0, +300)
        mToast?.setText(text)
        mToast?.duration = Toast.LENGTH_SHORT
        mToast?.show()
    }

    private fun getHumanTimeText(milliseconds: Long): String {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)))
    }

}
