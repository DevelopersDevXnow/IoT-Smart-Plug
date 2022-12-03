package com.devxnow.smartplug.methods

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.devxnow.smartplug.BuildConfig
import com.devxnow.smartplug.LoginSignup
import com.devxnow.smartplug.R
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import es.dmoral.toasty.Toasty
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.regex.Pattern
import javax.crypto.SecretKey


class Methods(private val context: Context) {

    private var vibrator: Vibrator? = null
    private val key: SecretKey? = null
    private val isClicked = false
    private lateinit var mConfirmPassword: String
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()


    var animError: Animation? = AnimationUtils.loadAnimation(context, R.anim.shake)

    companion object {

        private const val TAG = "METHOD_TAG"


    }


    fun logout(activity: Activity?) {
        try {
//            if (PlayerService.exoPlayer != null) {
//                Intent intent = new Intent(context, PlayerService.class);
//                intent.setAction(PlayerService.ACTION_STOP);
//                context.startService(intent);
//          }

            mAuth.signOut(); //End user session

            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();

          //  GoogleAuthProvider.getCredential(googleIdToken,null);

            val intent = Intent(activity, LoginSignup::class.java)
            activity?.startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    fun getPathImage(uri: Uri?): String? {
        return try {
            var filePath = ""
            val wholeID = DocumentsContract.getDocumentId(uri)

            // Split at colon, use second item in the array
            val id = wholeID.split(":").toTypedArray()[1]
            val column = arrayOf(MediaStore.Images.Media.DATA)

            // where id is equal to
            val sel = MediaStore.Images.Media._ID + "=?"
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, arrayOf(id), null
            )
            val columnIndex = cursor!!.getColumnIndex(column[0])
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex)
            }
            cursor.close()
            filePath
        } catch (e: Exception) {
            e.printStackTrace()
            if (uri == null) {
                return null
            }
            // try to retrieve the image from the media store first
            // this will only work for images selected from gallery
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null) {
                val column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                val returnn = cursor.getString(column_index)
                cursor.close()
                if (returnn == null) {
                    var path: String? = null
                    var image_id: String? = null
                    val cursor2 = context.contentResolver.query(uri, null, null, null, null)
                    if (cursor2 != null) {
                        cursor2.moveToFirst()
                        image_id = cursor2.getString(0)
                        image_id = image_id.substring(image_id.lastIndexOf(":") + 1)
                        cursor2.close()
                    }
                    val cursor3 = context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null,
                        MediaStore.Images.Media._ID + " = ? ",
                        arrayOf(image_id),
                        null
                    )
                    if (cursor3 != null) {
                        cursor3.moveToFirst()
                        path =
                            cursor3.getString(cursor3.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        cursor3.close()
                    }
                    return path
                }
                return returnn
            }
            // this is our fallback here
            uri.path
        }
    }

    fun getFilename(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result =
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    fun getfileExtension(uri: Uri?): String? {
        val contentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    //create bitmap from view and returns it
    fun getBitmapFromView(view: View): Bitmap {
        //Define a bitmap with the same size as the view
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            //   canvas.drawColor(MaterialColors.getColor(context, R.attr.colorSubBackground, Color.BLACK));
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    private fun saveBitMap(context: Context, drawView: View): File? {
        val pictureFileDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "KpQuotes"
        ) // enter folder name to save image
        if (!pictureFileDir.exists()) {
            val isDirectoryCreated = pictureFileDir.mkdirs()
            if (!isDirectoryCreated) Log.i("ATG", "Can't create directory to save the image")
            return null
        }
        val filename = pictureFileDir.path + File.separator + System.currentTimeMillis() + ".jpg"
        val pictureFile = File(filename)
        val bitmap = getBitmapFromView(drawView)
        try {
            pictureFile.createNewFile()
            val oStream = FileOutputStream(pictureFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream)
            oStream.flush()
            oStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.i("TAG", "There was an issue saving the image.")
        }
        scanGallery(context, pictureFile.absolutePath)
        return pictureFile
    }

    private fun scanGallery(cntx: Context, path: String) {
        try {
            MediaScannerConnection.scanFile(cntx, arrayOf(path), null) { path, uri -> }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Saves the image as PNG to the app's cache directory.
     *
     * @param image Bitmap to save.
     * @return Uri of the saved file or null
     */
    fun saveImage(image: Bitmap): Uri? {
        //TODO - Should be processed in another thread
        val imagesFolder = File(context.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri =
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        } catch (e: IOException) {
            Log.d(
                ContentValues.TAG,
                "IOException while trying to write file for sharing: " + e.message
            )
        }
        return uri
    }

    /**
     * Shares the PNG image from Uri.
     *
     * @param uri Uri of image to share.
     */
    fun shareImageUri(uri: Uri?) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.putExtra(
            Intent.EXTRA_TEXT, """
     I liked this quote from KP App and wanted to share it with you. 
     
     Get more insightful quotes daily on KP App. 
     Download App - https://app.keerthanprabhu.com
     """.trimIndent()
        )
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Kp Quote")
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.type = "image/*"
        context.startActivity(Intent.createChooser(shareIntent, "Share Via"))
    }

    fun setTextViewDrawableColor(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawablesRelative) {
            if (drawable != null) {
                drawable.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(textView.context, color),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    fun showToastMessage(message: String?) {
        Toasty.success(context, message!!, Toast.LENGTH_SHORT, true).show()
    }

    fun showToastMessageError(message: String?) {
        Toasty.error(context, message!!, Toast.LENGTH_SHORT, true).show()
    }

    fun vibrate(cancelVibrate: Boolean) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!vibrator!!.hasVibrator()) {
            return
        }
        if (cancelVibrate) {
            vibrator!!.cancel()
            return
        }
        vibrator!!.vibrate(
            VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    /**
     * Opens Url in custom chrome Tab
     *
     * @param activity        Pass activity
     * @param url             Where you need to pass url
     * @param customAnimation applies animation to custom chrome tab when true
     */
    fun openURL(activity: Activity?, url: String?, customAnimation: Boolean) {

        //Open Custom Chrome tab
        if (!TextUtils.isEmpty(url)) {
            if (isValidURL(url)) {
                val uri = Uri.parse(url)
                val intentBuilder = CustomTabsIntent.Builder()
                val params = CustomTabColorSchemeParams.Builder()
                    .setNavigationBarColor(ContextCompat.getColor(activity!!, R.color.black))
                    .setToolbarColor(ContextCompat.getColor(activity, R.color.black))
                    .setSecondaryToolbarColor(ContextCompat.getColor(activity, R.color.white))
                    .build()
                intentBuilder.setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, params)

                //set Animations
                if (customAnimation) {
                    intentBuilder.setStartAnimations(
                        context,
                        R.anim.slide_in_bottom,
                        R.anim.slide_out_top
                    )
                    intentBuilder.setExitAnimations(
                        context,
                        R.anim.slide_in_top,
                        R.anim.slide_out_bottom
                    )
                } else {
                    intentBuilder.setStartAnimations(
                        context,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    intentBuilder.setExitAnimations(
                        context,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                }
                val customTabsIntent = intentBuilder.build()
                customTabsIntent.launchUrl(activity, uri)
            }
        }
    }

    /**
     * Opens Url in custom chrome Tab
     *
     * @param activity Pass activity
     * @param url      Where you need to pass url
     */
    fun openCustomTab(activity: Activity?, url: String?) {

        //Open Custom Chrome tab
        if (!TextUtils.isEmpty(url)) {
            if (isValidURL(url)) {
                val uri = Uri.parse(url)
                val intentBuilder = CustomTabsIntent.Builder()
                val params = CustomTabColorSchemeParams.Builder()
                    .setNavigationBarColor(ContextCompat.getColor(activity!!, R.color.black))
                    .setToolbarColor(ContextCompat.getColor(activity, R.color.white))
                    .setSecondaryToolbarColor(ContextCompat.getColor(activity, R.color.white))
                    .build()
                intentBuilder.setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, params)
                val customTabsIntent = intentBuilder.build()
                customTabsIntent.launchUrl(activity, uri)
            }
        }
    }

    fun exitdialog(activity: Activity?) {
        val builder = AlertDialog.Builder(
            context
        )
        builder.setMessage("Are you sure you want to exit?")
        // builder.setIcon(R.drawable.menu_logout_icon);
        builder.setCancelable(false)
        builder.setPositiveButton("EXIT") { dialogInterface, i ->
            activity?.finishAffinity()
            System.exit(0)
        }
            .setNegativeButton("Cancel") { dialogInterface, i -> }
        val alertDialog = builder.create()
        alertDialog.show()
        val nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        //        nbutton.setTextColor(MaterialColors.getColor(context, R.attr.colorTitle, Color.BLACK));
        val pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        //        pbutton.setTextColor(ContextCompat.getColor(context, R.color.yellow_sea));
    }

    fun showUpdateAppPlayStoreDialog(activity: Activity?) {
        val builder = AlertDialog.Builder(
            context
        )
        builder.setMessage("Please Update App to latest version!")
        builder.setCancelable(false)
        builder.setPositiveButton("UPDATE") { dialogInterface, i ->
            val appPackageName = BuildConfig.APPLICATION_ID // package name of the app
            try {
                val viewIntent = Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
                context.startActivity(viewIntent)
            } catch (exception: Exception) {
                Toasty.normal(
                    context, "Please check latest version is installed!",
                    Toasty.LENGTH_LONG
                ).show()
                exception.printStackTrace()
            }
        }
            .setNegativeButton("EXIT") { dialogInterface, i ->
                if (activity != null) {
                    activity.finishAffinity()
                    System.exit(0)
                }
            }
        val alertDialog = builder.create()
        alertDialog.show()
        val nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        //        nbutton.setTextColor(MaterialColors.getColor(context, R.attr.colorTitle, Color.BLACK));
        val pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        //        pbutton.setTextColor(ContextCompat.getColor(context, R.color.yellow_sea));
//        pbutton.setBackgroundColor(ContextCompat.getColor(context, R.color.stratos));
    }

    fun showAppClosingDialog(activity: Activity) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Warning")
            .setMessage("Do you really want to close the app?")
            .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int -> activity.finish() }
            .setNegativeButton("No", null)
            .show()
    }

    fun getBitmap(context: Context?, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context!!, drawableId)
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawable) {
            getBitmap(drawable)
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }

    private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    fun setDrawableImage(
        context: Context?,
        resource: ImageView?,
        drawable: Drawable?,
        applyCircle: Boolean
    ) {
        val glide = Glide.with(context!!).load(drawable)
        if (applyCircle) {
            glide.apply(RequestOptions.circleCropTransform()).into(resource!!)
        } else {
            glide.into(resource!!)
        }
    }

    fun setLocalImage(
        context: Context?,
        resource: ImageView?,
        uri: Uri?,
        applyCircle: Boolean
    ) {
        val glide = Glide.with(context!!).load(uri)
        if (applyCircle) {
            glide.apply(RequestOptions.circleCropTransform()).into(resource!!)
        } else {
            glide.into(resource!!)
        }
    }

    fun animatingView(
        view: View?,
        @AnimRes id: Int
    ) {

        view?.animation = animError

    }

    /*
 Validation Functions
  */


    /*
 Email Validation Functions
   */
    fun validateEmail(
        view: EditText?
    ): Boolean {


        val mEmail: String = view?.text.toString().trim()
        val checkEmail = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        return if (mEmail.isEmpty()) {
            view?.error = "Field can not be empty"
            view?.animation = animError
            false
        } else if (!mEmail.matches(checkEmail)) {
            view?.error = "Invalid Email!"
            view?.animation = animError
            false
        } else {
            view?.error = null
            true
        }
    }


    /*
 Password Validation Functions
   */

    fun validatePassword(
        view: EditText?
    ): Boolean {
        val mPassword: String = view?.text.toString().trim()
        val checkPassword = "^" +
                "(?=.*[0-9])" +  //at least 1 digit
                "(?=.*[a-z])" +  //at least 1 lower case letter
                "(?=.*[A-Z])" +  //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +  //any letter
                "(?=.*[@#$%^&+=])" +  //at least 1 special character
                "(?=\\S+$)" +  //no white spaces
                ".{4,}" +  //at least 4 characters
                "$"


        return if (mPassword.isEmpty()) {
            view?.error = "Field can not be empty"
            view?.animation = animError
            false
        } else if (!mPassword.matches(checkPassword.toRegex())) {
            view?.error = "Password format is incorrect!"
            view?.animation = animError
            false
        } else {
            mConfirmPassword = mPassword
            view?.error = null
            true
        }
    }

    fun validateConfirmPassword(
        view: EditText?
    ): Boolean {
        val mPassword: String = view?.text.toString().trim()
        val checkPassword = "^" +
                "(?=.*[0-9])" +  //at least 1 digit
                "(?=.*[a-z])" +  //at least 1 lower case letter
                "(?=.*[A-Z])" +  //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +  //any letter
                "(?=.*[@#$%^&+=])" +  //at least 1 special character
                "(?=\\S+$)" +  //no white spaces
                ".{4,}" +  //at least 4 characters
                "$"


        return if (mPassword.isEmpty()) {
            view?.error = "Field can not be empty"
            view?.animation = animError
            false
        } else if (!mPassword.matches(checkPassword.toRegex())) {
            view?.error = "Password format is incorrect!"
            view?.animation = animError
            false
        } else if (mPassword != mConfirmPassword) {
            view?.error = "Confirm Password doesn't match"
            view?.animation = animError
            false
        } else {
            view?.error = null
            true
        }
    }


    fun capitalizeAllFirstLetters(name: String): String? {

        val array = name.lowercase().toCharArray()
        try {
            array[0] = array[0].uppercaseChar()
            for (i in 1 until array.size) {
                if (Character.isWhitespace(array[i - 1])) {
                    array[i] = array[i].uppercaseChar()
                }
            }
        } catch (e: Exception) {
        }

        return String(array)
    }

    fun validateUserName(
        view: EditText?
    ): Boolean {
        val mUserName: String = view?.text.toString().trim()

        return if (mUserName.isEmpty()) {
            view?.error = "Field can not be empty"
            view?.animation = animError
            false
        } else if (mUserName.length <=2) {
            view?.error = "User name must contain minimum 3 characters !"
            view?.animation = animError
            false
        } else {
            view?.setText(capitalizeAllFirstLetters(mUserName))
            view?.error = null
            true
        }
    }



    /**
     * Check if url is valid or nor
     *
     * @param url Where you need to pass url
     * @return
     */
    fun isValidURL(url: String?): Boolean {
        // Regex to check valid URL
        val regex = ("((http|https)://)(www.)?"
                + "[a-zA-Z0-9@:%._\\+~#?&//=]"
                + "{2,256}\\.[a-z]"
                + "{2,6}\\b([-a-zA-Z0-9@:%"
                + "._\\+~#?&//=]*)")

        // Compile the ReGex
        val p = Pattern.compile(regex)

        // If the string is empty
        // return false
        if (url == null) {
            return false
        }

        // Find match between given string
        // and regular expression
        // using Pattern.matcher()
        val m = p.matcher(url)

        // Return if the string
        // matched the ReGex
        return m.matches()
    }


}