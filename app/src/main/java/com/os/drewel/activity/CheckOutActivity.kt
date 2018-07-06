package com.os.drewel.activity

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
import java.text.DecimalFormat
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

        quantityTv.text = orderItemQuantity
        subTotalTv.text = orderNetPrice + " " + getString(R.string.omr)
        grandTotalTv.text = orderNetPrice + " " + getString(R.string.omr)
        checkoutRequest.delivery_address_type = delivery_address_type
        checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_COD
        checkoutRequest.quantity = orderItemQuantity
        checkoutRequest.amount = orderNetPrice
        checkoutRequest.deliverTo = nameTv.text.toString()
        checkoutRequest.deliverMobile = phoneNoTv.text.toString()
        checkoutRequest.addressId = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_ID
                ?: "") ?: ""
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

        checkoutRequest.deliverTo = nameTv.text.toString()
        checkoutRequest.deliverMobile = phoneNoTv.text.toString()
        checkoutRequest.addressId = pref?.getPreferenceStringData(pref?.KEY_DELIVERY_ADDRESS_ID
                ?: "") ?: ""
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
        applyCouponCodeTv.setOnClickListener(this)
        applyLoyaltyPointTv.setOnClickListener(this)
        confirmOrderBt.setOnClickListener(this)
        CouponCodeEditText.setOnClickListener(this)
        paymentMethodRadioGroup.setOnCheckedChangeListener({ group: RadioGroup, checkedId: Int ->
            when (checkedId) {
                R.id.cashRadioBt -> {
                    checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_COD
                }
                R.id.cardRadioBt -> {
                    checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_CARD
                }
                R.id.walletRadioBt -> {
                    checkoutRequest.paymentMode = Constants.PAYMENT_TYPE_WALLET
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
            R.id.applyCouponCodeTv -> {
                KeyboardUtils.hideSoftInput(this)
                val couponCode = CouponCodeEditText.text.toString().trim()
                if (appliedCouponCodes.contains(couponCode)) {
                    Toast.makeText(this, getString(R.string.coupon_already_applied), Toast.LENGTH_LONG).show()
                } else if (couponCode.isNotBlank()) {
                    if (isNetworkAvailable())
                        callApplyCouponCodeApi(couponCode)
                }

            }

            R.id.applyLoyaltyPointTv -> {
                KeyboardUtils.hideSoftInput(this)
                val loyaltyPoint = LoyaltyPointEditText.text.toString().trim()
                if (loyaltyPoint.isNotBlank()) {
                    if (applyLoyaltyPointTv.text == getString(R.string.apply)) {
                        if (isNetworkAvailable())
                            callApplyLoyaltyPointApi(loyaltyPoint)
                    } else {
                        //setCouponCodeEdibility(true)
                        LoyaltyPointEditText.setText("")
                        LoyaltyPointEditText.isEnabled = true
                        applyLoyaltyPointTv.text = getString(R.string.apply)
                        setLoyaltyPointDiscount(0.0)
                    }
                }

            }
            R.id.CouponCodeEditText -> {
                val intent = Intent(this, CouponCodeActivity::class.java)
                startActivityForResult(intent, AppRequestCodes.APPLY_COUPON_CODE)
            }

            R.id.confirmOrderBt -> {
                KeyboardUtils.hideSoftInput(this)

                if (isValidateCheckoutRequest()) {
                    checkoutRequest.deliveryEndTime = deliveryTimeSlotEndTime
                    checkoutRequest.deliveryStartTime = deliveryTimeSlotStartTime
                    checkoutRequest.userId = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
                    checkoutRequest.language = DrewelApplication.getInstance().getLanguage()
                    checkoutRequest.cartId = pref!!.getPreferenceStringData(pref!!.KEY_CART_ID)
                    checkoutRequest.deliveryLatitude = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LATITUDE)
                    checkoutRequest.deliveryLongitude = pref!!.getPreferenceStringData(pref!!.KEY_DELIVERY_ADDRESS_LONGITUDE)
                    deliveryDate
                    checkoutRequest.deliveryAddress = deliveryAddressTv.text.toString()
                    checkoutRequest.deliveryDate = deliveryDate
                    checkoutRequest.loyaltyPoints = if (isApplied) LoyaltyPointEditText.text.toString() else ""
                    checkoutRequest.deliveryCharges = deliveryChargesTv.text.toString().split(" ")[0]
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
                Toast.makeText(this, getString(R.string.coupon_already_applied), Toast.LENGTH_LONG).show()
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
                    deliveryChargesTv.text = DecimalFormat("#.##").format(deliveryChargesResponse?.expediteDeliveryCharges?.toDouble()) + " " + getString(R.string.omr)
                chooseDeliveryTypeTv.text = date.toString() + ", " + deliveryTimeSlotStartTime + "- " + deliveryTimeSlotEndTime
                setGrandTotal()
            } else if (checkoutRequest.deliveryType.equals(Constants.SAME_DAY_DELIVERY)) {
                if (deliveryChargesResponse?.deliveryCharge!!.isNotEmpty())
                    deliveryChargesTv.text = DecimalFormat("#.##").format(deliveryChargesResponse!!.sameDayDeliveryCharge!!.toDouble()) + " " + getString(R.string.omr)
                chooseDeliveryTypeTv.text = date.toString() + ", " + deliveryTimeSlotStartTime + "- " + deliveryTimeSlotEndTime
                setGrandTotal()
            } else {
                if (deliveryChargesResponse?.deliveryCharge!!.isNotEmpty())
                    deliveryChargesTv.text = DecimalFormat("#.##").format(deliveryChargesResponse!!.deliveryCharge!!.toDouble()) + " " + getString(R.string.omr)
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
            Toast.makeText(this, getString(R.string.select_delivery_time_slot), Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }


    private fun showDeliveryTypeDialog() {
        if (deliveryTypeAlertDialog == null) {
            deliveryTypeAlertDialog = AlertDialog.Builder(this, R.style.DeliveryTypeTheme).create()

            deliveryTypeAlertDialog!!.setTitle(getString(R.string.choose_delivery_type))


            deliveryTypeAlertDialog!!.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.deliver_now), { dialog, id ->

                chooseDeliveryTypeTv.text = getString(R.string.deliver_now)
                if (deliveryChargesResponse?.deliveryCharge!!.isNotEmpty())
                    deliveryChargesTv.text = DecimalFormat("#.##").format(deliveryChargesResponse?.deliveryCharge?.toDouble()) + " " + getString(R.string.omr)

                setGrandTotal()

                checkoutRequest.deliveryType = Constants.DELIVERY_NOW

                /* get time slot for delivery now*/
                val startCalendar = Calendar.getInstance()
                if (startCalendar.get(Calendar.MINUTE) < 30) {
                    startCalendar.set(Calendar.MINUTE, 0)
                } else {
                    startCalendar.add(Calendar.MINUTE, 30) // overstep hour and clear minutes
                    startCalendar.clear(Calendar.MINUTE)
                }
                val slotTime = SimpleDateFormat("HH:mm:ss")
                deliveryTimeSlotStartTime = slotTime.format(startCalendar.time)
                startCalendar.add(Calendar.MINUTE, 60)
                deliveryTimeSlotEndTime = slotTime.format(startCalendar.time)

                deliveryDate = SimpleDateFormat("yyyy-MM-dd").format(startCalendar.time)

            })

            deliveryTypeAlertDialog!!.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.same_day_delivery), DialogInterface.OnClickListener { dialog, id ->
                chooseDeliveryTypeTv.text = getString(R.string.same_day_delivery)
                deliveryTimeSlots(Calendar.getInstance())
                if (deliveryChargesResponse?.deliveryCharge!!.isNotEmpty())
                    deliveryChargesTv.text = DecimalFormat("#.##").format(deliveryChargesResponse!!.sameDayDeliveryCharge!!.toDouble()) + " " + getString(R.string.omr)
                setGrandTotal()
                checkoutRequest.deliveryType = Constants.SAME_DAY_DELIVERY
            })

            deliveryTypeAlertDialog!!.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.next_day_delivery), DialogInterface.OnClickListener { dialog, id ->
                chooseDeliveryTypeTv.text = getString(R.string.next_day_delivery)
                val calender = Calendar.getInstance()
                calender.add(Calendar.DAY_OF_MONTH, 1)
                calender.set(Calendar.HOUR_OF_DAY, 0)
                calender.set(Calendar.MINUTE, 0)
                deliveryTimeSlots(calender)
                if (deliveryChargesResponse?.deliveryCharge!!.isNotEmpty())
                    deliveryChargesTv.text = DecimalFormat("#.##").format(deliveryChargesResponse!!.expediteDeliveryCharges!!.toDouble()) + " " + getString(R.string.omr)
                setGrandTotal()
                checkoutRequest.deliveryType = Constants.NEXT_DAY_DELIVERY.toString()
            })
        }
        deliveryTypeAlertDialog!!.show()
    }

    private fun setGrandTotal() {
        val discount = discountTv.text.toString().split(" ")[0].toFloat()
        val subTotal = subTotalTv.text.split(" ")[0].toFloat()
        val deliverCharge = deliveryChargesTv.text.toString().split(" ")[0].toFloat()
        val discountedAmount = (subTotal - discount)

        val grandTotal = if (discountedAmount > 0)
            discountedAmount + deliverCharge
        else
            deliverCharge

        if (deliveryChargesResponse!!.is_edited == "1") {
            if (deliveryChargesResponse!!.last_paid!!.isNotEmpty() ) {
                ll_lastpaid.visibility = View.VISIBLE
                last_paidTv.setText(deliveryChargesResponse!!.last_paid + " " + getString(R.string.omr))
                if (deliveryChargesResponse!!.last_paid!!.toDouble() > grandTotal.toDouble()) {
                    var diffAmount = deliveryChargesResponse!!.last_paid!!.toDouble() - grandTotal.toDouble()
                    if (diffAmount != 0.0) {
                        last_instruction.visibility = View.VISIBLE
                        last_instruction.setText("(" + DecimalFormat("#.##").format(diffAmount).toString() + " " + getString(R.string.amount_will_be_transfered) + ")")

                    } else
                        last_instruction.visibility = View.GONE
                    grandTotalTv.text = "0 " + getString(R.string.omr)
//                    last_instruction.visibility = View.VISIBLE
                    cardRadioBt.isChecked = true
                    cashRadioBt.isEnabled = false
                    walletRadioBt.isEnabled = false
                } else {
                    var diffAmount = grandTotal.toDouble() - deliveryChargesResponse!!.last_paid!!.toDouble()
                    Log.e("diff Amount", diffAmount.toString())
//                                    last_instruction.setText("(" + diffAmount.toString() + " " + getString(R.string.amount_will_be_transfered) + ")")
                    grandTotalTv.text = DecimalFormat("#.##").format(diffAmount).toString() + " " + getString(R.string.omr)
                    last_instruction.visibility = View.GONE
                    cardRadioBt.isChecked = true
                }
            } else {
                last_instruction.visibility = View.GONE
                if (grandTotal > 0) {
                    grandTotalTv.text = DecimalFormat("#.##").format(grandTotal) + " " + getString(R.string.omr)
                } else
                    grandTotalTv.text = "0 " + getString(R.string.omr)
            }
        } else {
            last_instruction.visibility = View.GONE
            if (grandTotal > 0) {
                grandTotalTv.text = DecimalFormat("#.##").format(grandTotal) + " " + getString(R.string.omr)
            } else
                grandTotalTv.text = "0 " + getString(R.string.omr)
        }


//        if (grandTotal > 0) {
//            grandTotalTv.text = DecimalFormat("#.##").format(grandTotal) + " " + getString(R.string.omr)
//            if (deliveryChargesResponse!!.is_edited == "1") {
//                if (deliveryChargesResponse!!.last_paid!!.isNotEmpty()) {
//                    rl_lastpaid.visibility = View.VISIBLE
//                    if (deliveryChargesResponse!!.last_paid!!.toDouble() > grandTotal) {
//                        var diffAmount = deliveryChargesResponse!!.last_paid!!.toDouble() - grandTotal
//                        if (diffAmount > 0) {
//                            last_instruction.visibility = View.VISIBLE
//                            last_instruction.setText("(" + DecimalFormat("#.##").format(diffAmount).toString() + " " + getString(R.string.amount_will_be_transfered) + ")")
//                            grandTotalTv.text = DecimalFormat("#.##").format(diffAmount) + " " + getString(R.string.omr)
//                            grandTotalTv.text = "0 " + getString(R.string.omr)
//                        } else {
//                            var diffAmount = grandTotal - deliveryChargesResponse!!.last_paid!!.toDouble()
//                            last_instruction.visibility = View.GONE
//                            grandTotalTv.text = DecimalFormat("#.##").format(diffAmount) + " " + getString(R.string.omr)
//                        }
//                    }
//                } else
//                    rl_lastpaid.visibility = View.GONE
//            } else
//                rl_lastpaid.visibility = View.GONE
//        } else
//            grandTotalTv.text = "0 " + getString(R.string.omr)
    }

    private fun setDiscount(newDiscount: Double) {

        val previousDiscount = discountTv.text.toString().split(" ")[0].toFloat()

        val totalDiscount = (newDiscount + previousDiscount)

        discountTv.text = DecimalFormat("#.##").format(totalDiscount) + " " + getString(R.string.omr)

    }


    private fun removeDiscount(newDiscount: Double) {

        val previousDiscount = discountTv.text.toString().split(" ")[0].toFloat()

        val totalDiscount = (previousDiscount - newDiscount)

        discountTv.text = DecimalFormat("#.##").format(totalDiscount) + " " + getString(R.string.omr)
    }

    /*remove previous loyalty point discount from total discount*/
    private fun setLoyaltyPointDiscount(loyaltyPointsDiscount: Double) {
        val previousDiscount = discountTv.text.toString().split(" ")[0].toFloat()

        var totalDiscount = (loyaltyPointsDiscount + previousDiscount) - previousLoyaltyPointDiscount

        if (totalDiscount < 0)
            totalDiscount = 0.0

        previousLoyaltyPointDiscount = loyaltyPointsDiscount

        discountTv.text = DecimalFormat("#.##").format(totalDiscount) + " " + getString(R.string.omr)
    }

    /* when user click on remove coupon button adapter then deduct discount from previously applied discount*/
    override fun onCouponCodeRemove(position: Int) {
        removeDiscount(appliedCouponCodesAllInfo[position].discountAmount!!.toDouble())
        appliedCouponCodes.remove(appliedCouponCodesAllInfo[position].couponCode)
        appliedCouponCodesAllInfo.removeAt(position)
        appliedCouponCodeAdapter?.notifyItemRemoved(position)
        appliedCouponCodeAdapter?.notifyItemRangeRemoved(0, appliedCouponCodesAllInfo.size)
        //setLoyaltyPointEdibility(true)
        // CouponCodeEditText.setText("")
    }


    fun deliveryTimeSlots(startCalendar: Calendar) {
        try {
            timeSlotList.clear()
            if (startCalendar[Calendar.HOUR_OF_DAY] < 23) {
                if (startCalendar.get(Calendar.MINUTE) < 30) {
                    startCalendar.set(Calendar.MINUTE, 0)
                } else {
                    startCalendar.add(Calendar.MINUTE, 30) // overstep hour and clear minutes
                    startCalendar.clear(Calendar.MINUTE)
                }

                if (startCalendar[Calendar.HOUR_OF_DAY] < 9/*deliveryChargesResponse!!.deliveryStartTime!!.toInt()*/) {
                    startCalendar.set(Calendar.HOUR_OF_DAY, 9/*deliveryChargesResponse!!.deliveryStartTime!!.toInt()*/)
                }

                val endCalendar = Calendar.getInstance()
                endCalendar.time = startCalendar.time

                // if you want dates for whole next day, uncomment next line
                //endCalendar.add(Calendar.DAY_OF_YEAR, 1);

                /* we want 9Am to 11 PM*/
                endCalendar.add(Calendar.HOUR_OF_DAY, 24 - startCalendar.get(Calendar.HOUR_OF_DAY) - 1)

                endCalendar.clear(Calendar.MINUTE)
                endCalendar.clear(Calendar.SECOND)
                endCalendar.clear(Calendar.MILLISECOND)

                val slotTime = SimpleDateFormat("hh:mm a", Locale.getDefault())

                while (endCalendar.after(startCalendar)) {
                    val slotStartTime = slotTime.format(startCalendar.time)
                    startCalendar.add(Calendar.MINUTE, 60)
                    val slotEndTime = slotTime.format(startCalendar.time)
                    timeSlotList.add("$slotStartTime - $slotEndTime")

                    Log.d("DATE", "$slotStartTime - $slotEndTime")
                }

                deliveryDate = SimpleDateFormat("yyyy-MM-dd").format(startCalendar.time)

                if (timeSlotList.isNotEmpty()) {
                    val timeSlotBottomSheetDialog = TimeSlotBottomSheetDialog(this, timeSlotList)
                    timeSlotBottomSheetDialog.show()

                    if (deliverySlotSelect != null)
                        deliverySlotSelect!!.dispose()

                    /* when user click on set delivery slot button from dialog then selected result will come here*/
                    deliverySlotSelect = timeSlotBottomSheetDialog.deliverySlotSelect.subscribe(
                            { result ->

                                selectedTimeSlot = result

                                val timeSlotAry = selectedTimeSlot.split("-")

                                deliveryTimeSlotStartTime = Utils.getInstance().convertTimeFormat(timeSlotAry[0], "hh:mm a", "HH:mm:ss")
                                deliveryTimeSlotEndTime = Utils.getInstance().convertTimeFormat(timeSlotAry[0], "hh:mm a", "HH:mm:ss")


                            }, { error ->
                        error.printStackTrace()
                    })
                } else {
                    Toast.makeText(this, getString(R.string.no_time_slot_available_today), Toast.LENGTH_SHORT).show()
                    chooseDeliveryTypeTv.text = ""
                }
            } else {
                Toast.makeText(this, getString(R.string.no_time_slot_available_today), Toast.LENGTH_SHORT).show()
                chooseDeliveryTypeTv.text = ""
            }
        } catch (e: Exception) {
            Log.d("error", e.message)
        }

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
                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
                    }

                }, { error ->
                    setProgressState(View.GONE, View.VISIBLE)
                    applyCouponCodeTv.isEnabled = true
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
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
                            if (result.response!!.data!!.last_paid!!.isNotEmpty()) {
                                ll_lastpaid.visibility = View.VISIBLE
                                last_paidTv.setText(result.response!!.data!!.last_paid + " " + getString(R.string.omr))
                                if (result.response!!.data!!.last_paid!!.toDouble() > orderNetPrice.toDouble()) {
                                    var diffAmount = result.response!!.data!!.last_paid!!.toDouble() - orderNetPrice.toDouble()
                                    if (diffAmount != 0.0) {
                                        last_instruction.setText("(" + DecimalFormat("#.##").format(diffAmount).toString() + " " + getString(R.string.amount_will_be_transfered) + ")")
                                        last_instruction.visibility = View.VISIBLE
                                    } else {
                                        last_instruction.visibility = View.GONE
                                    }
                                    grandTotalTv.text = "0 " + getString(R.string.omr)

                                    cardRadioBt.isChecked = true
                                    cashRadioBt.isEnabled = false
                                    walletRadioBt.isEnabled = false
                                } else {
                                    var diffAmount = orderNetPrice.toDouble() - result.response!!.data!!.last_paid!!.toDouble()
//                                    last_instruction.setText("(" + diffAmount.toString() + " " + getString(R.string.amount_will_be_transfered) + ")")
                                    grandTotalTv.text = DecimalFormat("#.##").format(diffAmount).toString() + " " + getString(R.string.omr)
                                    last_instruction.visibility = View.GONE
                                    cardRadioBt.isChecked = true
                                    cashRadioBt.isEnabled = false
                                    walletRadioBt.isEnabled = false
                                }
                            }
                        } else{
                            last_instruction.visibility = View.GONE
                            ll_lastpaid.visibility = View.GONE
                        }
                    } else
                        Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()

                }, { error ->
                    setProgressState(View.GONE, View.VISIBLE)
                    applyCouponCodeTv.isEnabled = true
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

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
                    Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()

                    applyLoyaltyPointTv.isEnabled = true

                    if (result.response!!.status!!) {
                        setLoyaltyPointDiscount(result.response!!.data!!.discount.toString().toDouble())
                        setGrandTotal()
                        LoyaltyPointEditText.isEnabled = false
                        applyLoyaltyPointTv.text = getString(R.string.remove)
                        // setCouponCodeEdibility(false)
                    } else {
                        LoyaltyPointEditText.setText("")
                    }

                }, { error ->
                    setProgressState(View.GONE, View.VISIBLE)
                    applyLoyaltyPointTv.isEnabled = true
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, result.response!!.message, Toast.LENGTH_LONG).show()
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
                        startActivity(intent)
                        finish()
                    }
                }, { error ->
                    confirmOrderBt.isEnabled = true
                    setProgressState(View.GONE, View.VISIBLE)
                    Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
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