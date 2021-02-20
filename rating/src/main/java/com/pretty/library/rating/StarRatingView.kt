package com.pretty.library.rating

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.InverseBindingMethod
import androidx.databinding.InverseBindingMethods
import com.prettyclown.widget.rating.OnStarProgressChange
import java.math.BigDecimal
import kotlin.math.roundToInt

@InverseBindingMethods(
    InverseBindingMethod(
        type = StarRatingView::class,
        attribute = "starProgress",
        event = "starProgressAttrChanged",
        method = "getStarProgress"
    )
)
class StarRatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    //Star 的大小
    private var starSize = 20f

    //Star间距
    private var starSpace = 10f

    //Star 步长
    private var starStepFull = true

    //Star总数
    private var starCount = 5
    private var starProgress = 0f
    private var starMinProgress = 0f
    private var emptyDrawable: Drawable? = null
    private var halfDrawable: Drawable? = null
    private var fillDrawable: Drawable? = null

    //可以点击
    private var clickEnable: Boolean = true

    //星星进度变化
    private var starProgressChange: OnStarProgressChange? = null

    init {
        orientation = HORIZONTAL
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StarRatingView)
        val starCount = typedArray.getInteger(R.styleable.StarRatingView_starCount, starCount)
        val starProgress = typedArray.getFloat(R.styleable.StarRatingView_starProgress, 0f)
        starMinProgress = typedArray.getFloat(R.styleable.StarRatingView_starMinProgress, 0f)
        starSize = typedArray.getDimension(R.styleable.StarRatingView_starSize, starSize)
        starSpace = typedArray.getDimension(R.styleable.StarRatingView_starSpace, starSpace)
        starStepFull = typedArray.getBoolean(R.styleable.StarRatingView_starStepFull, true)
        emptyDrawable = typedArray.getDrawable(R.styleable.StarRatingView_starEmpty)
        halfDrawable = typedArray.getDrawable(R.styleable.StarRatingView_starHalf)
        fillDrawable = typedArray.getDrawable(R.styleable.StarRatingView_starFill)
        clickEnable = typedArray.getBoolean(R.styleable.StarRatingView_starClickable, clickEnable)
        typedArray.recycle()
        setStarCount(starCount)
        setStarProgress(starProgress)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!clickEnable)
            return false
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val stars = calculateStarsValue(event.x)
                if (stars < starMinProgress)
                    setStarProgress(starMinProgress)
                else
                    setStarProgress(stars)
            }
        }
        return true
    }

    /**
     * 设置Star总数
     */
    fun setStarCount(count: Int) = apply {
        removeAllViews()
        for (i in 0 until count) {
            addView(ImageView(context).apply {
                id = i
                layoutParams = LayoutParams(starSize.roundToInt(), starSize.roundToInt()).apply {
                    setMargins(0, 0, starSpace.roundToInt(), 0)
                }
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.CENTER_CROP
                minimumWidth = 10
                minimumHeight = 10
                setImageDrawable(emptyDrawable)
            })
        }
        this.starCount = count
    }

    /**
     * 设置最小进度
     */
    fun setStarMinProgress(progress: Float) = apply {
        this.starMinProgress = progress
    }

    /**
     * 设置Star进度
     */
    fun setStarProgress(progress: Float) {
        if (starProgress == progress)
            return
        val realityProgress = when {
            progress <= 0 -> 0f
            progress >= starCount -> starCount.toFloat()
            else -> progress
        }
        this.starProgress = realityProgress

        // 浮点数的整数值
        val integer = realityProgress.toInt()
        val b1 = BigDecimal(realityProgress.toString())
        val b2 = BigDecimal(integer.toString())
        // 浮点数的小数值
        val decimals = b1.subtract(b2).toFloat()

        // 设置选中的星星
        for (i in 0 until integer)
            (getChildAt(i) as ImageView).setImageDrawable(fillDrawable)

        // 设置没有选中的星星
        for (i in integer until starCount)
            (getChildAt(i) as ImageView).setImageDrawable(emptyDrawable)

        // 小数点默认增加半颗星
        if (decimals > 0)
            (getChildAt(integer) as ImageView).setImageDrawable(if (starStepFull) fillDrawable else halfDrawable)

        starProgressChange?.onProgressChange(starProgress)
    }

    /**
     * 设置进度改变回调
     */
    fun onStarProgressChange(onChange: OnStarProgressChange?) = apply {
        this.starProgressChange = onChange
    }

    /**
     * 获取Star进度
     */
    fun getStarProgress() = starProgress


    private fun calculateStarsValue(x: Float): Float {
        val iOneStarWidth: Float = starSize + starSpace
        val value = x * 2 / iOneStarWidth
        return value.roundToInt().toFloat() / 2 + 0.5f
    }

    companion object {

        @JvmStatic
        @BindingAdapter(
            value = ["onStarProgressChange", "starProgressAttrChanged"],
            requireAll = false
        )
        fun setValueChangedListener(
            view: StarRatingView,
            valueChangedListener: OnStarProgressChange?,
            bindingListener: InverseBindingListener?
        ) {
            if (bindingListener == null)
                view.onStarProgressChange(valueChangedListener)
            else {
                view.onStarProgressChange { progress ->
                    valueChangedListener?.onProgressChange(progress)
                    bindingListener.onChange()
                }
            }
        }
    }
}