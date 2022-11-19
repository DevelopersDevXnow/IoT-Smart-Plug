package com.devxnow.smartplug

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class LoginSignup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.Theme_IotSmartPlug);
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_login_signup)

        //show the ad


    }
}