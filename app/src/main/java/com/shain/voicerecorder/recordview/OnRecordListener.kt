package com.shain.voicerecorder.recordview

/**
 * Created by Shain on 15/06/2018.
 */

interface OnRecordListener {
    fun onStart()
    fun onCancel()
    fun onFinish(recordTime: Long)
    fun onLessThanSecond()
}
