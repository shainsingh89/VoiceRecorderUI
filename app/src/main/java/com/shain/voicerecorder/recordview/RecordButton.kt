package com.shain.voicerecorder.recordview

import android.content.Context
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.shain.voicerecorder.R

/**
 * Created by Shain on 15/06/2018.
 */

class RecordButton : android.support.v7.widget.AppCompatImageView, View.OnTouchListener, View.OnClickListener {

    private var scaleAnim: ScaleAnim? = null
    private var recordView: RecordView? = null
    var isListenForRecord : Boolean = true
    private var onRecordClickListener: OnRecordClickListener? = null


    fun setRecordView(recordView: RecordView) {
        this.recordView = recordView
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)


    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordButton)

            val imageResource = typedArray.getResourceId(R.styleable.RecordButton_src, -1)


            if (imageResource != -1) {
                setTheImageResource(imageResource)
            }

            typedArray.recycle()
        }


        scaleAnim = ScaleAnim(this)
        this.setOnTouchListener(this)
        this.setOnClickListener(this)


    }


    private fun setTheImageResource(imageResource: Int) {
        val image = AppCompatResources.getDrawable(context, imageResource)
        setImageDrawable(image)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (isListenForRecord) {
            when (event.action) {

                MotionEvent.ACTION_DOWN -> recordView!!.onActionDown(v as RecordButton, event)


                MotionEvent.ACTION_MOVE -> recordView!!.onActionMove(v as RecordButton, event)

                MotionEvent.ACTION_UP -> recordView!!.onActionUp(v as RecordButton)
            }

        }
        return isListenForRecord


    }


    fun startScale() {
        scaleAnim!!.start()
    }

    fun stopScale() {
        scaleAnim!!.stop()
    }

    fun setOnRecordClickListener(onRecordClickListener: OnRecordClickListener) {
        this.onRecordClickListener = onRecordClickListener
    }


    override fun onClick(v: View) {
        if (onRecordClickListener != null)
            onRecordClickListener!!.onClick(v)
    }
}

