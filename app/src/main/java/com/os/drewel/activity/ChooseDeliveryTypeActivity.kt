package com.os.drewel.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.KeyboardUtils
import com.os.drewel.R
import com.os.drewel.adapter.AppliedCouponCodeAdapter
import com.os.drewel.adapter.TimeSlotAdapter
import com.os.drewel.apicall.responsemodel.applycouponresponsemodel.Coupon
import com.os.drewel.apicall.responsemodel.deliverychargesresponsemodel.Data
import com.os.drewel.constant.AppIntentExtraKeys
import com.os.drewel.constant.AppRequestCodes
import com.os.drewel.constant.Constants
import com.os.drewel.delegate.OnClick
import com.os.drewel.model.TimeSlotModel
import com.os.drewel.utill.DateUtils
import com.os.drewel.utill.EqualSpacingItemDecoration
import com.os.drewel.utill.Utils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import devs.mulham.horizontalcalendar.HorizontalCalendar
import devs.mulham.horizontalcalendar.HorizontalCalendarView
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import kotlinx.android.synthetic.main.activity_choosedeliverytype.*
import java.text.ParseException
import java.util.concurrent.TimeUnit


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
        setContentView(R.layout.activity_choosedeliverytype)
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

        val horizontalCalendar = HorizontalCalendar.Builder(this, R.id.calendarView)
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
                    isCalled=false
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
            R.id.txt_submit -> {
                if (deliveryType.isEmpty() || deliveryDate.isEmpty() || deliveryTimeSlotStartTime.isEmpty() || deliveryTimeSlotEndTime.isEmpty()) {
                    Utils.getInstance().showToast(this, getString(R.string.choose_delivery_time_slot_validation))
                } else {
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
            R.id.txt_delivery_charges -> {
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
//          endCalendar.time = startCalendar.time
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
                    timeSlotModel.fare = String.format("%.3f", deliveryChargesResponse!!.sameDayDeliveryCharge!!.toDouble()) + " " + getString(R.string.omr)
                    timeSlotModel.deliverType = Constants.SAME_DAY_DELIVERY
                } else {
                    timeSlotModel.fare = String.format("%.3f", deliveryChargesResponse!!.deliveryCharge!!.toDouble()) + " " + getString(R.string.omr)
                    timeSlotModel.deliverType = Constants.NEXT_DAY_DELIVERY
                }
                if (isToday) {
                    if (strt.after(Calendar.getInstance().time)) {
                        timeSlotList.add(timeSlotModel)
                    } else {
                        if (strt.before(Calendar.getInstance().time) && end.after(Calendar.getInstance().time)) {
                            timeSlotModel.fare = String.format("%.3f", deliveryChargesResponse!!.expediteDeliveryCharges!!.toDouble()) + " " + getString(R.string.omr)
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
                Utils.getInstance().showToast(this, getString(R.string.no_time_slot_available_today))

            }
        } catch (e: Exception) {
            Log.d("error", e.message)
        }

    }

    fun setDeliveryTimeSlot2(startCalendar: Calendar, isToday: Boolean) {
        try {
            timeSlotList.clear()
            if (startCalendar[Calendar.HOUR_OF_DAY] < 23) {
                if (startCalendar.get(Calendar.MINUTE) < 30) {
                    startCalendar.set(Calendar.MINUTE, 0)
                } else {
                    startCalendar.add(Calendar.MINUTE, 30) // overstep hour and clear minutes
                    startCalendar.clear(Calendar.MINUTE)
                }

                if (startCalendar[Calendar.HOUR_OF_DAY] < /*9*/deliveryChargesResponse!!.deliveryStartTime!!.toInt()) {
                    startCalendar.set(Calendar.HOUR_OF_DAY, /*9*/deliveryChargesResponse!!.deliveryStartTime!!.toInt())
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
                    var timeSlotModel = TimeSlotModel()
                    timeSlotModel.isCheck = false
                    timeSlotModel.slot = "$slotStartTime - $slotEndTime"
                    timeSlotList.add(timeSlotModel)
                    Log.d("DATE", "$slotStartTime - $slotEndTime")
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
                            if (isToday)
                                deliveryType = Constants.SAME_DAY_DELIVERY
                            else
                                deliveryType = Constants.NEXT_DAY_DELIVERY
                            val timeSlotAry = selectedTimeSlot.split("-")
                            deliveryTimeSlotStartTime = Utils.getInstance().convertTimeFormat(timeSlotAry[0], "hh:mm a", "HH:mm:ss")
                            deliveryTimeSlotEndTime = Utils.getInstance().convertTimeFormat(timeSlotAry[0], "hh:mm a", "HH:mm:ss")

                        }
                    })
                    rv_slots.layoutManager = LinearLayoutManager(this)
                    rv_slots.adapter = timeSlotAdapter

                    if (deliverySlotSelect != null)
                        deliverySlotSelect!!.dispose()

                } else {
                    Utils.getInstance().showToast(this, getString(R.string.no_time_slot_available_today))
//                    chooseDeliveryTypeTv.text = ""
                }
            } else {
                Utils.getInstance().showToast(this, getString(R.string.no_time_slot_available_today))
//                chooseDeliveryTypeTv.text = ""
            }
        } catch (e: Exception) {
            Log.d("error", e.message)
        }

    }


    /*fun deliveryTimeSlots(startCalendar: Calendar) {
        try {
            timeSlotList.clear()
            if (startCalendar[Calendar.HOUR_OF_DAY] < 23) {
                if (startCalendar.get(Calendar.MINUTE) < 30) {
                    startCalendar.set(Calendar.MINUTE, 0)
                } else {
                    startCalendar.add(Calendar.MINUTE, 30) // overstep hour and clear minutes
                    startCalendar.clear(Calendar.MINUTE)
                }

                if (startCalendar[Calendar.HOUR_OF_DAY] < 9*//*deliveryChargesResponse!!.deliveryStartTime!!.toInt()*//*) {
                    startCalendar.set(Calendar.HOUR_OF_DAY, 9*//*deliveryChargesResponse!!.deliveryStartTime!!.toInt()*//*)
                }

                val endCalendar = Calendar.getInstance()
                endCalendar.time = startCalendar.time

                // if you want dates for whole next day, uncomment next line
                //endCalendar.add(Calendar.DAY_OF_YEAR, 1);

                *//* we want 9Am to 11 PM*//*
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

                    *//* when user click on set delivery slot button from dialog then selected result will come here*//*
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

    }*/

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}