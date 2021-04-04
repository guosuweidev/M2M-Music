package com.m2mmusic.android.ui.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.m2mmusic.android.R

class RoundConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var mMaskPaint: Paint? = null
    private val mXfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    private var mMaskBitmap: Bitmap? = null
    private val mLayerPaint = Paint()
    private var mRadius = 0

    init {
        init(context)
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.RoundConstraintLayout
        )
        mRadius = a.getDimensionPixelSize(
            R.styleable.RoundConstraintLayout_round_corner,
            4
        )
        a.recycle()
    }

    private fun init(context: Context) {
        setWillNotDraw(false)
        mMaskPaint = Paint()
        mMaskPaint!!.setAntiAlias(true)
        mMaskPaint!!.setFilterBitmap(true)
        mMaskPaint!!.setColor(ContextCompat.getColor(context, R.color.white))
    }

    private fun updateMask() {
        if (mMaskBitmap != null && !mMaskBitmap!!.isRecycled()) {
            mMaskBitmap!!.recycle()
            mMaskBitmap = null
        }
        try {
            mMaskBitmap = Bitmap.createBitmap(
                width, height,
                Bitmap.Config.ARGB_8888
            )
        } catch (e: Throwable) {
        }
        if (mMaskBitmap != null) {
            val canvas = Canvas(mMaskBitmap!!)
            canvas.drawRoundRect(
                RectF(
                    0F, 0F, mMaskBitmap!!.getWidth().toFloat(),
                    mMaskBitmap!!.getHeight().toFloat()
                ), mRadius.toFloat(), mRadius.toFloat(), mMaskPaint!!
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateMask()
    }

    override fun draw(canvas: Canvas) {
        if (mMaskBitmap != null) {
            val sc = canvas.saveLayer(
                0f, 0f, width.toFloat(), height.toFloat(),
                mLayerPaint, Canvas.ALL_SAVE_FLAG
            )
            super.draw(canvas)
            mMaskPaint!!.xfermode = mXfermode
            canvas.drawBitmap(mMaskBitmap!!, 0f, 0f, mMaskPaint)
            mMaskPaint!!.xfermode = null
            canvas.restoreToCount(sc)
        } else {
            super.draw(canvas)
        }
    }
}