package com.drewel.drewel.utill

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.support.v7.widget.AppCompatImageView

import com.drewel.drewel.R

class TintableImageView : AppCompatImageView {

    private var tint: ColorStateList? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet, defStyle: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TintableImageView, defStyle, 0)
        tint = a.getColorStateList(R.styleable.TintableImageView_tintColorStateList)
        a.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (tint != null && tint!!.isStateful)
            updateTintColor()
    }

    private fun updateTintColor() {
        val color = tint!!.getColorForState(drawableState, 0)
        setColorFilter(color)
        invalidate()
    }

}