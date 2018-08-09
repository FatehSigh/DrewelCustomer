package com.os.drewel.model

class TimeSlotModel {
    var deliverType: String = ""
    var slot: String = ""
    var fare: String = ""
    var isCheck: Boolean? = false
    override fun toString(): String {
        return "TimeSlotModel(slot='$slot', fare='$fare', isCheck=$isCheck)"
    }

}