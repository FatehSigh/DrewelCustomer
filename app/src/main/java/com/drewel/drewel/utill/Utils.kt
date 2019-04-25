package com.drewel.drewel.utill

import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.drewel.drewel.apicall.responsemodel.googledirectionresultmodel.DirectionResults
import com.drewel.drewel.apicall.responsemodel.googledirectionresultmodel.Location
import com.drewel.drewel.apicall.responsemodel.googledirectionresultmodel.Steps
import com.drewel.drewel.constant.Constants
import com.drewel.drewel.prefrences.Prefs
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Hours
import org.joda.time.format.DateTimeFormat
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by monikab on 2/23/2018.
 */
class Utils private constructor() {
    var IMAGE_MAX_SIZE = 1024
    var toast: Toast? = null

    companion object {

        var utills: Utils? = null

        fun getInstance(): Utils {
            if (utills == null)
                utills = Utils()
            return utills as Utils
        }
    }

    fun dpToPix(mContext: Context?, value: Int): Int {
        var calculatedValue = value
        try {
            if (mContext != null)
                calculatedValue = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), mContext.resources.displayMetrics))
        } catch (e: Exception) {
        }

        return calculatedValue

    }

    fun pixToDP(mContext: Context, value: Int): Float {
        val r = mContext.resources
        var calculatedValue = value.toFloat()
        try {
            calculatedValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), r.displayMetrics)
        } catch (e: Exception) {
        }

        return calculatedValue

    }


    fun showToast(mContext: Context?, Message: String) {
        try {
            if (mContext != null) {
                if (toast != null) {
                    toast!!.cancel()
                }
                toast = Toast.makeText(mContext, Message, Toast.LENGTH_SHORT)
                toast!!.show()
            } else
                Log.e("tag", "context is null at showing toast.")
        } catch (e: Exception) {
            Log.e("tag", "context is null at showing toast.", e)
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap: Bitmap?

        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap!!)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    fun saveBitmapToExternalStorage(bitmap: Bitmap?, name: String): String {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/Android/data/com.os.drewel.activity/cache/image")
        myDir.mkdirs()
        val file = File(myDir, name)
        Log.i("file ", "" + file)
        if (file.exists())
            file.delete()
        try {
            val out = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return myDir.toString() + "/" + name
    }


    fun getCompressImagePath(uri: Uri, mContext: Context): String? {
        var `in`: InputStream?
        val returnedBitmap: Bitmap?
        val mContentResolver: ContentResolver
        // val getImagePath = ""
        try {
            mContentResolver = mContext.contentResolver
            `in` = mContentResolver.openInputStream(uri)
            //Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`in`, null, o)
            `in`!!.close()
            var scale = 1
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = Math.pow(2.0, Math.round(Math.log(IMAGE_MAX_SIZE / Math.max(o.outHeight, o.outWidth).toDouble()) / Math.log(0.5)).toInt().toDouble()).toInt()
            }

            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            `in` = mContentResolver.openInputStream(uri)
            val bitmap: Bitmap? = BitmapFactory.decodeStream(`in`, null, o2)
            `in`!!.close()

            //First check
            val ei = ExifInterface(uri.path)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    returnedBitmap = rotateImage(bitmap, 90f)
                    //Free up the memory
                    bitmap!!.recycle()
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    returnedBitmap = rotateImage(bitmap, 180f)
                    //Free up the memory
                    bitmap!!.recycle()
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    returnedBitmap = rotateImage(bitmap, 270f)
                    //Free up the memory
                    bitmap!!.recycle()
                }
                else -> returnedBitmap = bitmap
            }
            val root = Environment.getExternalStorageDirectory().toString()
            val myDir = File("$root/Android/data/com.os.pikpak.activity/cache/image")
            myDir.mkdirs()

            val tsLong = System.currentTimeMillis()
            val timeStamp = tsLong.toString()
            val fname = timeStamp + "_" + ".jpg"
            val file = File(myDir, fname)
            Log.i("file ", "" + file)
            if (file.exists())
                file.delete()
            try {
                val out = FileOutputStream(file)
                returnedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

//compressImagePath = image_path;

            return myDir.toString() + "/" + fname
        } catch (e: FileNotFoundException) {
            //  L.e(e);
        } catch (e: IOException) {
            //L.e(e);
        }

        return null
    }

    private fun rotateImage(source: Bitmap?, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source!!, 0, 0, source.width, source.height, matrix, true)
    }


    fun changeTimeToRelativeTime(timeStamp: Long): String {

        // it comes out like this 2013-08-31 15:55:22 so adjust the date format
        return try {

            DateUtils.getRelativeTimeSpanString(timeStamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
        } catch (e: Exception) {
            ""
        }

    }

    fun getTimeStampFromDate(str_date: String): Long {
        var timestamp: Long = 0
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = formatter.parse(str_date) as Date
            timestamp = date.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return timestamp
    }


    fun convertTimeFormat(time: String, fromFormat: String, toFormat: String): String {
        var timea = ""
        try {
            val sdf = SimpleDateFormat(fromFormat, Locale.getDefault())
            val dateObj = sdf.parse(time)
            println(dateObj)
            timea = SimpleDateFormat(toFormat, Locale.getDefault()).format(dateObj)
            return timea
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timea
    }

    fun convertTimeFormatEnglish(time: String, fromFormat: String, toFormat: String): String {
        var timea = ""
        try {
            val sdf = SimpleDateFormat(fromFormat, Locale(Constants.LANGUAGE_ENGLISH))
            val dateObj = sdf.parse(time)
            println(dateObj)
            timea = SimpleDateFormat(toFormat, Locale(Constants.LANGUAGE_ENGLISH)).format(dateObj)
            return timea
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return timea
    }

    /* get hour difference between current time and created time , timezone UTC*/

    fun getHourDifferenceBetweenTwoDate(createdTime: String): Int {

        val dtCurrentTime = DateTime(Constants.timeZone)

        val outputFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(Constants.timeZone)

        val dtCreatedTime = outputFormatter.parseDateTime(createdTime)
        val hoursDifference = Hours.hoursBetween(dtCreatedTime, dtCurrentTime)
        return hoursDifference.hours
    }

    fun convertTimeFormatAndTimeZone(time: String, fromFormat: String, toFormat: String): String {
        var timea = ""
        try {
            val fromDateFormat = DateTimeFormat.forPattern(fromFormat).withZone(Constants.timeZone)

            val toDateFormat = DateTimeFormat.forPattern(toFormat).withZone(DateTimeZone.getDefault())
            val jodaTime = fromDateFormat.parseDateTime(time)

            timea = jodaTime.toString(toDateFormat)
            return timea
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return timea
    }


    fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val strOrigin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val strDest = "destination=" + dest.latitude + "," + dest.longitude

        // Sensor enabled
        val sensor = "sensor=false"

        // Building the parameters to the web service
        val parameters = "$strOrigin&$strDest&$sensor"

        // Output format
        val output = "json"

        // Building the url to the web service

        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }

    fun getStepsToDrawPolyline(directionResults: DirectionResults): ArrayList<LatLng> {
        val routeList = ArrayList<LatLng>()
        if (directionResults.routes!!.isNotEmpty()) {
            var decodeList: ArrayList<LatLng>
            val routeA = directionResults.routes[0]
            if (routeA.legs!!.isNotEmpty()) {
                val steps = routeA.legs[0].steps
                var step: Steps
                var location: Location?
                var polyline: String?
                if (steps != null) {
                    for (i in steps.indices) {
                        step = steps.get(i)
                        location = step.start_location
                        routeList.add(LatLng(location!!.lat, location.lng))
                        Log.i("zacharia", "Start Location :" + location.lat + ", " + location.lng)
                        polyline = step.polyline!!.points
                        decodeList = RouteDecode.decodePoly(polyline!!)
                        routeList.addAll(decodeList)
                        location = step.end_location
                        routeList.add(LatLng(location!!.lat, location.lng))
                    }
                }
            }
        }
        return routeList
    }


    private fun updateResourcesLocale(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)

    }


    private fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }


    fun updateBaseContextLocale(context: Context): Context {
        val language = Prefs.getInstance(context).getPreferenceStringData(Prefs.prefs!!.KEY_LANGUAGE)
        val locale = Locale(language)
        Locale.setDefault(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResourcesLocale(context, locale)
        }
        return updateResourcesLocaleLegacy(context, locale)
    }

    private val WIDTH_INDEX = 0
    private val HEIGHT_INDEX = 1

    fun getScreenSize(context: Context): IntArray {
        val widthHeight = IntArray(2)
        widthHeight[WIDTH_INDEX] = 0
        widthHeight[HEIGHT_INDEX] = 0

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.getDefaultDisplay()

        val size = Point()
        display.getSize(size)
        widthHeight[WIDTH_INDEX] = size.x
        widthHeight[HEIGHT_INDEX] = size.y

        if (!isScreenSizeRetrieved(widthHeight)) {
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            widthHeight[0] = metrics.widthPixels
            widthHeight[1] = metrics.heightPixels
        }

        // Last defense. Use deprecated API that was introduced in lower than API 13
        if (!isScreenSizeRetrieved(widthHeight)) {
            widthHeight[0] = display.getWidth() // deprecated
            widthHeight[1] = display.getHeight() // deprecated
        }

        return widthHeight
    }

    private fun isScreenSizeRetrieved(widthHeight: IntArray): Boolean {
        return widthHeight[WIDTH_INDEX] != 0 && widthHeight[HEIGHT_INDEX] != 0
    }


    fun isAppIsInBackground(context: Context): Boolean {

        var isInBackground = false
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {

            val runningProcesses = am.getRunningAppProcesses()
            for (processInfo in runningProcesses) {
                if (processInfo.importance === ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.getPackageName()) {
                            isInBackground = true
                            break
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo.get(0).topActivity
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = true
                return isInBackground
            }
        }
        return isInBackground
    }


    /**
     * check if app is in background or foreground
     *
     * @param mContext
     * @return
     */
    fun isAppForground(mContext: Context): Boolean {

        val am = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.getRunningTasks(1)
        if (!tasks.isEmpty()) {
            val topActivity = tasks.get(0).topActivity
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                return false
            }
        }

        return true
    }

    fun deleteCache(context:Context) {
        try
        {
            val dir = context.cacheDir
            deleteDir(dir)
        }
        catch (e:Exception) {
            e.printStackTrace()
        }
    }
    private fun deleteDir(dir:File):Boolean {
        if (dir != null && dir.isDirectory)
        {
            val children = dir.list()
            for (i in children.indices)
            {
                val success = deleteDir(File(dir, children[i]))
                if (!success)
                {
                    return false
                }
            }
            return dir.delete()
        }
        else if (dir != null && dir.isFile)
        {
            return dir.delete()
        }
        else
        {
            return false
        }
    }


}