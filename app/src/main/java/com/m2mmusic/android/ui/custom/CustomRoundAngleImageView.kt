package com.m2mmusic.android.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.m2mmusic.android.R
import com.m2mmusic.android.utils.LogUtil
import kotlin.math.max

class CustomRoundAngleImageView : AppCompatImageView {

    private var width: Float = 0.0F
    private var height: Float = 0.0F
    private var radius: Int = 0
    private val defaultRadius = 0
    private var leftTopRadius: Int = 0
    private var rightTopRadius: Int = 0
    private var leftBottomRadius: Int = 0
    private var rightBottomRadius: Int = 0

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    @SuppressLint("Recycle")
    private fun init(context: Context, attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CustomRoundAngleImageView)
        radius = array.getDimensionPixelOffset(
            R.styleable.CustomRoundAngleImageView_radius,
            defaultRadius
        )
        leftTopRadius = array.getDimensionPixelOffset(
            R.styleable.CustomRoundAngleImageView_left_top_radius,
            defaultRadius
        )
        rightTopRadius = array.getDimensionPixelOffset(
            R.styleable.CustomRoundAngleImageView_right_top_radius,
            defaultRadius
        )
        leftBottomRadius = array.getDimensionPixelOffset(
            R.styleable.CustomRoundAngleImageView_left_bottom_radius,
            defaultRadius
        )
        rightBottomRadius = array.getDimensionPixelOffset(
            R.styleable.CustomRoundAngleImageView_right_bottom_radius,
            defaultRadius
        )

        if (defaultRadius == leftTopRadius) {
            leftTopRadius = radius
        }
        if (defaultRadius == rightTopRadius) {
            rightTopRadius = radius
        }
        if (defaultRadius == leftBottomRadius) {
            leftBottomRadius = radius
        }
        if (defaultRadius == rightBottomRadius) {
            rightBottomRadius = radius
        }

        array.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        width = getWidth().toFloat()
        height = getHeight().toFloat()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        val maxLeft = max(leftTopRadius, leftBottomRadius)
        val maxRight = max(rightTopRadius, rightBottomRadius)
        val minWidth = maxLeft + maxRight
        val maxTop = max(leftTopRadius, rightBottomRadius)
        val maxBottom = max(leftBottomRadius, rightBottomRadius)
        val minHeight = maxTop + maxBottom
        if (width >= minWidth && height >= minHeight) {
            val path = Path().apply {
                moveTo(leftTopRadius.toFloat(), 0F)
                lineTo(width.toFloat() - rightTopRadius.toFloat(), 0F)
                quadTo(width, 0F, width, rightTopRadius.toFloat())
                lineTo(width, height - rightBottomRadius.toFloat())
                quadTo(width, height, width - rightBottomRadius.toFloat(), height)
                lineTo(leftBottomRadius.toFloat(), height)
                quadTo(0F, height, 0F, height - leftBottomRadius.toFloat())
                lineTo(0F, leftTopRadius.toFloat())
                quadTo(0F, 0F, leftTopRadius.toFloat(), 0F)
            }
            canvas?.clipPath(path)
        }
        super.onDraw(canvas)
        /*if (width >= 12 && height > 12) {
            val path =  Path();
            //四个圆角
            path.moveTo(24F, 0F);
            path.lineTo(width - 24, 0F);
            path.quadTo(width, 0F, width, 24F);
            path.lineTo(width, height - 24);
            path.quadTo(width, height, width - 24, height);
            path.lineTo(24F, height);
            path.quadTo(0F, height, 0F, height - 24);
            path.lineTo(0F, 24F);
            path.quadTo(0F, 0F, 24F, 0F);

            canvas?.clipPath(path);
        }
        super.onDraw(canvas);*/
    }
}