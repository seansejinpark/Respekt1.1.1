package com.respekt.utils

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.VideoView


class VideoView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    VideoView(context, attrs, defStyleAttr) {
    private val mScaleType: Int
    private var mHorizontalAspectRatioThreshold = 0
    private var mVerticalAspectRatioThreshold = 0

    constructor(context: Context) : this(context, null, 0) {}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mScaleType == SCALE_TYPE_CENTER_CROP) {
            applyCenterCropMeasure(widthMeasureSpec, heightMeasureSpec)
        } else if (mScaleType == SCALE_TYPE_FILL) {
            applyFillMeasure(widthMeasureSpec, heightMeasureSpec)
        } // else default/no-op
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        if (mScaleType == SCALE_TYPE_CENTER_CROP) {
            applyCenterCropLayout(l, t, r, b)
        } else {
            super.layout(l, t, r, b)
        }
    }

    private fun applyCenterCropLayout(left: Int, top: Int, right: Int, bottom: Int) {
        super.layout(
            left + mHorizontalAspectRatioThreshold, top + mVerticalAspectRatioThreshold, right
                    + mHorizontalAspectRatioThreshold, bottom + mVerticalAspectRatioThreshold
        )
    }

    private fun applyCenterCropMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val videoWidth = measuredWidth
        val videoHeight = measuredHeight
        val viewWidth = View.getDefaultSize(0, widthMeasureSpec)
        val viewHeight = View.getDefaultSize(0, heightMeasureSpec)
        mHorizontalAspectRatioThreshold = 0
        mVerticalAspectRatioThreshold = 0
        if (videoWidth == viewWidth) {
            val newWidth = (videoWidth.toFloat() / videoHeight * viewHeight).toInt()
            setMeasuredDimension(newWidth, viewHeight)
            mHorizontalAspectRatioThreshold = -(newWidth - viewWidth) / 2
        } else {
            val newHeight = (videoHeight.toFloat() / videoWidth * viewWidth).toInt()
            setMeasuredDimension(viewWidth, newHeight)
            mVerticalAspectRatioThreshold = -(newHeight - viewHeight) / 2
        }
    }

    private fun applyFillMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.getDefaultSize(0, widthMeasureSpec)
        val height = View.getDefaultSize(0, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    companion object {
        private const val SCALE_TYPE_NORMAL = 0
        private const val SCALE_TYPE_CENTER_CROP = 1
        private const val SCALE_TYPE_FILL = 2
    }

    init {
        val attributes: TypedArray =
            context.theme
                .obtainStyledAttributes(attrs, com.respekt.R.styleable.IntroVideoView, 0, 0)
        mScaleType = attributes.getInt(
            com.respekt.R.styleable.IntroVideoView_scaleType,
            SCALE_TYPE_FILL
        )
    }
}