package com.frank.androidmedia.controller

import android.R
import android.content.Context
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.util.Log
import android.util.Pair
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import com.frank.androidmedia.listener.AudioEffectCallback
import java.util.ArrayList

/**
 * AudioEffect: Equalizer、PresetReverb
 *
 * @author frank
 * @date 2022/3/23
 */
open class AudioEffectController(audioEffectCallback: AudioEffectCallback) {

    companion object {
        val TAG: String = AudioEffectController::class.java.simpleName
    }

    private var bands: Short = 0
    private var minEQLevel: Short = 0
    private var mEqualizer: Equalizer? = null

    private var mPresetReverb: PresetReverb? = null
    private val reverbValues = ArrayList<String>()
//    private var seekBarList: List<SeekBar>? = ArrayList()

    private var mAudioEffectCallback: AudioEffectCallback? = null

    private val presetReverb = arrayOf("None", "SmallRoom", "MediumRoom", "LargeRoom",
            "MediumHall", "LargeHall", "Plate")

    init {
        mAudioEffectCallback = audioEffectCallback
    }

    fun setupEqualizer(audioSessionId: Int) {
        val equalizerList = ArrayList<Pair<*, *>>()
        mEqualizer = Equalizer(0, audioSessionId)
        mEqualizer!!.enabled = true
        // band level: min and max
        minEQLevel = mEqualizer!!.bandLevelRange[0]//min level
        val maxEQLevel = mEqualizer!!.bandLevelRange[1]  // max level
        bands = mEqualizer!!.numberOfBands
        for (i in 0 until bands) {
            val centerFreq = (mEqualizer!!.getCenterFreq(i.toShort()) / 1000).toString() + " Hz"
            val pair = Pair.create(centerFreq, mEqualizer!!.getBandLevel(i.toShort()) - minEQLevel)
            equalizerList.add(pair)
        }
        mAudioEffectCallback?.setEqualizerList(maxEQLevel - minEQLevel, equalizerList)
    }

    fun setupPresetStyle(context: Context, spinnerStyle: Spinner) {
        for (i in 0 until mEqualizer!!.numberOfPresets) {
            reverbValues.add(mEqualizer!!.getPresetName(i.toShort()))
        }

        spinnerStyle.adapter = ArrayAdapter(context, R.layout.simple_spinner_item, reverbValues)
        spinnerStyle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {
                try {
                    mEqualizer!!.usePreset(arg2.toShort())
                    val seekBarList: List<SeekBar>? = mAudioEffectCallback?.getSeeBarList()
                    if (bands > 0 && seekBarList != null && mEqualizer != null) {
                        for (band in 0 until bands) {
                            seekBarList[band].progress = mEqualizer!!.getBandLevel(band.toShort()) - minEQLevel
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "preset style error=$e")
                }

            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }

    fun onEqualizerProgress(index: Int, progress: Int) {
        mEqualizer!!.setBandLevel(index.toShort(), (progress + minEQLevel).toShort())
    }

    fun release() {
        mEqualizer?.release()
        mPresetReverb?.release()
    }


}