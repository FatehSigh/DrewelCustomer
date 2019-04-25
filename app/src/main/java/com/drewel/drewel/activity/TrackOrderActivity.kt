package com.drewel.drewel.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.MenuItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.drewel.drewel.R
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.AppIntentExtraKeys
import com.drewel.drewel.utill.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_toolbar.*
import kotlinx.android.synthetic.main.layout_track_order.*
import java.util.*
import java.util.concurrent.TimeUnit


class TrackOrderActivity : BaseActivity(), OnMapReadyCallback {


    private lateinit var googleMap: GoogleMap
    private var destinationLatitude = ""
    private var destinationLongitude = ""
    private var deliveryBoyId = ""
    private lateinit var driverCurrentLocationLatLng: LatLng
    private lateinit var deliveryLocationLatLng: LatLng
    private lateinit var driverCurrentMarker: Marker
    private var locationUpdateDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_track_order)
        initView()
        trackOrderMapView.onCreate(savedInstanceState)
        trackOrderMapView.onResume() // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        trackOrderMapView.getMapAsync(this)
    }

    /* set toolbar and back button*/
    private fun initView() {
        toolbarTitleTv.text = getString(R.string.track_order)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        getDataFromIntent()
    }

    private fun getDataFromIntent() {
        destinationLatitude = intent.getStringExtra(AppIntentExtraKeys.DELIVERY_ADDRESS_LATITUDE)
        destinationLongitude = intent.getStringExtra(AppIntentExtraKeys.DELIVERY_ADDRESS_LONGITUDE)
        deliveryBoyId = intent.getStringExtra(AppIntentExtraKeys.DELIVERY_BOY_ID)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        /* finish activity if user tap on action bar back button*/
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onMapReady(mMap: GoogleMap) {
        googleMap = mMap
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        checkRequestPermission()
        setMarkers()
    }

    /* set marker for destination and driver current location*/
    private fun setMarkers() {
        driverCurrentLocationLatLng = LatLng(destinationLatitude.toDouble(), destinationLongitude.toDouble())
        driverCurrentMarker = googleMap.addMarker(MarkerOptions().position(driverCurrentLocationLatLng).anchor(0.5f, 0.5f))

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(driverCurrentLocationLatLng, 14f)

        deliveryLocationLatLng = LatLng(destinationLatitude.toDouble(), destinationLongitude.toDouble())
        googleMap.addMarker(MarkerOptions().position(deliveryLocationLatLng).anchor(0.5f, 0.5f))

        googleMap.moveCamera(cameraUpdate)

        getDriverLocation()

    }

    /* get driver location in each 30 seconds */
    private fun getDriverLocation() {
        locationUpdateDisposable = Observable.interval(0, 30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (isNetworkAvailable())
                            callDriverLocationApi()
                        },
                        { error -> Log.e("TAG", "{$error.message}") },
                        { Log.d("TAG", "completed") })
    }

    private fun checkRequestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            googleMap.isMyLocationEnabled = true
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return
            googleMap.isMyLocationEnabled = true
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /* get distance and direction between destination and driver current location*/
    private fun callGetDirectionApi() {
        val directionUrl = Utils.getInstance().getDirectionsUrl(driverCurrentLocationLatLng, deliveryLocationLatLng)
        val getDirectionObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getGoogleDirection(directionUrl)
        getDirectionObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ directionResults ->
                    val routeList = Utils.getInstance().getStepsToDrawPolyline(directionResults)
                    if (routeList.size > 0) {
                        val rectLine = PolylineOptions().width(10f).color(Color.RED)
                        // Adding route on the map
                        rectLine.addAll(routeList)
                        googleMap.addPolyline(rectLine)
                    }
                }, { error ->
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    /* call api to get driver current location*/
    private fun callDriverLocationApi() {

        val driverLocationRequest = HashMap<String, String>()
        driverLocationRequest["delivery_boy_id"] = deliveryBoyId
        driverLocationRequest["language"] = DrewelApplication.getInstance().getLanguage()
        driverLocationRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        val driverLocationObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getDriverLocation(driverLocationRequest)
        driverLocationObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    //  setProgressState(View.GONE)
                    if (result.response?.status!!) {

                        result.response?.data?.location?.latitude?.let {
                            val latitude = result.response?.data?.location?.latitude!!.toDouble()
                            val longitude = result.response?.data?.location?.longitude!!.toDouble()

                            driverCurrentLocationLatLng = LatLng(latitude, longitude)
                            driverCurrentMarker.position = driverCurrentLocationLatLng
                            /*   val builder = LatLngBounds.Builder()
                           builder.include(driverCurrentLocationLatLng)
                           builder.include(LatLng(latitude, longitude))
                           val bounds = builder.build()


                           val padding = 80 // offset from edges of the map in pixels
                           val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)

                           googleMap.moveCamera(cu)
                           googleMap.animateCamera(cu)*/
                            if (isNetworkAvailable())
                                callGetDirectionApi()
                        }
                    }
                }, { error ->
                    // setProgressState(View.GONE)
                    com.drewel.drewel.utill.Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                )
    }

    /* private fun setProgressState(visibility: Int) {
         //    progressBar.visibility = visibility
     }*/

    override fun onStop() {
        super.onStop()

        locationUpdateDisposable?.dispose()
    }
}