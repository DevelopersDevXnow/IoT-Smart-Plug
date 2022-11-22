package com.devxnow.smartplug

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SPLASH_TAG"

        //Number of seconds to count down before showing the app open ad. This  simulates the time needed to load the app.
        private const val COUNTER_TIMER: Long = 5

    }

    private var secondsRemaining: Long = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createTimer(COUNTER_TIMER)

        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                finish()
            }
        })


    }

    private fun createTimer(seconds: Long) {

        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000,1000) {

            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "onTick: $millisUntilFinished")
                secondsRemaining = millisUntilFinished / 1000 + 1
            }

            override fun onFinish() {
                Log.d(TAG, "onFinish: ")
                secondsRemaining = 0

                val application = application
                if (application !is ApplicationClass) {

                    Log.d(
                        TAG,
                        "onCrete: Failed to cast application to ApplicationClass"
                    )// e.g. if you don't register your ApplicationClass in manifest
                    strartHomeActivity()
                    return
                }

                //show add
                application.showAdIfAvailable(
                    this@MainActivity,
                    object : ApplicationClass.OnShowAdCompleteListener {
                        override fun onShowAdComplete() {
                            Log.d(TAG, "onShowAdComplete: ")
                            strartHomeActivity()
                        }
                    }
                )


            }
        }

        //start timer
        countDownTimer.start()
    }

    private fun strartHomeActivity() {
//Start Home HomeActivity
        val intent  = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()

    }
}