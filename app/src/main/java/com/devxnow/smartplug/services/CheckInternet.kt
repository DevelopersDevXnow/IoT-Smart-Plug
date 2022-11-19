package com.devxnow.smartplug.services

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.WindowManager.BadTokenException
import android.widget.Toast
import com.devxnow.smartplug.R
import com.example.awesomedialog.*

class CheckInternet {

    companion object {
        private const val TAG = "CHECK_INTERNET_TAG"
    }

    fun isNetworkAvailable(context: Context): Boolean {


        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }


//        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetwork = cm.activeNetworkInfo
//        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }


    /*
  Show
  Internet
  Connection Dialog
   */
    fun showNetworkAlertDialog(context: Context) {
        buildAlertDialog(
            context,
            context.getString(R.string.network_disconnected),
            context.getString(R.string.network_error_message)
        )
    }

    /*
Show
Internet
Disconnect Popup Dialog
 */
    fun showNetworkDisconnectPopupDialog(context: Activity) {


        AwesomeDialog.build(context)
            .title(context.getString(R.string.network_disconnected))
            .body(context.getString(R.string.network_error_message))
            .icon(R.drawable.ic_no_internet,true)
            .position(AwesomeDialog.POSITIONS.BOTTOM)
            .onPositive("Connect") {
                Log.d("TAG", "positive Button")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
                } else context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))


            }


    }

    fun buildAlertDialog(context: Context?, title: String?, message: String?): AlertDialog? {
        if (context == null) return null
        val builder = AlertDialog.Builder(context)
        if (title != null) builder.setTitle(title)
        if (message != null) builder.setMessage(message)
        builder.setPositiveButton("Ok") { dialogInterface, i -> dialogInterface.dismiss() }
        val dialog = builder.create()
        try {
//            // crash Analytics Utility.java line 104
//            //Problem is that, alert tries to show, although your activity is finished.
//            //So what I do, is to check if activity is finishing before showing alert.
//            // For this purpose isFinishing() method is defined within Activity class


//     if (!((HomeActivity)context).isFinishing())
            dialog.show()
        } catch (e: BadTokenException) {
            showToast(context, "Exception ->$e")
        }
        return dialog
    }

    // Show Toast message
    fun showToast(context: Context?, message: String?) {
        if (context != null) Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}