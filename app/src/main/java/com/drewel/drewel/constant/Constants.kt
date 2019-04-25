package com.drewel.drewel.constant

import org.joda.time.DateTimeZone

/**
 * Created by monikab on 3/13/2018.
 */
object Constants {

    const val customerRoleId = "2"
    const val driverRoleId = "3"


    const val PAYMENT_TYPE_COD = "1"
    const val PAYMENT_TYPE_CARD = "2"
    const val PAYMENT_TYPE_WALLET = "3"
    const val PAYMENT_TYPE_THAWANI = "4"

    const val DELIVERY_NOW = "1"
    const val SAME_DAY_DELIVERY = "2"
    const val NEXT_DAY_DELIVERY = "3"

    const val CURRENT_ORDERS = "1"
    const val PREVIOUS_ORDERS = "2"

    val timeZone = DateTimeZone.UTC!!

    const val LANGUAGE_ARABIC = "ar"
    const val LANGUAGE_ENGLISH = "en"
    const val CURRENT_ORDER = 1
    const val COMPLETED_ORDER = 2
}