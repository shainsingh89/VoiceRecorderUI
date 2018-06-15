package com.shain.voicerecorder.recordview

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.support.annotation.RequiresApi
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.shain.voicerecorder.R

import java.io.IOException

/**
 * Created by Shain on 15/06/2018.
 */

class RecordView : RelativeLayout {
    var smallBlinkingMic: ImageView? = null
    private var basketImg: ImageView? = null
    private var counterTime: Chronometer? = null
    var slideToCancel: TextView? = null
    private var slideToCancelLayout: LinearLayout? = null
    private var arrow: ImageView? = null


    private var initialX: Float = 0.toFloat()
    private var basketInitialY: Float = 0.toFloat()
    private var difX = 0f
    var cancelBounds = 130f
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var alphaAnimation1: AlphaAnimation? = null
    private var alphaAnimation2: AlphaAnimation? = null
    private var recordListener: OnRecordListener? = null
    private var onBasketAnimationEndListener: OnBasketAnimationEnd? = null
    private var animatedVectorDrawable: AnimatedVectorDrawableCompat? = null
    private var isSwiped: Boolean = false
    var isLessThanSecondAllowed = false
    private var isSoundEnabled = true
    private var RECORD_START = R.raw.record_start
    private var RECORD_FINISHED = R.raw.record_finished
    private var RECORD_ERROR = R.raw.record_error
    private var player: MediaPlayer? = null

    constructor(context: Context) : super(context) {
        init(context, null, -1, -1)
    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, -1, -1)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, -1)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }


    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val view = View.inflate(context, R.layout.record_view, null)
        addView(view)
        val viewGroup = view.parent as ViewGroup
        viewGroup.clipChildren = false

        slideToCancelLayout = view.findViewById(R.id.slide_to_cancel_layout)
        arrow = view.findViewById(R.id.arrow)
        slideToCancel = view.findViewById(R.id.slide_to_cancel)
        smallBlinkingMic = view.findViewById(R.id.glowing_mic)
        counterTime = view.findViewById(R.id.counter_tv)
        basketImg = view.findViewById(R.id.basket_img)

        hideViews()


        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView,
                    defStyleAttr, defStyleRes)


            val slideArrowResource = typedArray.getResourceId(R.styleable.RecordView_slide_to_cancel_arrow, -1)
            val slideToCancelText = typedArray.getString(R.styleable.RecordView_slide_to_cancel_text)
            val slideMarginRight = typedArray.getDimension(R.styleable.RecordView_slide_to_cancel_margin_right, 30f).toInt()


            if (slideArrowResource != -1) {
                val slideArrow = AppCompatResources.getDrawable(context, slideArrowResource)
                arrow!!.setImageDrawable(slideArrow)
            }

            if (slideToCancelText != null)
                slideToCancel!!.text = slideToCancelText

            setMarginRight(slideMarginRight, true)

            typedArray.recycle()
        }

        animatedVectorDrawable = AnimatedVectorDrawableCompat.create(context, R.drawable.basket_animated)

        setAllParentsClip(smallBlinkingMic!!, false)
    }


    private fun animateBasket() {

        val micAnimation = AnimatorInflater.loadAnimator(context, R.animator.delete_mic_animation) as AnimatorSet
        micAnimation.setTarget(smallBlinkingMic) // set the view you want to animate
        micAnimation.duration = 600
        micAnimation.start()

        val translateAnimation1 = TranslateAnimation(0f, 0f, basketInitialY, basketInitialY - 90)
        translateAnimation1.duration = 250

        val translateAnimation2 = TranslateAnimation(0f, 0f, basketInitialY - 130, basketInitialY)
        translateAnimation2.duration = 250

        basketImg!!.setImageDrawable(animatedVectorDrawable)

        Handler().postDelayed({
            basketImg!!.visibility = View.VISIBLE
            basketImg!!.startAnimation(translateAnimation1)
        }, 700)

        translateAnimation1.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                smallBlinkingMic!!.visibility = View.INVISIBLE

                animatedVectorDrawable!!.start()
                Handler().postDelayed({
                    basketImg!!.startAnimation(translateAnimation2)
                    clearAlphaAnimation()
                    basketImg!!.visibility = View.INVISIBLE
                }, 250)


            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })


        translateAnimation2.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                basketImg!!.visibility = View.INVISIBLE

                if (onBasketAnimationEndListener != null)
                    onBasketAnimationEndListener!!.onAnimationEnd()
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })


    }

    internal fun reset(view: View?) {
        val set = AnimatorSet()
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
        set.duration = 100
        set.playTogether(scaleY, scaleX)
        set.start()
    }

    private fun hideViews() {
        slideToCancelLayout!!.visibility = View.GONE
        smallBlinkingMic!!.visibility = View.GONE
        counterTime!!.visibility = View.GONE
    }

    private fun showViews() {
        slideToCancelLayout!!.visibility = View.VISIBLE
        smallBlinkingMic!!.visibility = View.VISIBLE
        counterTime!!.visibility = View.VISIBLE
    }


    private fun moveImageToBack(recordBtn: RecordButton) {

        val positionAnimator = ValueAnimator.ofFloat(recordBtn.x, initialX)

        positionAnimator.interpolator = AccelerateDecelerateInterpolator()
        positionAnimator.addUpdateListener { animation ->
            val x = animation.animatedValue as Float
            recordBtn.x = x
        }

        recordBtn.stopScale()
        positionAnimator.duration = 0
        positionAnimator.start()


        // if the move event was not called ,then the difX will still 0 and there is no need to move it back
        if (difX != 0f) {
            val x = initialX - difX
            slideToCancelLayout!!.animate()
                    .x(x)
                    .setDuration(0)
                    .start()
        }


    }

    private fun animateSmallMicAlpha() {


        alphaAnimation1 = AlphaAnimation(0.0f, 1.0f)
        alphaAnimation1!!.duration = 500


        alphaAnimation1!!.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(arg0: Animation) {
                smallBlinkingMic!!.startAnimation(alphaAnimation2)
            }

            override fun onAnimationRepeat(arg0: Animation) {}

            override fun onAnimationStart(arg0: Animation) {}

        })

        alphaAnimation2 = AlphaAnimation(1.0f, 0.0f)

        alphaAnimation2!!.duration = 500


        alphaAnimation2!!.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationEnd(arg0: Animation) {
                // start animation1 when animation2 ends (repeat)
                smallBlinkingMic!!.startAnimation(alphaAnimation1)
            }

            override fun onAnimationRepeat(arg0: Animation) {}

            override fun onAnimationStart(arg0: Animation) {}

        })

        smallBlinkingMic!!.startAnimation(alphaAnimation1)


    }

    private fun clearAlphaAnimation() {
        alphaAnimation1!!.cancel()
        alphaAnimation1!!.reset()
        alphaAnimation1!!.setAnimationListener(null)
        alphaAnimation2!!.cancel()
        alphaAnimation2!!.reset()
        alphaAnimation2!!.setAnimationListener(null)
        smallBlinkingMic!!.clearAnimation()
        smallBlinkingMic!!.visibility = View.GONE
    }


    private fun isLessThanOneSecond(time: Long): Boolean {
        return time <= 1000
    }


    private fun playSound(soundRes: Int) {

        if (isSoundEnabled) {
            if (soundRes == 0)
                return

            try {
                player = MediaPlayer()
                val afd = context!!.resources.openRawResourceFd(soundRes) ?: return
                player!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                player!!.prepare()
                player!!.start()
                player!!.setOnCompletionListener { mp -> mp.release() }
                player!!.isLooping = false
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }


     fun onActionDown(recordBtn: RecordButton, motionEvent: MotionEvent) {

        if (recordListener != null)
            recordListener!!.onStart()

        reset(smallBlinkingMic)
        recordBtn.startScale()
        initialX = recordBtn.x

        basketInitialY = basketImg!!.y + 90

        playSound(RECORD_START)

        showViews()
        animateSmallMicAlpha()
        counterTime!!.base = SystemClock.elapsedRealtime()
        startTime = System.currentTimeMillis()
        counterTime!!.start()
        isSwiped = false

    }

     fun onActionMove(recordBtn: RecordButton, motionEvent: MotionEvent) {


        if (!isSwiped) {


            //Swipe To Cancel
            if (slideToCancelLayout!!.x != 0f && slideToCancelLayout!!.x <= counterTime!!.x + cancelBounds) {
                hideViews()
                moveImageToBack(recordBtn)
                counterTime!!.stop()
                animateBasket()
                if (recordListener != null)
                    recordListener!!.onCancel()

                isSwiped = true


            } else {


                //if statement is to Prevent Swiping out of bounds
                if (motionEvent.rawX < initialX) {
                    recordBtn.animate()
                            .x(motionEvent.rawX)
                            .setDuration(0)
                            .start()


                    if (difX == 0f)
                        difX = initialX - slideToCancelLayout!!.x


                    slideToCancelLayout!!.animate()
                            .x(motionEvent.rawX - difX)
                            .setDuration(0)
                            .start()


                }


            }

        }
    }

     fun onActionUp(recordBtn: RecordButton) {

        elapsedTime = System.currentTimeMillis() - startTime

        if (!isLessThanSecondAllowed && isLessThanOneSecond(elapsedTime) && !isSwiped) {
            if (recordListener != null)
                recordListener!!.onLessThanSecond()

            playSound(RECORD_ERROR)


        } else {
            if (recordListener != null && !isSwiped)
                recordListener!!.onFinish(elapsedTime)

            if (!isSwiped)
                playSound(RECORD_FINISHED)

        }


        hideViews()


        if (!isSwiped)
            clearAlphaAnimation()


        moveImageToBack(recordBtn)
        counterTime!!.stop()


    }

    private fun dp(value: Float): Int {

        if (value == 0f) {
            return 0
        }
        val density = context!!.resources.displayMetrics.density

        return Math.ceil((density * value).toDouble()).toInt()
    }


    private fun setMarginRight(marginRight: Int, convertToDp: Boolean) {
        val layoutParams = slideToCancelLayout!!.layoutParams as RelativeLayout.LayoutParams
        if (convertToDp) {
            layoutParams.rightMargin = dp(marginRight.toFloat())
        } else
            layoutParams.rightMargin = marginRight

        slideToCancelLayout!!.layoutParams = layoutParams
    }


    fun setOnRecordListener(recrodListener: OnRecordListener) {
        this.recordListener = recrodListener
    }

    fun setOnBasketAnimationEndListener(onBasketAnimationEndListener: OnBasketAnimationEnd) {
        this.onBasketAnimationEndListener = onBasketAnimationEndListener
    }

    fun setSoundEnabled(isEnabled: Boolean) {
        isSoundEnabled = isEnabled
    }

   /* fun setLessThanSecondAllowed(isAllowed: Boolean) {
        isLessThanSecondAllowed = isAllowed
    }*/

    fun setSlideToCancelText(text: String) {
        slideToCancel!!.text = text
    }

    fun setSlideToCancelTextColor(color: Int) {
        slideToCancel!!.setTextColor(color)
    }

    fun setSmallMicColor(color: Int) {
        smallBlinkingMic!!.setColorFilter(color)
    }

    fun setSmallMicIcon(icon: Int) {
        smallBlinkingMic!!.setImageResource(icon)
    }

    fun setSlideMarginRight(marginRight: Int) {
        setMarginRight(marginRight, false)
    }

    fun setCancelToBound(value: Float){
        cancelBounds = value
    }

    fun setCustomSounds(startSound: Int, finishedSound: Int, errorSound: Int) {
        //0 means do not play sound
        RECORD_START = startSound
        RECORD_FINISHED = finishedSound
        RECORD_ERROR = errorSound
    }

    companion object {


        fun setAllParentsClip(view: View, enabled: Boolean) {
            var view1 = view
            while (view1.parent != null && view1.parent is ViewGroup) {
                val viewGroup = view1.parent as ViewGroup
                viewGroup.clipChildren = enabled
                viewGroup.clipToPadding = enabled
                view1 = viewGroup
            }
        }
    }


}


