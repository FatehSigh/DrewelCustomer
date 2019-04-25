package com.drewel.drewel.rxbus

import io.reactivex.subjects.PublishSubject

class NotificationRxJavaBus  {

     val notificationPublishSubject: PublishSubject<String> = PublishSubject.create<String>()

    companion object {

        private var mInstance: NotificationRxJavaBus? = null

            fun getInstance() : NotificationRxJavaBus {
                if (mInstance == null) {
                    mInstance = NotificationRxJavaBus()
                }
                return mInstance as NotificationRxJavaBus
            }
    }
}