package com.os.drewel.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.os.drewel.R
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.AppRequestCodes
import com.os.drewel.utill.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.delivery_map_address_activity.*
import java.util.*

/**
 * Created by sharukhb on 3/20/2018.
 */

class DeliveryMapAddressActivity : BaseActivity(), View.OnClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener, LocationListener {

    private var googleMap: GoogleMap? = null
    private var currentLocationMarker: Marker? = null
    private var locationManager: LocationManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.delivery_map_address_activity)

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        mapDoneButton.setOnClickListener(this)
        searchDeliveryAddress.setOnClickListener(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mMapView!!.onCreate(savedInstanceState)
        mMapView!!.getMapAsync(this)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == android.R.id.home) {
            finish()
        }

        return true
    }

    public override fun onResume() {
        super.onResume()
        mMapView!!.onResume()

        if (!isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsRequest()
        }
    }

    public override fun onPause() {
        super.onPause()
        mMapView!!.onPause()
    }


    public override fun onDestroy() {
        super.onDestroy()
        mMapView!!.onDestroy()
        locationManager!!.removeUpdates(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView!!.onLowMemory()
    }

    public override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView!!.onSaveInstanceState(outState)
    }

    override fun onClick(view: View) {

        if (view.id == mapDoneButton.id) {
            showLoading()
            geoLocationAddressAPI(LatLng(this.googleMap!!.cameraPosition.target.latitude, this.googleMap!!.cameraPosition.target.longitude))

        } else if (view.id == R.id.searchDeliveryAddress) {
            try {
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this)
                startActivityForResult(intent, AppRequestCodes.PLACE_AUTOCOMPLETE_REQUEST_CODE)
            } catch (e: GooglePlayServicesRepairableException) {
                Utils.getInstance().showToast(this,e.message!!)
            } catch (e: GooglePlayServicesNotAvailableException) {
                Utils.getInstance().showToast(this,e.message!!)
            }

        }

        /* else if (view.id == ivMyLocation!!.id) {

            if (currentLocationMarker != null) {
                val latLng = currentLocationMarker!!.position
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                googleMap!!.animateCamera(cameraUpdate)
            }
        }*/
    }

    override fun onMapReady(googleMap: GoogleMap) {

        val ivPin = ImageView(this)
        ivPin.setImageResource(R.mipmap.address)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        val density = resources.displayMetrics.density
        params.bottomMargin = (12 * density).toInt()
        ivPin.layoutParams = params
        mMapView!!.addView(ivPin)

        this.googleMap = googleMap

        this.googleMap!!.isBuildingsEnabled = false
        this.googleMap!!.isTrafficEnabled = false

        this.googleMap!!.uiSettings.isCompassEnabled = false
        this.googleMap!!.uiSettings.isMapToolbarEnabled = false
        this.googleMap!!.uiSettings.isTiltGesturesEnabled = false
        this.googleMap!!.uiSettings.isZoomControlsEnabled = false
        this.googleMap!!.uiSettings.isRotateGesturesEnabled = false

        //        this.googleMap.setOnMarkerClickListener(this);
        this.googleMap!!.setOnCameraMoveListener(this)
        this.googleMap!!.setOnCameraIdleListener(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 10)
            }

        } else {

            if (isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                if (locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {

                    //                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    onLocationChanged(locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER))

                } else {
                    showLoading()
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this) //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
                    locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this) //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
                }

            } else {

                showGpsRequest()
            }
        }
    }

    private fun isProviderEnabled(provider: String): Boolean {
        Log.d("isProviderEnabled", ">>isProviderEnabled")

        if (locationManager != null) {
            if (locationManager!!.isProviderEnabled(provider)) {
                return true
            }
        }

        return false
    }

    private fun showGpsRequest() {

        val mAlertDialog = android.support.v7.app.AlertDialog.Builder(this)
        mAlertDialog.setTitle(R.string.location_service_disabled_title)
                .setMessage(R.string.location_service_disabled_text)
                .setPositiveButton(R.string.enable, { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }).show()
    }

    override fun onCameraIdle() {

        Log.d("---------onCameraIdle", "------------------------")
        Log.d("latitude", ">>" + this.googleMap!!.cameraPosition.target.latitude)
        Log.d("longitude", ">>" + this.googleMap!!.cameraPosition.target.longitude)
        Log.d("zoom", ">>" + this.googleMap!!.cameraPosition.zoom)
        Log.d("--------------------", "------------------------")
    }

    override fun onCameraMove() {

        //        Log.d("---------onCameraMove", "------------------------");
        //        Log.d("latitude", ">>" + this.googleMap.getCameraPosition().target.latitude);
        //        Log.d("longitude", ">>" + this.googleMap.getCameraPosition().target.longitude);
        //        Log.d("zoom", ">>" + this.googleMap.getCameraPosition().zoom);
        //        Log.d("--------------------", "------------------------");
    }

    override fun onLocationChanged(location: Location) {

        hideLoading()
        val latLng = LatLng(location.latitude, location.longitude)

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14f)

        Log.d("latitude", ">>" + this.googleMap!!.cameraPosition.target.latitude)
        Log.d("longitude", ">>" + this.googleMap!!.cameraPosition.target.longitude)
        Log.d("zoom", ">>" + this.googleMap!!.cameraPosition.zoom)

        if (currentLocationMarker != null) {
            currentLocationMarker!!.remove()
        }

        /*   currentLocationMarker = this.googleMap!!.addMarker(MarkerOptions().position(latLng)
                   .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).anchor(0.5f, 0.5f))*/
        googleMap!!.animateCamera(cameraUpdate)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            showLoading()
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this) //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this) //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }
    }

    private fun geoLocationAddressAPI(latLng: LatLng) {

        Observable.fromCallable {
            val geocoder = Geocoder(this, Locale.ENGLISH)
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { addresses ->
                            if (/*addresses != null && */addresses.size > 0) {

                                val address = addresses[0]
                                var fullAddress = ""

                                Log.d("addresses", ">>" + Arrays.toString(addresses.toTypedArray()))
                                Log.d("addresses", ">>" + Arrays.toString(addresses.toTypedArray()))


                                for (i in 0 until address.maxAddressLineIndex + 1) {

                                    Log.d("getAddressLine$i", ">>" + address.getAddressLine(i))

                                    if (i == 0)
                                        fullAddress = address.getAddressLine(i)
                                    else if (i < address.maxAddressLineIndex)
                                        fullAddress += ", " + address.getAddressLine(i)
                                    else
                                        break
                                }
                                val subThoroughfare: String
                                val thoroughfare: String
                                val subLocality: String
                                val locality: String

                                subThoroughfare = if (address.subThoroughfare != null) address.subThoroughfare + " " else ""

                                thoroughfare = if (address.thoroughfare != null) address.thoroughfare + " " else ""

                                subLocality = if (address.subLocality != null) address.subLocality + " " else ""

                                locality = if (address.locality != null) address.locality else ""


                                val name = subThoroughfare + thoroughfare + subLocality + locality
                                hideLoading()
                                val intent = Intent()
                                intent.putExtra(AppIntentExtraKeys.ADDRESS, fullAddress)
                                intent.putExtra(AppIntentExtraKeys.ADDRESS_NAME, name)
                                intent.putExtra(AppIntentExtraKeys.POSTAL_CODE, address.postalCode)
                                intent.putExtra(AppIntentExtraKeys.LATLNG, latLng)
                                setResult(Activity.RESULT_OK, intent)
                                finish()

                            } else {
                                hideLoading()
                                Utils.getInstance().showToast(this,getString(R.string.error_address_not_found))
                            }
                        },
                        { error ->
                            hideLoading()
                            Log.e("TAG", "{$error.message}")
                        },
                        { Log.d("TAG", "completed") }
                )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == AppRequestCodes.PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            when (resultCode) {
                Activity.RESULT_OK -> {

                    val place = PlaceAutocomplete.getPlace(this, data!!)

                    Log.i("onActivityResult", "Place: " + place.name)
                    Log.i("onActivityResult", "Place: " + place.address)
                    Log.i("onActivityResult", "Place: " + place.latLng)

                    val latLng = LatLng(place.latLng.latitude, place.latLng.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14f)
                    googleMap!!.animateCamera(cameraUpdate)
                    searchDeliveryAddress.text = place.name

                }
                PlaceAutocomplete.RESULT_ERROR -> {

                    val status = PlaceAutocomplete.getStatus(this, data!!)
                    Log.i("onActivityResult", status.statusMessage)

                }
                Activity.RESULT_CANCELED -> // The user canceled the operation.
                    Log.e("onActivityResult", "canceled")
            }
        }
    }

    companion object {

        private const val MIN_TIME: Long = 400
        private const val MIN_DISTANCE = 1000f
    }
}