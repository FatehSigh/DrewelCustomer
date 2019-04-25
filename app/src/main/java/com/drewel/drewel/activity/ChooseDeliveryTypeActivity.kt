package com.drewel.drewel.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.drewel.drewel.adapter.TimeSlotAdapter
import com.drewel.drewel.apicall.DrewelApi
import com.drewel.drewel.apicall.responsemodel.deliverychargesresponsemodel.Data
import com.drewel.drewel.application.DrewelApplication
import com.drewel.drewel.constant.Constants
import com.drewel.drewel.delegate.OnClick
import com.drewel.drewel.model.TimeSlotModel
import com.drewel.drewel.utill.DateUtils
import com.drewel.drewel.utill.EqualSpacingItemDecoration
import com.drewel.drewel.utill.Utils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.HorizontalCalendarView
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_choosedeliverytype.*
import java.text.ParseException


/**
 * Created by monikab on 4/4/2018.
 */
class ChooseDeliveryTypeActivity : BaseActivity(), View.OnClickListener {
    private var selectedTimeSlot: String = ""
    private var deliverySlotSelect: Disposable? = null
    private var deliveryTimeSlotStartTime = ""
    private var deliveryTimeSlotEndTime = ""
    private var deliveryDate = ""
    private var deliveryChargesResponse: Data? = null
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    var deliveryType = ""
    var isCalled = true;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.drewel.drewel.R.layout.activity_choosedeliverytype)
        initView()
    }


    private fun initView() {
        deliveryChargesResponse = intent.getParcelableExtra("Data")
//        txt_delivery_charges.setText(getString(R.string.omr) + " " + String.format("%.3f", deliveryChargesResponse!!.expediteDeliveryCharges!!.toDouble()))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        txt_submit.setOnClickListener(this)
        rv_slots.layoutManager = LinearLayoutManager(this)
        txt_delivery_charges.setOnClickListener(this)
        val startDate = Calendar.getInstance()
        startDate.add(Calendar.DATE, 0)
        /* ends after 1 month from now */
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.DATE, 6)

        val horizontalCalendar = HorizontalCalendar.Builder(this, com.drewel.drewel.R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .build()
        horizontalCalendar.selectDate(Calendar.getInstance(), true)

        horizontalCalendar.calendarListener = object : HorizontalCalendarListener() {
            override fun onDateSelected(date: Calendar, position: Int) {
                Log.e("called ", "onDateSelected=" + date)
                rv_slots.adapter = null
                //do something
                if (DateUtils.isToday(date)) {
                    setDeliveryTimeSlot(Calendar.getInstance(), true)
                } else {
//                    val calender = Calendar.getInstance()
//                    calender.add(Calendar.DAY_OF_MONTH, 1)
                    date.set(Calendar.HOUR_OF_DAY, 0)
                    date.set(Calendar.MINUTE, 0)
                    setDeliveryTimeSlot(date, false)
                }
//                setDeliveryTimeSlot(date, true)
            }

            override fun onCalendarScroll(calendarView: HorizontalCalendarView?, dx: Int, dy: Int) {
                super.onCalendarScroll(calendarView, dx, dy)
                if (isCalled) {
                    isCalled = false
//                    horizontalCalendar.selectDate(Calendar.getInstance(), true)
                } else {
                    Log.e("called ", "onCalendarScroll=" + horizontalCalendar.getDateAt(calendarView!!.positionOfCenterItem))
                    horizontalCalendar.selectDate(horizontalCalendar.getDateAt(calendarView.positionOfCenterItem), true)
                }
            }
        }


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
            com.drewel.drewel.R.id.txt_submit -> {
                if (deliveryType.isEmpty() || deliveryDate.isEmpty() || deliveryTimeSlotStartTime.isEmpty() || deliveryTimeSlotEndTime.isEmpty()) {
                    Utils.getInstance().showToast(this, getString(com.drewel.drewel.R.string.choose_delivery_time_slot_validation))
                } else {

                   /* intent.putExtra("timeslot", selectedTimeSlot)
                    intent.putExtra("deliveryDate", deliveryDate)
                    intent.putExtra("deliveryType", deliveryType)
                    intent.putExtra("deliveryTimeSlotStartTime", deliveryTimeSlotStartTime)
                    intent.putExtra("deliveryTimeSlotEndTime", deliveryTimeSlotEndTime)*/

                    callDeliveryTimeSlotAvilable(deliveryDate,deliveryTimeSlotStartTime,deliveryTimeSlotEndTime)
                }
            }
            com.drewel.drewel.R.id.txt_delivery_charges -> {
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
                deliveryType = Constants.DELIVERY_NOW
                val intent = Intent()
                intent.putExtra("timeslot", selectedTimeSlot)
                intent.putExtra("deliveryDate", deliveryDate)
                intent.putExtra("deliveryType", deliveryType)
                intent.putExtra("deliveryTimeSlotStartTime", deliveryTimeSlotStartTime)
                intent.putExtra("deliveryTimeSlotEndTime", deliveryTimeSlotEndTime)
                setResult(Activity.RESULT_OK, intent)
                finish()

            }
        }
    }


    var timeSlotAdapter: TimeSlotAdapter? = null
    private var timeSlotList: MutableList<TimeSlotModel> = ArrayList()
    fun setDeliveryTimeSlot(startCalendar: Calendar, isToday: Boolean) {
        try {
            timeSlotList.clear()
            val endCalendar = Calendar.getInstance()
            val endtime = Calendar.getInstance()
            val startTime = Calendar.getInstance()
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            try {
                endtime.time = format.parse(deliveryChargesResponse!!.deliveryEndTime)
                startTime.time = format.parse(deliveryChargesResponse!!.deliveryStartTime)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            startCalendar.set(Calendar.HOUR_OF_DAY, /*9*/startTime.get(Calendar.HOUR_OF_DAY))
            startCalendar.set(Calendar.MINUTE, /*9*/startTime.get(Calendar.MINUTE))
            endCalendar.time = startCalendar.time
            endCalendar.set(Calendar.HOUR_OF_DAY, endtime.get(Calendar.HOUR_OF_DAY))
            endCalendar.set(Calendar.MINUTE, endtime.get(Calendar.MINUTE))
            endCalendar.clear(Calendar.MINUTE)
            endCalendar.clear(Calendar.SECOND)
            endCalendar.clear(Calendar.MILLISECOND)

            val slotTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            while (endCalendar.after(startCalendar)) {
                val slotStartTime = slotTime.format(startCalendar.time)
                val strt = startCalendar.time
                val amount = (deliveryChargesResponse!!.deliverySlotDuration)!!.toFloat() * 60
                startCalendar.add(Calendar.MINUTE, amount.toInt())
                val end = startCalendar.time
                val slotEndTime = slotTime.format(startCalendar.time)
                val timeSlotModel = TimeSlotModel()
                timeSlotModel.isCheck = false
                timeSlotModel.slot = "$slotStartTime - $slotEndTime"
                if (isToday) {
                    timeSlotModel.fare = String.format("%.3f", deliveryChargesResponse!!.sameDayDeliveryCharge!!.toDouble()) + " " + getString(com.drewel.drewel.R.string.omr)
                    timeSlotModel.deliverType = Constants.SAME_DAY_DELIVERY
                } else {
                    timeSlotModel.fare = String.format("%.3f", deliveryChargesResponse!!.deliveryCharge!!.toDouble()) + " " + getString(com.drewel.drewel.R.string.omr)
                    timeSlotModel.deliverType = Constants.NEXT_DAY_DELIVERY
                }
                if (isToday) {
                    val calendarToday = Calendar.getInstance()
                    calendarToday.time = strt
                    calendarToday.add(Calendar.HOUR, -1)

                    val calendarEnd = Calendar.getInstance()
                    calendarEnd.time = strt
                    calendarEnd.add(Calendar.HOUR, -1)


                    if (calendarToday.time.after(Calendar.getInstance().time)) {
                        Log.d("StartTime1", "$slotStartTime-$slotEndTime")
                        timeSlotList.add(timeSlotModel)
                    } else {
                        if (calendarToday.time.before(Calendar.getInstance().time) && calendarEnd.time.after(Calendar.getInstance().time)) {

                            Log.d("StartTime1", "$slotStartTime-$slotEndTime")

                            timeSlotModel.fare = String.format("%.3f", deliveryChargesResponse!!.expediteDeliveryCharges!!.toDouble()) + " " + getString(com.drewel.drewel.R.string.omr)
                            timeSlotModel.deliverType = Constants.DELIVERY_NOW
                            timeSlotList.add(timeSlotModel)
                        }
                    }
                } else
                    timeSlotList.add(timeSlotModel)
            }

            deliveryDate = SimpleDateFormat("yyyy-MM-dd", Locale(Constants.LANGUAGE_ENGLISH)).format(startCalendar.time)
            if (timeSlotList.isNotEmpty()) {
                timeSlotAdapter = TimeSlotAdapter(timeSlotList, object : OnClick {
                    override fun onClick(position: Int) {
                        for (i in timeSlotList.indices) {
                            timeSlotList[i].isCheck = position == i
                        }
                        timeSlotAdapter!!.notifyDataSetChanged()
                        selectedTimeSlot = timeSlotList[position].slot
                        deliveryType = timeSlotList[position].deliverType
                        val timeSlotAry = selectedTimeSlot.split("-")
                        deliveryTimeSlotStartTime = Utils.getInstance().convertTimeFormat(timeSlotAry[0], "hh:mm a", "HH:mm:ss")
                        deliveryTimeSlotEndTime = Utils.getInstance().convertTimeFormat(timeSlotAry[1], "hh:mm a", "HH:mm:ss")



                    }
                })
                rv_slots.layoutManager = LinearLayoutManager(this)
                rv_slots.addItemDecoration(EqualSpacingItemDecoration(16, EqualSpacingItemDecoration.GRID))
                rv_slots.adapter = timeSlotAdapter

                if (deliverySlotSelect != null)
                    deliverySlotSelect!!.dispose()
            } else {
                Utils.getInstance().showToast(this, getString(com.drewel.drewel.R.string.no_time_slot_available_today))

            }
        } catch (e: Exception) {
            Log.d("error", e.message)
        }

    }


    private fun callDeliveryTimeSlotAvilable(deliveryDate: String,deliveryStartTime: String,deliveryEndTime: String) {

        //user_id,language,delivery_date,delivery_start_time,delivery_end_time
        val applyCouponCodeRequest = HashMap<String, String>()
        applyCouponCodeRequest["user_id"] = pref!!.getPreferenceStringData(pref!!.KEY_USER_ID)
        applyCouponCodeRequest["language"] = DrewelApplication.getInstance().getLanguage()
        applyCouponCodeRequest["delivery_date"] = deliveryDate
        applyCouponCodeRequest["delivery_start_time"] = deliveryStartTime
        applyCouponCodeRequest["delivery_end_time"] = deliveryEndTime

        val applyCouponCodeObservable = DrewelApplication.getInstance().getRequestQueue().
                create(DrewelApi::class.java).deliverySlotCheck(applyCouponCodeRequest)
        compositeDisposable.add(applyCouponCodeObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->

                    var jsonObj=result.get("response").asJsonObject
                   var status= jsonObj.get("status").asString
                    if (status.equals("1",true)) {
                        val intent = Intent()
                        intent.putExtra("timeslot", selectedTimeSlot)
                        intent.putExtra("deliveryDate", deliveryDate)
                        intent.putExtra("deliveryType", deliveryType)
                        intent.putExtra("deliveryTimeSlotStartTime", deliveryTimeSlotStartTime)
                        intent.putExtra("deliveryTimeSlotEndTime", deliveryTimeSlotEndTime)
                        setResult(Activity.RESULT_OK, intent)
                        finish()

                    } else {
                        Utils.getInstance().showToast(this, jsonObj.get("message").asString!!)
                    }

                }, { error ->
                    //  setProgressState(View.GONE, View.VISIBLE)
                    Utils.getInstance().showToast(this, error.message!!)
                    Log.e("TAG", "{$error.message}")
                }
                ))
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}