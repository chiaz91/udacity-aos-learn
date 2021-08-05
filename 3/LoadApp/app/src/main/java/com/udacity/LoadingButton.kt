package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates


private const val PROGRESS_OFFSET = 20
private const val TEXT_SIZE_DEFAULT = 30f
class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var text = ""
    private var progress = 0f
    private var widthSize = 0
    private var heightSize = 0
    private var center = PointF()
    private var radius = 0f
    private var progressOffsetActual=0f


    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener {
            progress = animatedValue as Float
            rectLinearProgress.right = (rectView.right*progress).toInt()
            invalidate()
        }
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE
        duration = 3000
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { property, oldValue, newValue ->
        when(newValue) {
            ButtonState.Loading -> {
                text = resources.getString(R.string.button_loading)
                valueAnimator.start()
            }

            else -> {
                text = resources.getString(R.string.download)
                valueAnimator.cancel()
            }
        }
        invalidate()
        updateDrawBounds()
    }

    // const values
    private val colorPrimary = resources.getColor(R.color.colorPrimary, context.theme)
    private val colorPrimaryDark = resources.getColor(R.color.colorPrimaryDark, context.theme)
    private val colorAccent = resources.getColor(R.color.colorAccent, context.theme)

    // canvas related
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = TEXT_SIZE_DEFAULT
    }
    private val rectView = Rect()
    private var rectText = Rect()
    private var rectLinearProgress = Rect()
    private var rectfArcProgress = RectF()


    init {
        isClickable = true
        text="TEST"
        updateDrawBounds()
        // TODO: reading from attribute
    }

    override fun performClick(): Boolean {
//        if (super.performClick()) return true
        buttonState = buttonState.next()

        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawOutline()
        if (buttonState == ButtonState.Loading){
            canvas.drawLinearProgress()
            canvas.drawArcProgress()
        }
        canvas.drawTextOnCenter( text)

        // for test only
        canvas.drawDebugGrid()
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
        translate( progressOffsetActual,0f)
        paint.color = colorAccent
        drawArc(rectfArcProgress, 0f, progress*360f, true, paint)
        restore()
    }

    private fun Canvas.drawTextOnCenter(text: String){
        paint.color = Color.WHITE
        drawText(text, center.x, center.y-rectText.centerY().toFloat(), paint);
    }

    private fun Canvas.drawDebugGrid(){
        drawLine(0f, center.y, widthSize.toFloat(), center.y , paint)
        drawLine(center.x, 0f, center.x, heightSize.toFloat(), paint)
    }

    private fun updateDrawBounds(){
        paint.getTextBounds(text, 0, text.length, rectText)
        rectfArcProgress.set(
            0f, center.y-radius,
            radius, center.y+radius
        )
        progressOffsetActual = center.x + rectText.centerX() + PROGRESS_OFFSET
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        center.apply {
            x = w/2f
            y = h/2f
        }
        radius = 0.2f*h
        rectView.apply {
            left = paddingLeft
            top = paddingTop
            right = widthSize - paddingLeft
            bottom = heightSize - paddingBottom
        }
        rectLinearProgress.set(rectView)
        updateDrawBounds()
        setMeasuredDimension(w, h)
    }

}