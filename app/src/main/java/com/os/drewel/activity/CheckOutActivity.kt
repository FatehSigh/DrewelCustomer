package com.os.drewel.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import com.blankj.utilcode.util.KeyboardUtils
import com.os.drewel.R
import com.os.drewel.adapter.AppliedCouponCodeAdapter
import com.os.drewel.apicall.DrewelApi
import com.os.drewel.apicall.requestmodel.CheckOutRequest
import com.os.drewel.apicall.responsemodel.applycouponresponsemodel.Coupon
import com.os.drewel.apicall.responsemodel.deliverychargesresponsemodel.Data
import com.os.drewel.application.DrewelApplication
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.AppRequestCodes
import com.os.drewel.constant.Constants
import com.os.drewel.delegate.CouponCodeRemove
import com.os.drewel.dialog.TimeSlotBottomSheetDialog
import com.os.drewel.prefrences.Prefs.Companion.prefs
import com.os.drewel.rxbus.CartRxJavaBus
import com.os.drewel.utill.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.android.synthetic.main.content_checkout.*
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by monikab on 4/4/2018.
 */
class CheckOutActivity : BaseActivity(), View.OnClickListener, CouponCodeRemove {
    private var deliveryTypeAlertDialog: AlertDialog? = null
    private var timeSlotList: MutableList<String> = ArrayList()
    private var deliveryChargesResponse: Data? = null
    private var selectedTimeSlot: String = ""
    private var deliverySlotSelect: Disposable? = null
    private var appliedCouponCodes: MutableSet<String> = HashSet()
    private var appliedCouponCodesAllInfo: MutableList<Coupon> = ArrayList()
    private var previousLoyaltyPointDiscount: Double = 0.0

    private var checkoutRequest: CheckOutRequest = CheckOutRequest()

    private var deliveryTimeSlotStartTime = ""
    private var deliveryTimeSlotEndTime = ""
    private var deliveryDate = ""

    private var appliedCouponCodeAdapter: AppliedCouponCodeAdapter? = null

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var isApplied = false
    var delivery_address_type = "1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        initView()
        getDataFromIntent()
        callDeliveryChargesApi()
    }

    private fun getDataFromIntent() {
        if (pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_USERNAME!!)!!.isNotEmpty()) {
            nameTv.text = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_USERNAME
                    ?: "") ?: ""
            phoneNoTv.text = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_PHONE_NUMBER
                    ?: "") ?: ""
            deliveryAddressTv.text = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS
                    ?: "") ?: ""
            checkoutRequest.deliveryLandmark = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_lANDMARK
                    ?: "") ?: ""
            delivery_address_type = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_TYPE
                    ?: "") ?: ""
            Log.e("delivery_address_type", delivery_address_type)
            checkoutRequest.addressId = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_ID
                    ?: "") ?: ""
        }
        quantityTv.text = orderItemQuantity
        subTotalTv.text = String.format("%.3f", orderNetPrice.toDouble()) + " " + getString(R.string.omr)
        subTotalTvValue = orderNetPrice.toFloat()
        grandTotalTv.text = String.format("%.3f", orderNetPrice.toDouble()) + " " + getString(R.string.omr)
        checkoutRequest.delivery_address_type = delivery_address_type
        checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_COD
        checkoutRequest.quantity = orderItemQuantity
        checkoutRequest.amount = orderNetPrice.toString()
        checkoutRequest.deliverTo = nameTv.text.toString()
        checkoutRequest.deliverMobile = phoneNoTv.text.toString()

        if (delivery_address_type.isNotEmpty()) {
            if (delivery_address_type.equals("1")) {
                deliveryAddressTv_dropdown.setText(getString(R.string.apartment))
            } else if (delivery_address_type.equals("2")) {
                deliveryAddressTv_dropdown.setText(getString(R.string.house))
            } else if (delivery_address_type.equals("3")) {
                deliveryAddressTv_dropdown.setText(getString(R.string.office))
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_USERNAME!!)!!.isNotEmpty()) {
            nameTv.text = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_USERNAME
                    ?: "") ?: ""
            phoneNoTv.text = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_PHONE_NUMBER
                    ?: "") ?: ""
            deliveryAddressTv.text = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS
                    ?: "") ?: ""
            checkoutRequest.deliveryLandmark = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_lANDMARK
                    ?: "") ?: ""
            delivery_address_type = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_TYPE
                    ?: "") ?: ""
            Log.e("delivery_address_type", delivery_address_type)
            checkoutRequest.addressId = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_ID
                    ?: "") ?: ""
        }
        checkoutRequest.deliverTo = nameTv.text.toString()
        checkoutRequest.deliverMobile = phoneNoTv.text.toString()
        if (delivery_address_type.isNotEmpty()) {
            if (delivery_address_type.equals("1")) {
                deliveryAddressTv_dropdown.setText(getString(R.string.apartment))
            } else if (delivery_address_type.equals("2")) {
                deliveryAddressTv_dropdown.setText(getString(R.string.house))
            } else if (delivery_address_type.equals("3")) {
                deliveryAddressTv_dropdown.setText(getString(R.string.office))
            }
        }
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        deliveryAddressTv_change.setOnClickListener(this)
        chooseDeliveryTypeTv.setOnClickListener(this)
        applyCoupanCodeTv.setOnClickListener(this)
        applyLoyaltyPointTv.setOnClickListener(this)
        confirmOrderBt.setOnClickListener(this)
//        CouponCodeEditText.setOnClickListener(this)
        paymentMethodRadioGroup.setOnCheckedChangeListener({ group: RadioGroup, checkedId: Int ->
            when (checkedId) {
                R.id.cashRadioBt -> {
                    checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_COD
                }
                R.id.cardRadioBt -> {
                    checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_COD
//                    checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_CARD
                }
                R.id.walletRadioBt -> {
                    checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_COD
//                    checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_WALLET
                }
            }
        })
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

    override fun onClick(view: View) {
        when (view.id) {

            R.id.chooseDeliveryTypeTv -> {
                startActivityForResult(Intent(this, ChooseDeliveryTypeActivity::class.java).putExtra("Data", deliveryChargesResponse), 1245)
//                showDeliveryTypeDialog()
            }
            R.id.deliveryAddressTv_change -> {
                startActivity(Intent(this, DeliveryAddressActivity::class.java).putExtra("TYPE", 2))
//                showDeliveryTypeDialog()
            }
            R.id.applyCoupanCodeTv -> {
//                KeyboardUtils.hideSoftInput(this)
//                val couponCode = CouponCodeEditText.text.toString().trim()
//                if (appliedCouponCodes.contains(couponCode)) {
//                    Toast.makeText(this, getString(R.string.coupon_already_applied), Toast.LENGTH_LONG).show()
//                } else if (couponCode.isNotBlank()) {
//                    if (isNetworkAvailable())
//                        callApplyCouponCodeApi(couponCode)
//                }
                val intent = Intent(this, CouponCodeActivity::class.java)
                startActivityForResult(intent, AppRequestCodes.APPLY_COUPON_CODE)
            }

            R.id.applyLoyaltyPointTv -> {
                KeyboardUtils.hideSoftInput(this)
                if (applyLoyaltyPointTv.text.equals(getString(R.string.remove))) {
                    applyLoyaltyPointTv.text = getString(R.string.apply)
                    LoyaltyPointEditText.text = getString(R.string.add_loyalty_point)
//                    discountTv.setText("0" + " " + getString(R.string.omr))
//                    discountTvValue = 0f
                    loyaltyPoints = ""
                    setLoyaltyPointDiscount(0.0)
                    setGrandTotal()
                } else {
                    callApplyLoyaltyPointApi("")
                }


//                val loyaltyPoint = LoyaltyPointEditText.text.toString().trim()
//                if (loyaltyPoint.isNotBlank()) {
//                    if (applyLoyaltyPointTv.text == getString(R.string.apply)) {
//                        if (isNetworkAvailable())
//                            callApplyLoyaltyPointApi(loyaltyPoint)
//                    } else {
//                        //setCouponCodeEdibility(true)
//                        LoyaltyPointEditText.setText("")
//                        LoyaltyPointEditText.isEnabled = true
//                        applyLoyaltyPointTv.text = getString(R.string.apply)
//                        setLoyaltyPointDiscount(0.0)
//                    }
//                }

            }
            R.id.CouponCodeEditText -> {
//                val intent = Intent(this, CouponCodeActivity::class.java)
//                startActivityForResult(intent, AppRequestCodes.APPLY_COUPON_CODE)
            }

            R.id.confirmOrderBt -> {
                KeyboardUtils.hideSoftInput(this)

                if (isValidateCheckoutRequest()) {

                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
                    val sdfdate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    var date: Date? = null
                    try {
//                        date = sdf.parse(dateOfBirth)
                        checkoutRequest.deliveryEndTime = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(sdf.parse(deliveryTimeSlotEndTime))
                        checkoutRequest.deliveryStartTime = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(sdf.parse(deliveryTimeSlotStartTime))
                        checkoutRequest.deliveryDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(sdfdate.parse(deliveryDate))
                    } catch (e: ParseException) {
                        // handle exception here !
                    }


//                    val myString = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
//                    checkoutRequest.deliveryEndTime = deliveryTimeSlotEndTime
//                    checkoutRequest.deliveryStartTime = deliveryTimeSlotStartTime
                    checkoutRequest.userId = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
                    checkoutRequest.language = DrewelApplication.getInstance().getLanguage()
                    checkoutRequest.cartId = pref!!.getPreferenceStringData(pref!!.KEY_CART_ID)
                    checkoutRequest.deliveryLatitude = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LATITUDE)
                    checkoutRequest.deliveryLongitude = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LONGITUDE)
                    deliveryDate
                    checkoutRequest.deliveryAddress = deliveryAddressTv.text.toString()
//                    checkoutRequest.deliveryDate = deliveryDate
                    checkoutRequest.loyaltyPoints = loyaltyPoints
                    checkoutRequest.deliveryCharges = deliveryChargesValue.toString()
                    checkoutRequest.transactionId = ""
                    val list: MutableList<String> = ArrayList()
                    list.addAll(appliedCouponCodes)
                    checkoutRequest.coupons = list

                    if (isNetworkAvailable())
                        checkoutApi()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppRequestCodes.APPLY_COUPON_CODE && resultCode == Activity.RESULT_OK) {

            val couponCode = data?.getStringExtra(AppIntentExtraKeys.COUPON_CODE) ?: ""
            if (appliedCouponCodes.contains(couponCode)) {
                Utils.getInstance().showToast(this,getString(R.string.coupon_already_applied))
            } else if (couponCode.isNotBlank()) {
                if (isNetworkAvailable())
                    callApplyCouponCodeApi(couponCode)
            }
        } else if (requestCode == 1245 && resultCode == Activity.RESULT_OK) {
            deliveryDate = data!!.getStringExtra("deliveryDate")
            checkoutRequest.deliveryType = data!!.getStringExtra("deliveryType")
            deliveryTimeSlotStartTime = data!!.getStringExtra("deliveryTimeSlotStartTime")
            deliveryTimeSlotEndTime = data!!.getStringExtra("deliveryTimeSlotEndTime")
            var date = SimpleDateFormat("dd MMM yy").format(SimpleDateFormat("yyyy-MM-dd").parse(deliveryDate))
            Log.e("cor deliveryType", checkoutRequest.deliveryType)
            if (checkoutRequest.deliveryType.equals(Constants.DELIVERY_NOW)) {
                if (deliveryChargesResponse?.deliveryCharge!!.isNotEmpty())
                    deliveryChargesValue = DrewelApplication.getInstance().convertToEnglish(deliveryChargesResponse?.expediteDeliveryCharges!!.toFloat()).toFloat()
                deliveryChargesTv.text = String.format("%.3f", deliveryChargesValue) +" "+ getString(R.string.omr)
                chooseDeliveryTypeTv.text = date.toString() + ", " + deliveryTimeSlotStartTime + "- " + deliveryTimeSlotEndTime
                setGrandTotal()
            } else if (checkoutRequest.deliveryType.equals(Constants.SAME_DAY_DELIVERY)) {
                if (deliveryChargesResponse?.deliveryCharge!!.isNotEmpty())
                    deliveryChargesValue = DrewelApplication.getInstance().convertToEnglish(deliveryChargesResponse?.sameDayDeliveryCharge!!.toFloat()).toFloat()
                deliveryChargesTv.text = String.format("%.3f", deliveryChargesValue) +" "+  getString(R.string.omr)
                chooseDeliveryTypeTv.text = date.toString() + ", " + deliveryTimeSlotStartTime + "- " + deliveryTimeSlotEndTime
                setGrandTotal()
            } else {
                if (deliveryChargesResponse?.deliveryCharge!!.isNotEmpty())
                    deliveryChargesValue = DrewelApplication.getInstance().convertToEnglish(deliveryChargesResponse?.deliveryCharge!!.toFloat()).toFloat()
                deliveryChargesTv.text = String.format("%.3f", deliveryChargesValue) +" "+  getString(R.string.omr)
                chooseDeliveryTypeTv.text = date.toString() + ", " + deliveryTimeSlotStartTime + "- " + deliveryTimeSlotEndTime
                setGrandTotal()
            }
        }
    }


    private fun isValidateCheckoutRequest(): Boolean {
//        if (checkoutRequest.deliveryType == null) {
//            Toast.makeText(this, getString(R.string.select_delivery_type), Toast.LENGTH_LONG).show()
//            return false
//        }
        if (deliveryTimeSlotStartTime.isEmpty() || deliveryTimeSlotEndTime.isEmpty()) {
            Utils.getInstance().showToast(this,getString(R.string.select_delivery_time_slot))
            return false
        }
        return true
    }


    var discountTvValue = 0f
    var subTotalTvValue = 0f
    var deliveryChargesValue = 0f
    private fun setGrandTotal() {
        var discount = 0f
        var subTotal = 0f
        var deliverCharge = 0f
        discount = discountTvValue
        subTotal = subTotalTvValue
        deliverCharge = deliveryChargesValue

//        discount = discountTv.text.toString().split(" ")[0].toFloat()
//        subTotal = subTotalTv.text.split(" ")[0].toFloat()
//        deliverCharge = deliveryChargesTv.text.toString().split(" ")[0].toFloat()

        val discountedAmount = (subTotal - discount)
        val grandTotal = if (discountedAmount > 0)
            discountedAmount + deliverCharge
        else
            deliverCharge
        if (deliveryChargesResponse!!.is_edited == "1") {
            if (deliveryChargesResponse!!.payment_mode!!.isNotEmpty() && !deliveryChargesResponse!!.payment_mode!!.equals(Constants.PAYMENT_TYPE_COD)) {
                ll_lastpaid.visibility = View.VISIBLE
                last_paidTv.setText(String.format("%.3f", deliveryChargesResponse!!.last_paid!!.toDouble()) + " " + getString(R.string.omr))
                if (deliveryChargesResponse!!.last_paid!!.toDouble() > grandTotal.toDouble()) {
                    var diffAmount = deliveryChargesResponse!!.last_paid!!.toDouble() - grandTotal.toDouble()
                    if (DrewelApplication.getInstance().convertToEnglish(diffAmount.toFloat()).toFloat() > 0) {
                        last_instruction.visibility = View.VISIBLE
                        last_instruction.setText("(" + String.format("%.3f", diffAmount) + " " + getString(R.string.amount_will_be_transfered) + ")")
                    } else
                        last_instruction.visibility = View.GONE
                    grandTotalTv.text = "0.000" + getString(R.string.omr)
//                  last_instruction.visibility = View.VISIBLE
                    cardRadioBt.isChecked = true
                    cashRadioBt.isEnabled = false
                    walletRadioBt.isEnabled = true
                } else {
                    var diffAmount = grandTotal.toDouble() - deliveryChargesResponse!!.last_paid!!.toDouble()
                    Log.e("diff Amount", diffAmount.toString())
//                                    last_instruction.setText("(" + diffAmount.toString() + " " + getString(R.string.amount_will_be_transfered) + ")")
                    grandTotalTv.text = String.format("%.3f", diffAmount) + " " + getString(R.string.omr)
                    last_instruction.visibility = View.GONE
                    cardRadioBt.isChecked = true
                }
            } else {
                last_instruction.visibility = View.GONE
                if (grandTotal > 0) {
                    grandTotalTv.text = String.format("%.3f", grandTotal) + " " + getString(R.string.omr)
                } else
                    grandTotalTv.text ="0.000" + getString(R.string.omr)
            }
        } else {
            last_instruction.visibility = View.GONE
            if (grandTotal > 0) {
                grandTotalTv.text = String.format("%.3f", grandTotal) + " " + getString(R.string.omr)
            } else
                grandTotalTv.text = "0.000" + getString(R.string.omr)
        }
    }

    private fun setDiscount(newDiscount: Double) {
//        discountTvValue=newDiscount.toFloat()
        var previousDiscount = 0f
//        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ARABIC)) {
//            previousDiscount = discountTv.text.toString().split(" ")[1].toFloat()
//        } else
//        previousDiscount = discountTv.text.toString().split(" ")[0].toFloat()
        previousDiscount = discountTvValue
        val totalDiscount = (newDiscount + previousDiscount)
        discountTvValue = totalDiscount.toFloat()
        discountTv.text = String.format("%.3f", DrewelApplication.getInstance().convertToEnglish(totalDiscount.toFloat()).toDouble()) + " " + getString(R.string.omr)

    }


    private fun removeDiscount(newDiscount: Double) {
//        var previousDiscount = 0f
//
//        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ARABIC)) {
//            previousDiscount = discountTv.text.toString().split(" ")[0].toFloat()
//        } else
//            previousDiscount = discountTv.text.toString().split(" ")[0].toFloat()
//        val previousDiscount = discountTv.text.toString().split(" ")[0].toFloat()
        val previousDiscount = discountTvValue
        val totalDiscount = (previousDiscount - newDiscount)
        discountTvValue = totalDiscount.toFloat()
        discountTv.text = String.format("%.3f", DrewelApplication.getInstance().convertToEnglish(totalDiscount.toFloat()).toDouble()) + " " + getString(R.string.omr)
    }

    /*remove previous loyalty point discount from total discount*/
    private fun setLoyaltyPointDiscount(loyaltyPointsDiscount: Double) {
//        var previousDiscount = 0f
//
//        if (DrewelApplication.getInstance().getLanguage().equals(Constants.LANGUAGE_ARABIC)) {
//            previousDiscount = discountTv.text.toString().split(" ")[1].toFloat()
//        } else
//            previousDiscount = discountTv.text.toString().split(" ")[0].toFloat()
//        val previousDiscount = discountTv.text.toString().split(" ")[0].toFloat()
        val previousDiscount = discountTvValue
        var totalDiscount = (loyaltyPointsDiscount + previousDiscount) - previousLoyaltyPointDiscount

        if (totalDiscount < 0)
            totalDiscount = 0.0

        previousLoyaltyPointDiscount = loyaltyPointsDiscount

        discountTvValue = totalDiscount.toFloat()
        discountTv.text = String.format("%.3f", DrewelApplication.getInstance().convertToEnglish(totalDiscount.toFloat()).toDouble()) + " " + getString(R.string.omr)
    }

    /* when user click on remove coupon button adapter then deduct discount from previously applied discount*/
    override fun onCouponCodeRemove(position: Int) {
        removeDiscount(appliedCouponCodesAllInfo[position].discountAmount!!.toDouble())
        appliedCouponCodes.remove(appliedCouponCodesAllInfo[position].couponCode)
        appliedCouponCodesAllInfo.removeAt(position)
        appliedCouponCodeAdapter?.notifyItemRemoved(position)
        appliedCouponCodeAdapter?.notifyItemRangeRemoved(0, appliedCouponCodesAllInfo.size)
        setGrandTotal()
        //setLoyaltyPointEdibility(true)
        // CouponCodeEditText.setText("")
    }


    private fun callApplyCouponCodeApi(couponCode: String) {

        applyCouponCodeTv.isEnabled = false
        setProgressState(View.VISIBLE, View.VISIBLE)

        val applyCouponCodeRequest = HashMap<String, String>()
        applyCouponCodeRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        applyCouponCodeRequest["language"] = DrewelApplication.getInstance().getLanguage()
        applyCouponCodeRequest["cart_id"] = pref!!.getPreferenceStringData(pref!!.KEY_CART_ID)
        applyCouponCodeRequest["coupon_code"] = couponCode

        val applyCouponCodeObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).applyCoupon(applyCouponCodeRequest)

        compositeDisposable.add(applyCouponCodeObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    setProgressState(View.GONE, View.VISIBLE)
                    applyCouponCodeTv.isEnabled = true
                    if (result.response!!.status!!) {
                        /* user can apply multiple coupon code*/
                        CouponCodeEditText.setText("")
                        result.response!!.data!!.coupon!!.couponCode = couponCode
                        appliedCouponCodesAllInfo.add(result.response!!.data!!.coupon!!)
                        appliedCouponCodes.add(couponCode)
                        setDiscount(result.response!!.data!!.coupon!!.discountAmount.toString().toDouble())
                        setGrandTotal()
                        setAppliedCouponCodesAdapter()
                        // setLoyaltyPointEdibility(false)
                    } else {
                        CouponCodeEditText.setText("")
                        Utils.getInstance().showToast(this,result.response!!.message!!)
                    }

                }, { error ->
                    setProgressState(View.GONE, View.VISIBLE)
                    applyCouponCodeTv.isEnabled = true
                    Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    private fun setAppliedCouponCodesAdapter() {
        if (appliedCouponCodeAdapter == null) {
            couponCodeRv.layoutManager = LinearLayoutManager(this)
            appliedCouponCodeAdapter = AppliedCouponCodeAdapter(this, appliedCouponCodesAllInfo, this)
            couponCodeRv.adapter = appliedCouponCodeAdapter
        } else
            appliedCouponCodeAdapter?.notifyDataSetChanged()
    }


    private fun callDeliveryChargesApi() {

        setProgressState(View.VISIBLE, View.GONE)

        val getDeliveryChargesRequest = HashMap<String, String>()
        getDeliveryChargesRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        getDeliveryChargesRequest["language"] = DrewelApplication.getInstance().getLanguage()
        getDeliveryChargesRequest["cart_id"] = pref!!.getPreferenceStringData(pref!!.KEY_CART_ID)

        val getDeliveryChargesObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).getDeliveryCharges(getDeliveryChargesRequest)

        compositeDisposable.add(getDeliveryChargesObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setProgressState(View.GONE, View.VISIBLE)
                    applyCouponCodeTv.isEnabled = true
                    if (result.response!!.status!!) {
                        deliveryChargesResponse = result.response!!.data
                        if (result.response!!.data!!.is_edited == "1") {
                            if (deliveryChargesResponse!!.payment_mode!!.isNotEmpty() && !deliveryChargesResponse!!.payment_mode!!.equals(Constants.PAYMENT_TYPE_COD)) {
                                ll_lastpaid.visibility = View.VISIBLE
                                last_paidTv.setText(String.format("%.3f", result.response!!.data!!.last_paid!!.toDouble()) + " " + getString(R.string.omr))
                                if (result.response!!.data!!.last_paid!!.toDouble() > orderNetPrice.toDouble()) {
                                    var diffAmount = result.response!!.data!!.last_paid!!.toDouble() - orderNetPrice.toDouble()
                                    if (diffAmount != 0.0) {
                                        last_instruction.setText("(" + String.format("%.3f", diffAmount) + " " + getString(R.string.amount_will_be_transfered) + ")")
                                        last_instruction.visibility = View.VISIBLE
                                    } else {
                                        last_instruction.visibility = View.GONE
                                    }
                                    grandTotalTv.text = "0.000"+ getString(R.string.omr)

                                    cardRadioBt.isChecked = true
                                    cashRadioBt.isEnabled = false
                                    walletRadioBt.isEnabled = true
                                } else {
                                    var diffAmount = orderNetPrice.toDouble() - result.response!!.data!!.last_paid!!.toDouble()
//                                    last_instruction.setText("(" + diffAmount.toString() + " " + getString(R.string.amount_will_be_transfered) + ")")
                                    grandTotalTv.text = String.format("%.3f", diffAmount) + " " + getString(R.string.omr)
                                    last_instruction.visibility = View.GONE
                                    cardRadioBt.isChecked = true
                                    cashRadioBt.isEnabled = false
                                    walletRadioBt.isEnabled = true
                                }
                            }
                        } else {
                            last_instruction.visibility = View.GONE
                            ll_lastpaid.visibility = View.GONE
                        }
                    } else
                        Utils.getInstance().showToast(this,result.response!!.message!!)

                }, { error ->
                    setProgressState(View.GONE, View.VISIBLE)
                    applyCouponCodeTv.isEnabled = true
                    Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    var loyaltyPoints: String = ""
    private fun callApplyLoyaltyPointApi(loyaltyCode: String) {

        applyLoyaltyPointTv.isEnabled = false
        setProgressState(View.VISIBLE, View.VISIBLE)

        val applyLoyaltyPointRequest = HashMap<String, String>()
        applyLoyaltyPointRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        applyLoyaltyPointRequest["language"] = DrewelApplication.getInstance().getLanguage()
        applyLoyaltyPointRequest["cart_id"] = pref!!.getPreferenceStringData(pref!!.KEY_CART_ID)
        applyLoyaltyPointRequest["loyalty_points"] = loyaltyCode

        val applyLoyaltyPointObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).applyLoyaltyPoint(applyLoyaltyPointRequest)

        compositeDisposable.add(applyLoyaltyPointObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    setProgressState(View.GONE, View.VISIBLE)
                    Utils.getInstance().showToast(this,result.response!!.message!!)

                    applyLoyaltyPointTv.isEnabled = true

                    if (result.response!!.status!!) {
                        setLoyaltyPointDiscount(result.response!!.data!!.loyalty_points.toString().toDouble())
                        loyaltyPoints = result.response!!.data!!.loyalty_points.toString()
                        setGrandTotal()
                        LoyaltyPointEditText.text = result.response!!.data!!.loyalty_points.toString() + " " + getString(R.string.add_loyalty_point)
                        applyLoyaltyPointTv.text = getString(R.string.remove)
                        // setCouponCodeEdibility(false)
                    } else {
                        LoyaltyPointEditText.setText("")
                    }

                }, { error ->
                    setProgressState(View.GONE, View.VISIBLE)
                    applyLoyaltyPointTv.isEnabled = true
                    Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    /* private fun setCouponCodeEdibility(enable: Boolean) {
         CouponCodeEditText.isEnabled=enable
         applyCouponCodeTv.isEnabled=enable

         isApplied = !enable

         appliedCouponCodesAllInfo.clear()
         appliedCouponCodes.clear()
         setAppliedCouponCodesAdapter()
     }

     private fun setLoyaltyPointEdibility(enable: Boolean) {
         LoyaltyPointEditText.isEnabled=enable
         applyLoyaltyPointTv.isEnabled=enable

         CouponCodeEditText.isEnabled=enable
         applyCouponCodeTv.isEnabled=enable

         isApplied = !enable
     }*/


    private fun checkoutApi() {
        setProgressState(View.VISIBLE, View.VISIBLE)
        confirmOrderBt.isEnabled = false
        val checkoutObservable = DrewelApplication.getInstance().getRequestQueue().create(DrewelApi::class.java).checkout(checkoutRequest)

        compositeDisposable.add(checkoutObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    confirmOrderBt.isEnabled = true
                    setProgressState(View.GONE, View.VISIBLE)
                    Utils.getInstance().showToast(this,result.response!!.message!!)
                    if (result.response!!.status!!) {

                        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_USERNAME, checkoutRequest.deliverTo
                                ?: "")
                        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_PHONE_NUMBER, checkoutRequest.deliverMobile
                                ?: "")
                        pref!!.setPreferenceStringData(pref!!.KEY_FULL_DELIVERY_ADDRESS, checkoutRequest.deliveryAddress
                                ?: "")
                        pref!!.setPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_lANDMARK, checkoutRequest.deliveryLandmark
                                ?: "")
                        pref!!.setPreferenceStringData(pref!!.KEY_CART_ID, "")
                        CartRxJavaBus.getInstance().cartPublishSubject.onNext("0")

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra("FROM", 1)
                        startActivity(intent)


                    }
                }, { error ->
                    confirmOrderBt.isEnabled = true
                    setProgressState(View.GONE, View.VISIBLE)
                    Utils.getInstance().showToast(this,error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }


    private fun setProgressState(progressVisibility: Int, viewVisibility: Int) {
        progressBar.visibility = progressVisibility
        orderSummaryLayout.visibility = viewVisibility
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}