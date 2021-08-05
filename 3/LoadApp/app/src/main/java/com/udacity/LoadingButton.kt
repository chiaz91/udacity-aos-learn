package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates


private const val PROGRESS_OFFSET = 20
private const val TEXT_SIZE_DEFAULT = 30f
class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var text: String
    private var progress = 0f
    private val valueAnimator = ValueAnimator()
    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Default) { property, oldValue, newValue ->
        if (oldValue==newValue) return@observable
        valueAnimator.clear()
        when(newValue) {
            ButtonState.Pressed -> {
                valueAnimator.startRippleEffect()
            }
            ButtonState.Loading -> {
                text = resources.getString(R.string.button_loading)
                valueAnimator.startProgress()
            }
            else -> {
                text = resources.getString(R.string.download)
            }
        }
        invalidate()
        updateDrawingBounds()
    }

    // dimension and sizing
    private var widthSize = 0
    private var heightSize = 0
    private var progressOffsetActual=0f

    // drawing and bounds related
    private var center = PointF()
    private val lastTouch = PointF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }
    private val rectView = Rect()
    private var rectText = Rect()
    private var rectLinearProgress = Rect()
    private var rectfArcProgress = RectF()

    // styling
    private var colorText = 0
    private val colorPrimary = resources.getColor(R.color.colorPrimary, context.theme)
    private val colorPrimaryDark = resources.getColor(R.color.colorPrimaryDark, context.theme)
    private val colorAccent = resources.getColor(R.color.colorAccent, context.theme)

    init {
        isClickable = true
        text = resources.getString(R.string.download)
        // reading from attribute
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            colorText = getColor(R.styleable.LoadingButton_android_textColor, Color.WHITE)
            paint.textSize = getDimension(R.styleable.LoadingButton_android_textSize, TEXT_SIZE_DEFAULT)
        }
    }

    fun setLoading(isLoading: Boolean){
        valueAnimator.clear()
        buttonState = if (isLoading) ButtonState.Loading else ButtonState.Default
        isEnabled = !isLoading
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        when (event.action){
            MotionEvent.ACTION_DOWN -> {
                lastTouch.apply {
                    x = event.x
                    y = event.y
                }
                buttonState = ButtonState.Pressed
            }
            MotionEvent.ACTION_UP -> buttonState = ButtonState.Default
        }
        return super.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h

        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        center.apply {
            x = w/2f
            y = h/2f
        }
        rectView.apply {
            left = paddingLeft
            top = paddingTop
            right = w - paddingLeft
            bottom = h - paddingBottom
        }
        rectLinearProgress.set(rectView)
        updateDrawingBounds()
    }

    private fun updateDrawingBounds(){
        val radius = 0.2f*heightSize
        paint.getTextBounds(text, 0, text.length, rectText)
        rectfArcProgress.set(-radius, -radius, radius, radius)
        progressOffsetActual = center.x + rectText.centerX() + radius + PROGRESS_OFFSET
    }

    // Drawing
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawOutline()
            when (buttonState){
                ButtonState.Loading -> {
                    drawLinearProgress()
                    drawArcProgress()
                }
                ButtonState.Pressed -> drawRipple()
                else -> {}
            }
            drawText(text)
        }
    }

    private fun Canvas.drawOutline(){
        paint.color = colorPrimary
        drawRect(rectView, paint)
    }

    private fun Canvas.drawLinearProgress(){
        paint.color = colorPrimaryDark
        drawRect(rectLinearProgress, paint)
    }

    private fun Canvas.drawArcProgress(){
        save()
        translate( progressOffsetActual , center.y)
        paint.color = colorAccent
        drawArc(rectfArcProgress, 0f, progress*360f, true, paint)
        restore()
    }

    private fun Canvas.drawRipple(){
        save()
        clipRect(rectView)
        translate( lastTouch.x , lastTouch.y)
        paint.color = colorAccent
        drawCircle(0f, 0f, progress*widthSize, paint)
        restore()
    }

    private fun Canvas.drawText(text: String){
        paint.color = colorText
        drawText(text, center.x, center.y-rectText.centerY().toFloat(), paint);
    }


    // Animation
    private fun ValueAnimator.startProgress(){
        clear()
        setFloatValues(0f, 1f)
        addUpdateListener {
            progress = animatedValue as Float
            rectLinearProgress.right = (rectView.right*progress).toInt()
            invalidate()
        }
        interpolator = AccelerateDecelerateInterpolator()
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        duration = 1500
        start()
    }

    // attempt to replicate ripple effect
    private fun ValueAnimator.startRippleEffect(){
        clear()
        setFloatValues(0f, 1f)
        addUpdateListener {
            progress = animatedValue as Float
            invalidate()
        }
        interpolator = AccelerateDecelerateInterpolator()
        repeatCount = 0
        duration = 500
        start()
    }

    private fun ValueAnimator.clear(){
        removeAllUpdateListeners()
        cancel()
    }

}