package com.os.drewel.rxbus

import io.reactivex.subjects.PublishSubject

class UnreadCountRxJavaBus  {

     val unreadCountRxJavaBus: PublishSubject<String> = PublishSubject.create<String>()

    companion object {

        private var mInstance: UnreadCountRxJavaBus? = null

            fun getInstance() : UnreadCountRxJavaBus {
                if (mInstance == null) {
                    mInstance = UnreadCountRxJavaBus()
                }
                return mInstance as UnreadCountRxJavaBus
            }
    }
}