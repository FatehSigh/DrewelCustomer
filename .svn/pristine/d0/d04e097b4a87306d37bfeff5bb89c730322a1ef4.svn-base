package com.os.drewel.rxbus

import io.reactivex.subjects.PublishSubject

class SampleRxJavaBus  {

    public val objectPublishSubject = PublishSubject.create<Any>()


    companion object {

        private var mInstance: SampleRxJavaBus? = null

            fun getInstance() : SampleRxJavaBus {
                if (mInstance == null) {
                    mInstance = SampleRxJavaBus()
                }
                return mInstance as SampleRxJavaBus
            }
    }
}