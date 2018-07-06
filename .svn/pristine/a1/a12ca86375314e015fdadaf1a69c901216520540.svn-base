package com.os.drewel.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.view.View
import android.view.WindowManager
import com.os.drewel.R
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.time_slot_layout.*


/**
 * Created by monikab on 3/21/2018.
 */
class TimeSlotBottomSheetDialog(val mContext: Context, val timeSlotList: MutableList<String>) : Dialog(mContext) , View.OnClickListener{

    val deliverySlotSelect = PublishSubject.create<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(context.getString(R.string.share_product))
        setContentView(R.layout.time_slot_layout)

        timeSlotWheelView.setCanLoop(false)
        timeSlotWheelView.setLoopListener({ item -> })
        timeSlotWheelView.setItems(timeSlotList)

        setDeliveryTimeBt.setOnClickListener(this)
    }

    public override fun onStart() {
        super.onStart()
        val lp = WindowManager.LayoutParams()
        val window = window
        lp.copyFrom(window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
    }

    override fun onClick(view: View) {
            dismiss()
        deliverySlotSelect.onNext(timeSlotList[timeSlotWheelView.selectedItem])
    }


}