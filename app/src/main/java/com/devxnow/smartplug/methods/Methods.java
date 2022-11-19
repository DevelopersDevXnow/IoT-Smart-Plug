package com.devxnow.smartplug.methods;

import static android.content.ContentValues.TAG;
import static android.content.Context.VIBRATOR_SERVICE;


import android.app.Activity;
import android.content.ContentResolver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.devxnow.smartplug.BuildConfig;
import com.devxnow.smartplug.R;
import com.google.android.material.color.MaterialColors;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import es.dmoral.toasty.Toasty;


public class Methods {

    private Context context;
    private Vibrator vibrator;
    private SecretKey key;
    private boolean isClicked = false;


    public Methods(Context context) {

        this.context = context;
    }


    public void logout(Activity activity) {
        try {
//            if (PlayerService.exoPlayer != null) {
//                Intent intent = new Intent(context, PlayerService.class);
//                intent.setAction(PlayerService.ACTION_STOP);
//                context.startService(intent);
//          }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public int getColumnWidth(int column, int grid_padding) {
        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, grid_padding, r.getDisplayMetrics());
        return (int) ((getScreenWidth() - ((column + 1) * padding)) / column);
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnWidth = point.x;
        return columnWidth;
    }

    public int getScreenHeight() {
        int height;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        height = point.y;
        return height;
    }


    public String getPathImage(Uri uri) {
        try {
            String filePath = "";
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            if (uri == null) {
                return null;
            }
            // try to retrieve the image from the media store first
            // this will only work for images selected from gallery
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String returnn = cursor.getString(column_index);
                cursor.close();

                if (returnn == null) {
                    String path = null, image_id = null;
                    Cursor cursor2 = context.getContentResolver().query(uri, null, null, null, null);
                    if (cursor2 != null) {
                        cursor2.moveToFirst();
                        image_id = cursor2.getString(0);
                        image_id = image_id.substring(image_id.lastIndexOf(":") + 1);
                        cursor2.close();
                    }

                    Cursor cursor3 = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
                    if (cursor3 != null) {
                        cursor3.moveToFirst();
                        path = cursor3.getString(cursor3.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        cursor3.close();
                    }
                    return path;
                }
                return returnn;
            }
            // this is our fallback here
            return uri.getPath();
        }
    }

    public String getFilename(Uri uri) {

        String result = null;
        if (uri.getScheme().equals("content")) {

            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {


                if (cursor != null && cursor.moveToFirst()) {

                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }


            } finally {
                cursor.close();

            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {

                result = result.substring(cut + 1);

            }

        }
        return result;
    }

    public String getfileExtension(Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    //create bitmap from view and returns it
    public Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
         //   canvas.drawColor(MaterialColors.getColor(context, R.attr.colorSubBackground, Color.BLACK));
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private File saveBitMap(Context context, View drawView) {

        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "KpQuotes"); // enter folder name to save image
        if (!pictureFileDir.exists()) {
            boolean isDirectoryCreated = pictureFileDir.mkdirs();
            if (!isDirectoryCreated)
                Log.i("ATG", "Can't create directory to save the image");
            return null;
        }
        String filename = pictureFileDir.getPath() + File.separator + System.currentTimeMillis() + ".jpg";
        File pictureFile = new File(filename);
        Bitmap bitmap = getBitmapFromView(drawView);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, oStream);
            oStream.flush();
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", "There was an issue saving the image.");
        }
        scanGallery(context, pictureFile.getAbsolutePath());
        return pictureFile;
    }

    private void scanGallery(Context cntx, String path) {
        try {
            MediaScannerConnection.scanFile(cntx, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Saves the image as PNG to the app's cache directory.
     *
     * @param image Bitmap to save.
     * @return Uri of the saved file or null
     */

    public Uri saveImage(Bitmap image) {
        //TODO - Should be processed in another thread
        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);

        } catch (IOException e) {
            Log.d(TAG, "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return uri;
    }


    /**
     * Shares the PNG image from Uri.
     *
     * @param uri Uri of image to share.
     */
    public void shareImageUri(Uri uri) {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "I liked this quote from KP App and wanted to share it with you. \n" +
                "\n" +
                "Get more insightful quotes daily on KP App. \n" +
                "Download App - https://app.keerthanprabhu.com");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Kp Quote");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setType("image/*");
        context.startActivity(Intent.createChooser(shareIntent, "Share Via"));

    }


    public static void setDrawableImage(Activity activity, ImageView resource, Drawable drawable, boolean applyCircle) {

        RequestBuilder<Drawable> glide = Glide.with(activity).load(drawable);
        if (applyCircle) {
            glide.apply(RequestOptions.circleCropTransform()).into(resource);
        } else {
            glide.into(resource);
        }

    }

    public void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawablesRelative()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(textView.getContext(), color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    public static void setLocalImage(Activity activity, ImageView resource, Uri uri, boolean applyCircle) {
        RequestBuilder<Drawable> glide = Glide.with(activity).load(uri);
        if (applyCircle) {
            glide.apply(RequestOptions.circleCropTransform()).into(resource);
        } else {
            glide.into(resource);
        }
    }


    public void showSnackBar(String message) {
        Toasty.success(context, message, Toast.LENGTH_SHORT, true).show();
    }

    public void showSnackBarError(String message) {
        Toasty.error(context, message, Toast.LENGTH_SHORT, true).show();
    }

    public void vibrate(Boolean cancelVibrate) {

        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);

        if (!vibrator.hasVibrator()) {

            return;
        }

        if (cancelVibrate) {

            vibrator.cancel();
            return;
        }

        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        );

    }


    /**
     * Check if url is valid or nor
     *
     * @param url Where you need to pass url
     * @return
     */

    public static boolean isValidURL(String url) {
        // Regex to check valid URL
        String regex = "((http|https)://)(www.)?"
                + "[a-zA-Z0-9@:%._\\+~#?&//=]"
                + "{2,256}\\.[a-z]"
                + "{2,6}\\b([-a-zA-Z0-9@:%"
                + "._\\+~#?&//=]*)";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the string is empty
        // return false
        if (url == null) {
            return false;
        }

        // Find match between given string
        // and regular expression
        // using Pattern.matcher()
        Matcher m = p.matcher(url);

        // Return if the string
        // matched the ReGex
        return m.matches();
    }


    /**
     * Opens Url in custom chrome Tab
     *
     * @param activity        Pass activity
     * @param url             Where you need to pass url
     * @param customAnimation applies animation to custom chrome tab when true
     */

    public void openURL(Activity activity, String url, boolean customAnimation) {

        //Open Custom Chrome tab

        if (!TextUtils.isEmpty(url)) {

            if (Methods.isValidURL(url)) {
                Uri uri = Uri.parse(url);
                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                CustomTabColorSchemeParams params = new CustomTabColorSchemeParams.Builder()
                        .setNavigationBarColor(ContextCompat.getColor(activity, R.color.black))
                        .setToolbarColor(ContextCompat.getColor(activity, R.color.black))
                        .setSecondaryToolbarColor(ContextCompat.getColor(activity, R.color.white))
                        .build();
                intentBuilder.setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, params);

                //set Animations
                if (customAnimation) {
                    intentBuilder.setStartAnimations(context, R.anim.slide_in_bottom, R.anim.slide_out_top);
                    intentBuilder.setExitAnimations(context, R.anim.slide_in_top, R.anim.slide_out_bottom);
                } else {

                    intentBuilder.setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left);
                    intentBuilder.setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right);
                }


                CustomTabsIntent customTabsIntent = intentBuilder.build();
                customTabsIntent.launchUrl(activity, uri);

            }

        }

    }

    /**
     * Opens Url in custom chrome Tab
     *
     * @param activity Pass activity
     * @param url      Where you need to pass url
     */
    public void openCustomTab(Activity activity, String url) {

        //Open Custom Chrome tab

        if (!TextUtils.isEmpty(url)) {

            if (Methods.isValidURL(url)) {

                Uri uri = Uri.parse(url);
                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                CustomTabColorSchemeParams params = new CustomTabColorSchemeParams.Builder()
                        .setNavigationBarColor(ContextCompat.getColor(activity, R.color.black))
                        .setToolbarColor(ContextCompat.getColor(activity, R.color.white))
                        .setSecondaryToolbarColor(ContextCompat.getColor(activity, R.color.white))
                        .build();
                intentBuilder.setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, params);


                CustomTabsIntent customTabsIntent = intentBuilder.build();
                customTabsIntent.launchUrl(activity, uri);


            }


        }


    }


    public void exitdialog(Activity activity) {


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to exit?");
       // builder.setIcon(R.drawable.menu_logout_icon);
        builder.setCancelable(false);
        builder.setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (activity != null)
                            activity.finishAffinity();
                        System.exit(0);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//        nbutton.setTextColor(MaterialColors.getColor(context, R.attr.colorTitle, Color.BLACK));
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        pbutton.setTextColor(ContextCompat.getColor(context, R.color.yellow_sea));
    }

    public void showUpdateAppPlayStoreDialog(Activity activity) {


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Please Update App to latest version!");
        builder.setCancelable(false);
        builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        final String appPackageName = BuildConfig.APPLICATION_ID; // package name of the app

                        try {
                            Intent viewIntent =
                                    new Intent("android.intent.action.VIEW",
                                            Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
                            context.startActivity(viewIntent);
                        } catch (Exception exception) {
                            Toasty.normal(context, "Please check latest version is installed!",
                                    Toasty.LENGTH_LONG).show();
                            exception.printStackTrace();
                        }


                    }
                })
                .setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (activity != null)
                            activity.finishAffinity();
                           System.exit(0);

                    }
                });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button nbutton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//        nbutton.setTextColor(MaterialColors.getColor(context, R.attr.colorTitle, Color.BLACK));
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        pbutton.setTextColor(ContextCompat.getColor(context, R.color.yellow_sea));
//        pbutton.setBackgroundColor(ContextCompat.getColor(context, R.color.stratos));


    }


}
