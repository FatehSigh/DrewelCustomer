package com.os.drewel.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.os.drewel.R
import com.os.drewel.constant.AppIntentExtraKeys
import kotlinx.android.synthetic.main.content_delivery_details_activity.*
import kotlinx.android.synthetic.main.delivery_details_activity.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.widget.ImageViewCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.model.LatLng
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.responsemodel.deliveryaddressresponsemodel.Address
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppRequestCodes
import com.os.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


/**
 * Created by monikab on 4/4/2018.
 */
class DeliveryDetailActivity : BaseActivity(), View.OnClickListener {

    var from = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delivery_details_activity)
        initView()
    }


    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        if (intent != null)
            from = intent.getIntExtra("TYPE", 0)
        if (from==3) {
            fulladdressEditText.visibility = View.VISIBLE
            view_fulladdress.visibility = View.VISIBLE
        }
//                supportActionBar!!.setDisplayHomeAsUpEnabled(false)
//                supportActionBar!!.setDisplayShowHomeEnabled(false)
        saveDeliveryDetailBt.setOnClickListener(this)
        addressTv.text = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS)
        setSelector(APARTMENT)
        fulladdressEditText.setOnClickListener(this)
        rl_house.setOnClickListener(this)
        rl_apartment.setOnClickListener(this)
        rl_office.setOnClickListener(this)
    }
var place:Place?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppRequestCodes.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    place = PlaceAutocomplete.getPlace(this, data)
                    Log.i("onActivityResult", "Place: " + place!!.name)
                    Log.i("onActivityResult", "Place: " + place!!.address)
                    Log.i("onActivityResult", "Place: " + place!!.latLng)
                    val logoutAlertDialog = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()
                    logoutAlertDialog.setTitle(getString(R.string.app_name))
                    logoutAlertDialog.setMessage(getString(R.string.want_to_defaultaddress))
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
//                        if (isNetworkAvailable())
//                            callAddAddressApi(place.name.toString(), place.address.toString(), place.latLng.latitude, place.latLng.longitude, "", ",", "", "", "", "")

                    })
                    logoutAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), DialogInterface.OnClickListener { dialog, id ->
                        logoutAlertDialog.dismiss()
                    })
                    logoutAlertDialog.show()


                }
                PlaceAutocomplete.RESULT_ERROR -> {

                    val status = PlaceAutocomplete.getStatus(this, data)
                    Log.i("onActivityResult", status.statusMessage)
                    Utils.getInstance().showToast(this,status.statusMessage!!)
                }
                RESULT_CANCELED -> // The user canceled the operation.
                    Log.e("onActivityResult", "canceled")
            }
        }
    }
    override fun onClick(view: View) {
        when (view.id) {
            R.id.saveDeliveryDetailBt -> {
                if (validate()) {
//                    val addressDetail = ApartmentNumEditText.text.toString().trim() + ", " + FloorEditText.text.toString().trim() + ", " + BuildingEditText.text.toString().trim() + ", " + StreetEditText.text.toString().trim() + " " + pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS)
                    KeyboardUtils.hideSoftInput(this)
//                    val intent = Intent(this, CheckOutActivity::class.java)
                    if (rl_apartment.isSelected) {
                        val addressDetail = ApartmentNumEditText.text.toString().trim() + ", " + FloorEditText.text.toString().trim() + ", " + BuildingEditText.text.toString().trim() + ", " + StreetEditText.text.toString().trim() + ", Way No.-" + WayNumEditText.text.toString().trim() + ", " + pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS)
//                        intent.putExtra(AppIntentExtraKeys.ADDRESS_TYPE, APARTMENT)
//                        intent.putExtra(AppIntentExtraKeys.ADDRESS, addressDetail)
                        callAddAddressApi(addressDetail, APARTMENT, PhoneNumberEditText.text.toString().trim(), additionDirectionEditText.text.toString().trim(), NameEditText.text.toString().trim())
                    } else if (rl_house.isSelected) {
                        val addressDetail = ApartmentNumEditText.text.toString().trim() + ", " /*+ FloorEditText.text.toString().trim() + ", " + BuildingEditText.text.toString().trim() + ", "*/ + StreetEditText.text.toString().trim() + ", Way No.-" + WayNumEditText.text.toString().trim() + ", " + pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS)
//                        intent.putExtra(AppIntentExtraKeys.ADDRESS_TYPE, VILLA)
//                        intent.putExtra(AppIntentExtraKeys.ADDRESS, addressDetail)
                        callAddAddressApi(addressDetail, VILLA, PhoneNumberEditText.text.toString().trim(), additionDirectionEditText.text.toString().trim(), NameEditText.text.toString().trim())

                    } else {
                        val addressDetail = ApartmentNumEditText.text.toString().trim() + ", " + FloorEditText.text.toString().trim() + ", " + BuildingEditText.text.toString().trim() + ", " + StreetEditText.text.toString().trim() + ", Way No.-" + WayNumEditText.text.toString().trim() + ", " + pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS)
//                        intent.putExtra(AppIntentExtraKeys.ADDRESS_TYPE, OFFICE)
//                        intent.putExtra(AppIntentExtraKeys.ADDRESS, addressDetail)
                        callAddAddressApi(addressDetail, OFFICE, PhoneNumberEditText.text.toString().trim(), additionDirectionEditText.text.toString().trim(), NameEditText.text.toString().trim())

                    }
//                    intent.putExtra(AppIntentExtraKeys.ADDRESS, addressDetail)
//                    intent.putExtra(AppIntentExtraKeys.NAME, NameEditText.text.toString().trim())
//                    intent.putExtra(AppIntentExtraKeys.MOBILE_NUMBER, PhoneNumberEditText.text.toString().trim())
//                    intent.putExtra(AppIntentExtraKeys.LANDMARK, additionDirectionEditText.text.toString().trim())
//                    startActivity(intent)

                }
            }
            R.id.fulladdressEditText->{
                                try {
                    val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this)
                    startActivityForResult(intent, AppRequestCodes.PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: GooglePlayServicesRepairableException) {
                    Utils.getInstance().showToast(this, e.message!!)
                } catch (e: GooglePlayServicesNotAvailableException) {
                    Utils.getInstance().showToast(this, e.message!!)
                }
            }
            R.id.rl_apartment -> {
                setSelector(APARTMENT)
                ApartmentNumEditText.requestFocus()
            }

            R.id.rl_house -> {
                setSelector(VILLA)
                ApartmentNumEditText.requestFocus()
            }


            R.id.rl_office -> {
                setSelector(OFFICE)
                ApartmentNumEditText.requestFocus()
            }
        }
    }

    /*user_id,language,cart_id,delivery_address,delivery_latitude,delivery_longitude,delivery_landmark,delivery_address_type,deliver_to,deliver_mobile,address_id
 */
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private fun callAddAddressApi(address: String, delivery_address_type: String, mobile_number: String, landmark: String, name: String) {
        progressBar.visibility = View.VISIBLE
        saveDeliveryDetailBt.isEnabled = false
        val addDeliveryAddressRequest = HashMap<String, String>()
        addDeliveryAddressRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
//        if (from==3){
//            addDeliveryAddressRequest["delivery_latitude"] = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LATITUDE)
//            addDeliveryAddressRequest["delivery_longitude"] = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LONGITUDE)
//            addDeliveryAddressRequest["address"] = address
//            addDeliveryAddressRequest["name"] = name
//            addDeliveryAddressRequest["is_default"] = "1"
//            addDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
//            addDeliveryAddressRequest["zip_code"] =
//        }else{
//            addDeliveryAddressRequest["address_id"] = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_ID)
//            addDeliveryAddressRequest["delivery_latitude"] = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LATITUDE)
//            addDeliveryAddressRequest["delivery_longitude"] = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LONGITUDE)
//        }

        addDeliveryAddressRequest["address_id"] = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_ID)
        addDeliveryAddressRequest["delivery_latitude"] = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LATITUDE)
        addDeliveryAddressRequest["delivery_longitude"] = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LONGITUDE)
        addDeliveryAddressRequest["delivery_address"] = address
        addDeliveryAddressRequest["deliver_to"] = name
        addDeliveryAddressRequest["delivery_address_type"] = delivery_address_type

        addDeliveryAddressRequest["delivery_landmark"] = landmark
        addDeliveryAddressRequest["cart_id"] = pref!!.getPreferenceStringData(pref!!.KEY_CART_ID)
        addDeliveryAddressRequest["language"] = DrewelApplication.getInstance().getLanguage()
        addDeliveryAddressRequest["deliver_mobile"] = mobile_number
        val signUpObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).order_address_save(addDeliveryAddressRequest)
        compositeDisposable.add(signUpObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    progressBar.visibility = View.GONE
                    saveDeliveryDetailBt.isEnabled = true
                    DrewelApplication.getInstance().logoutWhenAccountDeactivated(result.response!!.isDeactivate!!, this)
                    if (result.response!!.status!!) {
                        saveDefaultAddressToPref(address, name, mobile_number, address, landmark, delivery_address_type)
                    } else {
                        Utils.getInstance().showToast(this, result.response!!.message!!)
                    }
                }, { error ->
                    progressBar.visibility = View.GONE
                    saveDeliveryDetailBt.isEnabled = true
                    Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    private fun saveDefaultAddressToPref(address: String, username: String, phoneNumber: String, fullAddress: String, landmark: String, delivery_address_type: String) {
//        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_ID, id)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS, address)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_USERNAME, username)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_PHONE_NUMBER, phoneNumber)
        pref!!.setPreferenceStringData(pref!!.KEY_FULL_DELIVERY_ADDRESS, fullAddress)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_lANDMARK, landmark)
        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_TYPE, delivery_address_type)
        if (from == 2)
            finish()
        else {
            startActivity(Intent(this, CheckOutActivity::class.java))
        }
//        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_NAME, name)
//        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LATITUDE, latitude)
//        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LONGITUDE, longitude)
//        pref!!.setPreferenceStringData(pref!!.KEY_ZIP_CODE, zip_code)
    }

    val APARTMENT = "1"
    val VILLA = "2"
    val OFFICE = "3"
    fun setSelector(type: String) {
        rl_apartment.isSelected = false
        txt_apartment.isSelected = false
        img_apartment.isSelected = false
        ApartmentNoTextLayout.hint = getString(R.string.apartment_no)


        rl_house.isSelected = false
        txt_house.isSelected = false
        img_house.isSelected = false
        ApartmentNoTextLayout.visibility = View.GONE
        FloorTextLayout.visibility = View.GONE
        BuildingTextLayout.visibility = View.GONE

        rl_office.isSelected = false
        txt_office.isSelected = false
        img_office.isSelected = false
        ApartmentNoTextLayout.isErrorEnabled = false
        FloorTextLayout.isErrorEnabled = false
        BuildingTextLayout.isErrorEnabled = false

        ApartmentNumEditText.setText("")
        FloorEditText.setText("")
        BuildingEditText.setText("")
        StreetEditText.setText("")
        additionDirectionEditText.setText("")
        ImageViewCompat.setImageTintList(img_apartment, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey_color_txt)))
        ImageViewCompat.setImageTintList(img_house, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey_color_txt)))
        ImageViewCompat.setImageTintList(img_office, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey_color_txt)))

//        DrawableCompat.setTint(img_apartment.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.grey_color_txt))
//        DrawableCompat.setTint(img_house.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.grey_color_txt))
//        DrawableCompat.setTint(img_office.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.grey_color_txt))

//        img_apartment.setColorFilter(ContextCompat.getColor(this, R.color.grey_color_txt), android.graphics.PorterDuff.Mode.MULTIPLY);
//        img_house.setColorFilter(ContextCompat.getColor(this, R.color.grey_color_txt), android.graphics.PorterDuff.Mode.MULTIPLY);
//        img_office.setColorFilter(ContextCompat.getColor(this, R.color.grey_color_txt), android.graphics.PorterDuff.Mode.MULTIPLY);
        when (type) {
            APARTMENT -> {
                rl_apartment.isSelected = true
                txt_apartment.isSelected = true
                img_apartment.isSelected = true
                ApartmentNoTextLayout.hint = getString(R.string.apartment_no)
//                img_apartment.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
//                DrawableCompat.setTint(img_apartment.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                ImageViewCompat.setImageTintList(img_apartment, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)))
                ApartmentNoTextLayout.visibility = View.VISIBLE
                FloorTextLayout.visibility = View.VISIBLE
                BuildingTextLayout.visibility = View.VISIBLE
            }
            VILLA -> {
                rl_house.isSelected = true
                txt_house.isSelected = true
                img_house.isSelected = true
                ApartmentNoTextLayout.hint = getString(R.string.house_no)
//                img_house.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
//                DrawableCompat.setTint(img_house.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                ImageViewCompat.setImageTintList(img_house, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)))

                ApartmentNoTextLayout.visibility = View.VISIBLE
                FloorTextLayout.visibility = View.GONE
                BuildingTextLayout.visibility = View.GONE
            }
            OFFICE -> {
                rl_office.isSelected = true
                txt_office.isSelected = true
                img_office.isSelected = true
                ApartmentNoTextLayout.hint = getString(R.string.office_no)
//                img_office.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
//                DrawableCompat.setTint(img_office.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                ImageViewCompat.setImageTintList(img_office, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)))

                ApartmentNoTextLayout.visibility = View.VISIBLE
                FloorTextLayout.visibility = View.VISIBLE
                BuildingTextLayout.visibility = View.VISIBLE
            }

        }
    }

    private fun validate(): Boolean {

        if (NameEditText.text.toString().isBlank()) {
            nameTextLayout.error = getString(R.string.enter_name)
            nameTextLayout.requestFocus()
            return false
        }
        nameTextLayout.isErrorEnabled = false

        if (PhoneNumberEditText.text.toString().isBlank()) {
            PhoneNumberTextLayout.error = getString(R.string.enter_phone_number)
            PhoneNumberTextLayout.requestFocus()
            return false
        }
        PhoneNumberTextLayout.isErrorEnabled = false

        if (ApartmentNumEditText.text.toString().isBlank()) {
            if (rl_house.isSelected)
                ApartmentNoTextLayout.error = getString(R.string.enter_house_number)
            else if (rl_office.isSelected)
                ApartmentNoTextLayout.error = getString(R.string.enter_office_number)
            else
                ApartmentNoTextLayout.error = getString(R.string.enter_apartment_number)

            ApartmentNoTextLayout.requestFocus()
            return false
        }
        ApartmentNoTextLayout.isErrorEnabled = false

        if (!rl_house.isSelected) {
            if (FloorEditText.text.toString().isBlank()) {
                FloorTextLayout.error = getString(R.string.enter_floor)
                FloorTextLayout.requestFocus()
                return false
            }
            FloorTextLayout.isErrorEnabled = false

            if (BuildingEditText.text.toString().isBlank()) {
                BuildingTextLayout.error = getString(R.string.enter_building)
                BuildingTextLayout.requestFocus()
                return false
            }
            BuildingTextLayout.isErrorEnabled = false
        }
        if (WayNumEditText.text.toString().isEmpty()) {
            WayNumEditText.error = getString(R.string.enter_way)
            WayNumEditText.requestFocus()
            return false
        }
        WayNoTextLayout.isErrorEnabled = false

        /* if(StreetEditText.text.toString().isEmpty()) {
             StreetTextLayout.error = getString(R.string.enter_name)
             StreetTextLayout.requestFocus()
             return false
         }
         StreetTextLayout.isErrorEnabled = false

         if(additionDirectionEditText.text.toString().isEmpty()) {
             additionDirectionTextLayout.error = getString(R.string.enter_name)
             additionDirectionTextLayout.requestFocus()
             return false
         }
         additionDirectionTextLayout.isErrorEnabled = false*/

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                KeyboardUtils.hideSoftInput(this)
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}