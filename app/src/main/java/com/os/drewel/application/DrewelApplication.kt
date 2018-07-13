package com.os.drewel.application

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import android.support.v4.content.ContextCompat
import com.blankj.utilcode.util.Utils
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.QueueProcessingType
import com.os.drewel.R
import com.os.drewel.activity.WelcomeActivity
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.constant.Constants
import com.os.drewel.prefrences.Prefs
import net.danlew.android.joda.JodaTimeAndroid
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by monikab on 2/22/2018.
 */
class DrewelApplication : MultiDexApplication() {

    var options: DisplayImageOptions? = null
    private var retrofit: Retrofit? = null

    companion object {

        lateinit var drewelApplication: DrewelApplication

        fun getInstance(): DrewelApplication {
            return drewelApplication
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        drewelApplication = this
        /* initialize joda Time*/
        JodaTimeAndroid.init(this)
        /* initialize universal image loader*/
        initImageLoader(this)

        val defaultBitmap1 = com.os.drewel.utill.Utils.getInstance().drawableToBitmap(ContextCompat.getDrawable(this, R.mipmap.default_image)!!)
        options = DisplayImageOptions.Builder()
                .showImageOnLoading(BitmapDrawable(resources, defaultBitmap1))
                .showImageForEmptyUri(BitmapDrawable(resources, defaultBitmap1))
                .showImageOnFail(BitmapDrawable(resources, defaultBitmap1))
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build()

        Utils.init(this)
    }


    private fun initImageLoader(context: Context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        val config = ImageLoaderConfiguration.Builder(context)
        config.threadPriority(Thread.NORM_PRIORITY - 2)
        config.denyCacheImageMultipleSizesInMemory()
        config.diskCacheFileNameGenerator(Md5FileNameGenerator())
        config.diskCacheSize(100 * 1024 * 1024) // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO)
        config.writeDebugLogs() // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build())
    }


    fun getRequestQueue(): Retrofit {

        if (retrofit == null) {
            val logging = HttpLoggingInterceptor()
            // set your desired log level
            logging.level = HttpLoggingInterceptor.Level.BODY

            val builder = OkHttpClient.Builder()
            val okHttpClient = builder.connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build()

            retrofit = Retrofit.Builder()
                    .baseUrl(DrewelApi.BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
        }
        return retrofit as Retrofit
    }

    fun getLanguage(): String {
        val language = Prefs.getInstance(this).getPreferenceStringData(Prefs.prefs?.KEY_LANGUAGE
                ?: "")
        return if (language.isEmpty())
            Constants.LANGUAGE_ENGLISH
        else
            language
    }

    //    fun setLocale(lang: String, mContext: Context): Context {
//        val myLocale = Locale(lang)
//        val res = mContext.resources
//        //val dm = res.displayMetrics
//        val conf = res.configuration
//        conf.setLayoutDirection(myLocale)
//        return mContext.createConfigurationContext(conf)
//    }
    fun setLocale(lang: String, mContext: Context) {
//        var lang = lang
//        if (lang == null) {
//            lang = Constants.LANGUAGE_ENGLISH
//        }
//        val myLocale = Locale(lang)
//        val res = mContext.resources
//        val dm = res.displayMetrics
//        val conf = res.configuration
//        conf.locale = myLocale
//        conf.setLayoutDirection(myLocale)
//        res.updateConfiguration(conf, dm)

        val locale = Locale(lang, "US")
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config,
                baseContext.resources.displayMetrics)
    }

    public fun getStringByLocal(context: Activity, value: String): String {
        var configuration = Configuration(context.getResources().getConfiguration());
        configuration.setLocale(Locale("en"))
        return context.createConfigurationContext(configuration).getResources().getString(R.string.call);
    }

    fun convertToEnglish(float: Float): String {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        val formatter = nf as DecimalFormat
        formatter.applyPattern("#.###")
        val fString = formatter.format(float)
        return fString
    }


    fun logoutWhenAccountDeactivated(isDeactivated: String, context: Context) {
        if (isDeactivated == "1") {
            val pref = Prefs.getInstance(this)
            pref.setPreferenceStringData(pref.KEY_USER_ID, "")
            pref.setPreferenceStringData(pref.KEY_FIRST_NAME, "")
            pref.setPreferenceStringData(pref.KEY_LAST_NAME, "")
            pref.setPreferenceStringData(pref.KEY_EMAIL, "")
            pref.setPreferenceStringData(pref.KEY_PHOTO, "")
            pref.setPreferenceStringData(pref.KEY_ROLE_ID, "")
            val intent = Intent(context, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

}