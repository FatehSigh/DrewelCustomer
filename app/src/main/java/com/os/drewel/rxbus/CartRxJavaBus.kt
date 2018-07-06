package com.os.drewel.rxbus

import io.reactivex.subjects.PublishSubject

class CartRxJavaBus  {

     val cartPublishSubject: PublishSubject<String> = PublishSubject.create<String>()

    companion object {

        private var mInstance: CartRxJavaBus? = null

            fun getInstance() : CartRxJavaBus {
                if (mInstance == null) {
                    mInstance = CartRxJavaBus()
                }
                return mInstance as CartRxJavaBus
            }
    }
}