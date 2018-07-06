package com.os.drewel.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.KeyboardUtils
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.model.LatLng
import com.os.drewel.R
import com.os.drewel.adapter.DeliveryListAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.deliveryaddressresponsemodel.Address
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.AppRequestCodes
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.content_set_delivery_address_activity.*
import kotlinx.android.synthetic.main.delivery_address_activity.*


/**
 * Created by monikab on 3/23/2018.
 */
class DeliveryAddressActivity : BaseActivity(), View.OnClickListener {

    private var disposable: Disposable? = null

    private var deliverListAdapter: DeliveryListAdapter? = null
    private var addressList: MutableList<Address> = mutableListOf()

    private lateinit var itemClickDisposable: Disposable
    private lateinit var deleteClickDisposable: Disposable
    private val defaultAddressClickSubject = PublishSubject.create<Int>()
    private val deleteAddressClickSubject = PublishSubject.create<Int>()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var From = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delivery_address_activity)
        initView()
        setAdapter()
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        mMapView.setOnClickListener {
            val intent = Intent(this, DeliveryMapAddressActivity::class.java)
            startActivityForResult(intent, AppRequestCodes.DELIVER_ADDRESS_REQUEST_CODE)
        }
        /* when user click on adapter row*/
        if (intent != null) {
            if (intent.getIntExtra("TYPE", 1) == 2) {
                From = intent.getIntExtra("TYPE", 1)
            }
        }
        clickOfAdapterItem()
        setClickListeners()
        if (isNetworkAvailable())
            callGetAddressApi()
    }

    private fun clickOfAdapterItem() {
        itemClickDisposable = defaultAddressClickSubject.subscribe({ addressPosition ->
            callSetDefaultAddressApi(addressPosition)
        })
        deleteClickDisposable = deleteAddressClickSubject.subscribe({ addressPosition ->

            val logoutAlertDialog = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()
            logoutAlertDialog.setTitle(getString(R.string.app_name))
            logoutAlertDialog.setMessage(getString(R.string.want_to_delete_address))
            logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
                logoutAlertDialog.dismiss()
                if (addressList.isNotEmpty() && addressList[addressPosition].isDefault.equals("1")) {
                    Toast.makeText(this, getString(R.string.cant_delete_default_address), Toast.LENGTH_LONG).show()
                } else
                    callDeleteAddressApi(addressPosition)
            })
            logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
                logoutAlertDialog.dismiss()
            })
            logoutAlertDialog.show()
        })
    }

    private fun setAdapter() {
//        if (deliverListAdapter == null) {
        Log.e("addressList==", addressList.toString())
        deliverListAdapter = DeliveryListAdapter(this, addressList)
        deliverListAdapter!!.defaultAddressClickSubject = defaultAddressClickSubject
        deliverListAdapter!!.deleteAddressClickSubject = deleteAddressClickSubject
        deliveryAddressRecyclerView.layoutManager = LinearLayoutManager(this)
        deliveryAddressRecyclerView.adapter = deliverListAdapter

//        } else {
//            Log.e("addressList==1", addressList.toString())
//            deliveryAddressRecyclerView.adapter.notifyDataSetChanged()
//        }
    }

    private fun setClickListeners() {
        searchDeliveryAddress.setOnClickListener(this)
        deliveryAddressDoneBt.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.searchDeliveryAddress -> {
                try {
                    val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this)
                    startActivityForResult(intent, AppRequestCodes.PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: GooglePlayServicesRepairableException) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                } catch (e: GooglePlayServicesNotAvailableException) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.deliveryAddressDoneBt -> {
                if (From == 2) {
                    if (pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_TYPE).isNullOrEmpty() || pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_TYPE) == ("")) {
                        val intent = Intent(this, DeliveryDetailActivity::class.java)
                        intent.putExtra("TYPE", 2)
                        startActivity(intent)
                    }
                    finish()
                } else {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppRequestCodes.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(this, data)
                    Log.i("onActivityResult", "Place: " + place.name)
                    Log.i("onActivityResult", "Place: " + place.address)
                    Log.i("onActivityResult", "Place: " + place.latLng)
                    val logoutAlertDialog = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()
                    logoutAlertDialog.setTitle(getString(R.string.app_name))
                    logoutAlertDialog.setMessage(getString(R.string.want_to_defaultaddress))
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                        if (isNetworkAvailable())
                            callAddAddressApi(place.name.toString(), place.address.toString(), place.latLng.latitude, place.latLng.longitude, "", ",", "", "", "", "")

                    })
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                    })
                    logoutAlertDialog.show()


                }
                PlaceAutocomplete.RESULT_ERROR -> {

                    val status = PlaceAutocomplete.getStatus(this, data)
                    Log.i("onActivityResult", status.statusMessage)
                    Toast.makeText(this, status.statusMessage, Toast.LENGTH_SHORT).show()
                }
                RESULT_CANCELED -> // The user canceled the operation.
                    Log.e("onActivityResult", "canceled")
            }
        } else if (requestCode == AppRequestCodes.DELIVER_ADDRESS_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    if (data != null) {
                        val address = data.getStringExtra(AppIntentExtraKeys.ADDRESS)
                        val name = data.getStringExtra(AppIntentExtraKeys.ADDRESS_NAME)
                        val latLang: LatLng = data.getParcelableExtra(AppIntentExtraKeys.LATLNG)
                        val postalCode = data.getStringExtra(AppIntentExtraKeys.POSTAL_CODE) ?: ""
                        if (isNetworkAvailable())
                            callAddAddressApi(name, address, latLang.latitude, latLang.longitude, postalCode, ",", "", "", "", "")
//                        callIsAvailbaleAddAddressApi(name, address, latLang.latitude, latLang.longitude, postalCode, ",", "", "", "", "")
                    }
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.delivery_address_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        /*if (id == R.id.action_map) {
            val intent = Intent(this, DeliveryMapAddressActivity::class.java)
            startActivityForResult(intent, AppRequestCodes.DELIVER_ADDRESS_REQUEST_CODE)
            return true
        } else*/ if (id == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun callIsAvailbaleAddAddressApi(name: String, address: String, latitude: Double, longitude: Double, postalCode: String, delivery_address_type: String, user_name: String, mobile_number: String, landmark: String, full_address: String) {
        setDoneButtonVisibility(View.GONE, View.VISIBLE)
        val addDeliveryAddressRequest = HashMap<String, String>()
        addDeliveryAddressRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
//        addDeliveryAddressRequest["address"] = address
//        addDeliveryAddressRequest["name"] = name
        addDeliveryAddressRequest["latitude"] = latitude.toString()
        addDeliveryAddressRequest["longitude"] = longitude.toString()
        addDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
//        addDeliveryAddressRequest["zip_code"] = postalCode
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).is_delivery_place(addDeliveryAddressRequest)
        compositeDisposable.add(signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    if (result.response!!.status!!) {
                        if (isNetworkAvailable())
                            callAddAddressApi(name, address, latitude, longitude, postalCode, ",", "", "", "", "")
                    } else {
                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
                        setDoneButtonVisibility(View.VISIBLE, View.GONE)
                    }
                }, { error ->
                    setDoneButtonVisibility(View.VISIBLE, View.GONE)
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    private fun callAddAddressApi(name: String, address: String, latitude: Double, longitude: Double, postalCode: String, delivery_address_type: String, user_name: String, mobile_number: String, landmark: String, full_address: String) {
        setDoneButtonVisibility(View.GONE, View.VISIBLE)
        val addDeliveryAddressRequest = HashMap<String, String>()
        addDeliveryAddressRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        addDeliveryAddressRequest["address"] = address
        addDeliveryAddressRequest["name"] = name
        addDeliveryAddressRequest["latitude"] = latitude.toString()
        addDeliveryAddressRequest["longitude"] = longitude.toString()
        addDeliveryAddressRequest["is_default"] = "1"
        addDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
        addDeliveryAddressRequest["zip_code"] = postalCode
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).addAddress(addDeliveryAddressRequest)
        compositeDisposable.add(signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    if (result.response!!.status!!) {
                        if (addressList.isNotEmpty()) {
                            addressList[deliverListAdapter!!.defualtAddressPos].isDefault = "0"
                            deliverListAdapter!!.notifyItemChanged(deliverListAdapter!!.defualtAddressPos)
                        }
                        val addressObj = Address()
                        addressObj.isDefault = "1"
                        addressObj.id = result.response!!.data!!.addressId
                        addressObj.name = name
                        addressObj.latitude = latitude.toString()
                        addressObj.longitude = longitude.toString()
                        addressObj.address = address

                        addressList.add(0, addressObj)

                        saveDefaultAddressToPref(result.response!!.data!!.addressId!!, address, name, latitude.toString(), longitude.toString(), "", "", "", "", "", "")
                        setAdapter()
                        setDoneButtonVisibility(View.VISIBLE, View.GONE)
                    } else {
                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
                        setDoneButtonVisibility(View.VISIBLE, View.GONE)
                    }
                }, { error ->
                    setDoneButtonVisibility(View.VISIBLE, View.GONE)
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    private fun saveDefaultAddressToPref(id: String, address: String, name: String, latitude: String, longitude: String, username: String, phoneNumber: String, fullAddress: String, landmark: String, delivery_address_type: String, zip_code: String) {
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_ID, id)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS, address)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_NAME, name)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LATITUDE, latitude)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LONGITUDE, longitude)

        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_USERNAME, username)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_PHONE_NUMBER, phoneNumber)
        pref!!.setPreferenceStringData(pref!!.KEY_FULL_DELIVERY_ADDRESS, fullAddress)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_lANDMARK, landmark)

        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_TYPE, delivery_address_type)
        pref!!.setPreferenceStringData(pref!!.KEY_ZIP_CODE, zip_code)
    }

    private fun callGetAddressApi() {
        setDoneButtonVisibility(View.GONE, View.VISIBLE)
        val addDeliveryAddressRequest = HashMap<String, String>()
        addDeliveryAddressRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        addDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getAddressList(addDeliveryAddressRequest)
        compositeDisposable.add(signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if (result.response!!.status!!) {
                        addressList = result.response!!.data!!.address!!
                        if (addressList.isEmpty())
                            setDoneButtonVisibility(View.GONE, View.GONE)
                        else
                            setDoneButtonVisibility(View.VISIBLE, View.GONE)
                        setAdapter()
                    } else {
                        setDoneButtonVisibility(View.GONE, View.GONE)
                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
                    }
                }, { error ->
                    setDoneButtonVisibility(View.GONE, View.GONE)
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    private fun callSetDefaultAddressApi(addressPosition: Int) {
        setDoneButtonVisibility(View.GONE, View.VISIBLE)
        val setDefaultDeliveryAddressRequest = HashMap<String, String>()
        setDefaultDeliveryAddressRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        setDefaultDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
        setDefaultDeliveryAddressRequest["address_id"] = addressList[addressPosition].id!!
        val defaultAddressObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).setDefaultAddress(setDefaultDeliveryAddressRequest)
        compositeDisposable.add(defaultAddressObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setDoneButtonVisibility(View.VISIBLE, View.GONE)
                    if (result.response!!.status!!) {

                        addressList[deliverListAdapter!!.defualtAddressPos].isDefault = "0"
                        deliverListAdapter!!.notifyItemChanged(deliverListAdapter!!.defualtAddressPos)

                        addressList[addressPosition].isDefault = "1"
                        deliverListAdapter!!.notifyItemChanged(addressPosition)

                        saveDefaultAddressToPref(addressList[addressPosition].id!!, addressList[addressPosition].address!!, addressList[addressPosition].name!!, addressList[addressPosition].latitude!!, addressList[addressPosition].longitude!!, addressList[addressPosition].username
                                ?: "", addressList[addressPosition].mobileNumber
                                ?: "", addressList[addressPosition].fullAddress
                                ?: "", addressList[addressPosition].landmark
                                ?: "", addressList[addressPosition].delivery_address_type
                                ?: "", addressList[addressPosition].zip_code ?: "")
                    } else
                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
                }, { error ->
                    setDoneButtonVisibility(View.VISIBLE, View.GONE)
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    private fun callDeleteAddressApi(addressPosition: Int) {
        progressBar.visibility = View.VISIBLE
        val setDefaultDeliveryAddressRequest = HashMap<String, String>()
        setDefaultDeliveryAddressRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        setDefaultDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
        setDefaultDeliveryAddressRequest["address_id"] = addressList[addressPosition].id!!
        val defaultAddressObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).deleteAddress(setDefaultDeliveryAddressRequest)
        compositeDisposable.add(defaultAddressObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    progressBar.visibility = View.GONE
                    if (result.response!!.status!!) {
                        Log.e("addressPosition==>", addressPosition.toString())
                        addressList.removeAt(addressPosition)
                        deliverListAdapter!!.notifyItemRemoved(addressPosition)
                    } else
                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
                }, { error ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteClickDisposable.dispose()
        itemClickDisposable.dispose()
        compositeDisposable.dispose()
    }


    private fun setDoneButtonVisibility(visibility: Int, progressVisibility: Int) {
        deliveryAddressDoneBt.visibility = visibility
        progressBar.visibility = progressVisibility
    }

//    override fun onResume() {
//        super.onResume()
//        KeyboardUtils.hideSoftInput(this)
//    }


}