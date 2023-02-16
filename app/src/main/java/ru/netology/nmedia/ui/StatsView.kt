package ru.netology.nmedia.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)
    private var fillingType = 0
    fun get(): Int {
        return fillingType
    }

    fun setFillingType(filType: Int) {
        fillingType = filType
        invalidate()
        requestLayout()
    }

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()
    private var angle = 0F
    private var startFrom = -90F
    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StatsView,
            0, 0
        ).apply {
            try {
                fillingType = getInteger(R.styleable.StatsView_fillingType, 0)
            } finally {
                recycle()
            }
        }
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            colors = resources.getIntArray(resId).toList()
        }
    }


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }
        setFillingType(2)
        drawFilling(canvas, fillingType)

        canvas.drawText(
            "%.2f%%".format(data.sum() * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint,
        )
    }

    private fun drawFilling(canvas: Canvas, fillingType: Int) {

        when (fillingType) {
            0 -> {
                // из четырех точек
                for ((index, datum) in data.withIndex()) {
                    angle = 360F * datum
                    paint.color = colors.getOrNull(index) ?: randomColor()
                    canvas.drawArc(oval, startFrom, angle * progress, false, paint)
                    startFrom += angle
                }
            }
            1 -> {
                //последовательно
                for ((index, datum) in data.withIndex()) {
                    angle = 360F * datum
                    paint.color = colors.getOrNull(index) ?: randomColor()
                    canvas.drawArc(oval, (startFrom + angle) * progress, angle, false, paint)
                    startFrom += angle
                }
            }
            2 -> {
                //в обе стороны
                startFrom = -45F
                for ((index, datum) in data.withIndex()) {
                    angle = 360F * datum
                    paint.color = colors.getOrNull(index) ?: randomColor()
                    canvas.drawArc(
                        oval,
                        startFrom - (angle * progress) / 2,
                        angle * progress,
                        false,
                        paint
                    )
                    startFrom += angle
                }
            }
        }
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 3500
            interpolator = DecelerateInterpolator()
        }.also {
            it.start()
        }
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}