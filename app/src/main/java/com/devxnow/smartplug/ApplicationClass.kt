package com.devxnow.smartplug

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class ApplicationClass : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private var appOpenAdManager: AppOpenAdManager? = null
    private var currentActivity: Activity? = null

    companion object {

        private const val TAG = "MY_APPLICATION_TAG"
    }

    override fun onCreate() {
        super.onCreate()
//Enabling Offline Capabilities on Android
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        registerActivityLifecycleCallbacks(this)

        MobileAds.initialize(this) {
            Log.d(TAG, "onCreate: onInitializationCompleted")
        }

        //Set your test devices. Check your logcat output for the hashed device ID to
        //get test ads on a physical device. e.g.
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("PLACE_TEST_DEVICE_ID", "PLACE_TEST_DEVICE_ID"))
                .build()
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()

    }

    //Interface definition for a callback to be invoked when an app open ad is complete * (i.e. dismissed or fails to show).
    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    //LifecycleObserver method that shows the app open ad when the app moves to foreground.
@OnLifecycleEvent(Lifecycle.Event.ON_START)
private  fun onMoveToForeground(){
        //Show the ad (if available) when the app moves to foreground
    appOpenAdManager!!.showAdIfAvailable(currentActivity!!)
}

    /**
     * show an app open ad.
     *
     * show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(activity: Activity,onShowAdCompleteListener: OnShowAdCompleteListener){
        //We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        appOpenAdManager!!.showAdIfAvailable(activity,onShowAdCompleteListener)
    }


    /*Activity lifecycle Callbacks methods*/

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d(TAG, "onActivityStarted")
        //An ad activity is started when an ad is showing, which could be AdActivity class from Google
        //SDK or another activity class implemented by a third party mediation partner. Updating the
        //currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if(!appOpenAdManager!!.isShowingAd){
            currentActivity = activity
        }

    }

    override fun onActivityResumed(activity: Activity) {
        Log.d(TAG, "onActivityResumed")
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d(TAG, "onActivityPaused")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d(TAG, "onActivityStopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d(TAG, "onActivitySaveInstanceState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d(TAG, "onActivityDestroyed")
    }



    /*Inner class that loads and shows app open ads*/
    private inner class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false

        private var loadTime: Long = 0

        private fun loadAd(context: Context) {

            if (isLoadingAd || isAdAvailable()) {
                return
            }
            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                "ca-app-pub-3940256099942544/3419835294",
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    /*Called when the App Open Ad failed to load */
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        Log.d(TAG, "onAdFailedToLoad: ${adError.message}")
                        Toast.makeText(
                            context,
                            "Failed to Load ad due to ${adError.message}",
                            Toast.LENGTH_LONG
                        ).show()

                    }

                    /*Called when the App Open Ad is loaded*/
                    override fun onAdLoaded(ad: AppOpenAd) {
                        super.onAdLoaded(ad)
                        Log.d(TAG, "onAdLoaded: ")
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                        Toast.makeText(context, "Ad Loaded.....", Toast.LENGTH_LONG).show()

                    }
                }
            )

        }

        /*Check if ad was loaded more than n hours ago*/
        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        /*Check if ad exists and can be shown*/
        private fun isAdAvailable(): Boolean {
            //Ad references in the app open beta will time out after four hours, but this time limitations
            //May change in future beta versions. For details, see:
            //https://support.google.com/admob/answer/9341964?hl=en
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        /**
         * show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         */
        fun showAdIfAvailable(activity: Activity) {
            showAdIfAvailable(activity,
                object : OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        Log.d(TAG, "onShowAdComplete: ")
                    }
                })
        }

        /**
         * show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
         */
        fun showAdIfAvailable(
            activity: Activity,
            onShowAdCompleteListener: OnShowAdCompleteListener
        ) {
            if (isShowingAd) {
                Log.d(TAG, "showAdIfAvailable: The app open ad is already showing...")
                return
            }

            //if the app open ad is not available yet, invoke the callback then load the ad
            if (!isAdAvailable()) {
                Log.d(TAG, "showAdIfAvailable: The app open ad is not yet ready...")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }

            appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    super.onAdClicked()

                    Log.d(TAG, "onAdClicked: ")
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    /*Called when the App Open Ad is dismissed */
                    Log.d(TAG, "onAdDismissedFullScreenContent: ")
                    //set the reference to null so isAdAvailable() return false
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    /*Called when App Open Ad is failed to show*/
                    Log.d(TAG, "onAdFailedToShowFullScreenContent: ${adError.message}")
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    Log.d(TAG, "onAdImpression: ")
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    Log.d(TAG, "onAdShowedFullScreenContent: ")
                }
            }
            isShowingAd = true
            appOpenAd!!.show(activity)

        }

    }


}