package com.jack.ma.audiodemo.util

import android.app.Activity
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast


import java.io.IOException
import java.math.BigDecimal


/**
 * Class for:
 * Created by   jack.马
 * Date: 2019/5/28
 * Time: 16:28
 */
class AudioUtil {

    //资源准备监听
    interface onReadyListener {
        fun isReady(duration: Int, chinese: String, durationTime: String) //总时间 秒   5分钟    00:12

        fun onError()

        fun onPlayOver()//非循环播放的时候才需要使用到
    }

    //播放进度监听
    interface onTimeListener {
        fun onTimeSleep(mMediaPlayer: MediaPlayer?, progress: Int, showTime: String, showTimeStamp: Int)
    }


    //播放事件监听
    interface onPlayListener {
        fun onPlay(isPlay: Boolean)
    }

    //*************************变量开始**********************************

    var mIsError: Boolean = false
    var mIsReady: Boolean = false

    var mMediaPlayer: MediaPlayer? = null

    private var mHandler: Handler? = null
    private var mRunnable: Runnable? = null

    private var mActivity: Activity? = null

    var mOnPlayType = TYPE_URLorFILE

    private var mMillisecond = 500L


    //必须指定一种类型   1 文件或者url  2assets文件 3raw文件
    constructor(mOnPlayType: Int) {
        this.mOnPlayType = mOnPlayType
    }

    private constructor()

    fun setPlayType(int: Int) {
        mOnPlayType = int

    }

    //*************************流程开始**********************************

    //顺序1   准备并直接播放
    fun ready(context: Activity?, isLoop: Boolean, path: String, readyListener: onReadyListener) {
        mActivity = context

        if (mMediaPlayer != null) {
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
        }


        mMediaPlayer = MediaPlayer()
        mIsError = false
        mIsReady = false
        initMedia(isLoop, path, readyListener)
    }

    //顺序2 设置轮询
    fun setTimer(millisecond: Long, timer: onTimeListener?) {
        mMillisecond = millisecond
        mHandler?.removeCallbacks(mRunnable)
        mHandler = Handler()

        mRunnable = object : Runnable {
            override fun run() {

                if (mMediaPlayer == null || !mMediaPlayer?.isPlaying!! || mIsError || !mIsReady) {
                    Log.d("进入了什么都不执行", "是否播放中" + mMediaPlayer?.isPlaying.toString())
                    mHandler?.postDelayed(this, millisecond)
                    return
                }

                val nowtime = (mMediaPlayer?.currentPosition!! / 1000).toDouble()
                val allTime = (mMediaPlayer?.duration!! / 1000).toDouble()
                val pro = if (allTime.toInt() == 0) 0 else (nowtime / allTime * 100).toInt()
                val sss = seconds2Chinese2(nowtime.toLong())//00:12
                Log.d("播放进度", "当前播放时间 $nowtime 中文 $sss 进度 $pro 总时间 $allTime");
                timer?.onTimeSleep(mMediaPlayer, pro, sss, nowtime.toInt())

                mHandler?.postDelayed(this, millisecond)
            }

        }

        mHandler?.postDelayed(mRunnable, millisecond)

    }


    fun toastShort(string: String?) {
        Toast.makeText(mActivity, string, Toast.LENGTH_SHORT).show()
    }


    fun checkIsCanPlay(): Boolean {
        if (mIsError) {
            toastShort("录音加载失败")
            return false
        }
        if (!mIsReady) {
            toastShort("正在加载中,请稍候")
            return false
        }
        return true
    }

    //顺序4  真正播放
    fun play(allDurationon: Int, nowduration: Int, onPlayListener: onPlayListener?) {

        if (!mIsReady) {
            toastShort("正在加载中,请稍候")
            return
        }

        if (mIsError) {
            toastShort("录音加载失败")
            return
        }

        if (mMediaPlayer == null) {
            toastShort("播放器异常!")
            return
        }

        var isplay = !mMediaPlayer?.isPlaying!!

        if (isplay) {//下个动作是否是播放
            mMediaPlayer?.start()

            if (nowduration != 0 && nowduration != 0 && nowduration < allDurationon)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mMediaPlayer?.seekTo((nowduration * 1000).toLong(), MediaPlayer.SEEK_CLOSEST)
                } else {
                    mMediaPlayer?.seekTo(nowduration * 1000)
                }

        } else {
            mMediaPlayer?.pause()
        }
        onPlayListener?.onPlay(isplay)
    }


    fun isPlaying(): Boolean {
        return mMediaPlayer?.isPlaying ?: false
    }

    fun pause() {
        mMediaPlayer?.pause()
    }


    //顺序4
    fun resume() {
        mMediaPlayer?.start()
    }

    /**
     * 快进 /快退
     *
     * @param percentage 每次进度条移动的程度 总进度/percentage
     * @param isRight    是否是快进
     * @param mPlayView  播放或者暂停按钮
     * @param seekBar
     */
    fun tospeed(
            isRight: Boolean,
            percentage: Int,
            mPlayView: View?,
            seekBar: SeekBar,
            msgleft: String?,
            msgright: String?
    ) {
        if (mMediaPlayer == null) return

        if (!mMediaPlayer?.isPlaying!!) {

            if (mPlayView == null) return
            mPlayView.postDelayed({ mPlayView.performClick() }, 100)
            return
        }

        val duration = mMediaPlayer?.duration!!
        val de = mMediaPlayer?.currentPosition!!
        val a = duration.toFloat() / percentage
        val b = (if (isRight) de.plus(a) else de.minus(a)).toInt()
        val toGo = (b.toFloat() / duration * 100).toInt()

        mMediaPlayer?.seekTo(b)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seekBar.setProgress(toGo, true)
        } else {
            seekBar.progress = toGo

        }

        if (msgleft != null && !isRight)
            toastShort(msgleft)

        if (msgright != null && isRight)
            toastShort(msgright)
    }


    private fun initMedia(isLoop: Boolean, path: String, readyListener: onReadyListener?) {

        try {


            when (mOnPlayType) {
                TYPE_ASSETS -> {
                    val afd = mActivity?.getAssets()?.openFd(path)
                    mMediaPlayer?.setDataSource(afd?.fileDescriptor, afd?.startOffset
                            ?: 0, afd?.length ?: 0)
                }
                TYPE_RAW -> {
                    val rawPath = string2Int(path);
                    if (rawPath == 0) {
                        readyListener?.onError()
                        toastShort("转换raw失败")
                        return
                    }
                    mMediaPlayer = MediaPlayer.create(mActivity, string2Int(path))

                }
                TYPE_URLorFILE -> {
                    mMediaPlayer?.setDataSource(path)
                }
            }

            mMediaPlayer?.isLooping = isLoop


            //播放错误监听
            mMediaPlayer?.setOnErrorListener { mp, what, extra ->
                mIsError = true
                mActivity?.runOnUiThread(Runnable {
                    toastShort("录音加载失败")
                    readyListener?.onError()
                })
                false
            }

            //播放完成监听
            mMediaPlayer?.setOnCompletionListener {
                mActivity?.runOnUiThread(Runnable {
                    readyListener?.onPlayOver()
                })
            }


            //不是raw才缓冲准备
            if (mOnPlayType != TYPE_RAW) {
                mMediaPlayer?.prepareAsync()

                //准备完成监听
                mMediaPlayer?.setOnPreparedListener { mp ->
                    val Dura = (mMediaPlayer?.duration!! / 1000).toLong()
                    mIsReady = true
                    readyListener?.isReady(Dura.toInt(), seconds2Chinese(Dura), seconds2Chinese2(Dura))
                }
            } else {
                //否则直接播放
                val Dura = (mMediaPlayer?.duration!! / 1000).toLong()
                mIsReady = true
                readyListener?.isReady(Dura.toInt(), seconds2Chinese(Dura), seconds2Chinese2(Dura))
            }


        } catch (e: IOException) {
            e.printStackTrace()
            mMediaPlayer = null
            readyListener?.onError()
            toastShort("初始化失败!")
        }

    }

    @Synchronized
    fun onDestory() {
        if (mMediaPlayer != null) {
            mMediaPlayer?.release()
            mMediaPlayer = null
        }
        mIsReady = false
        mIsError = false
        mHandler?.removeCallbacks(mRunnable)

    }

    companion object {

        var TYPE_URLorFILE = 1
        var TYPE_ASSETS = 2
        var TYPE_RAW = 3

        /**
         * 将秒数转为中文 不是毫秒  例如  1秒  5分钟
         */
        fun seconds2Chinese(seconds: Long): String {
            return if (seconds <= 60) seconds.toString() + "秒" else toScaleDoubleStr(seconds / 60.0, 1, true, "分钟")
        }


        /**
         * 将秒数转为中文   例如 00:00  00:12;
         */
        fun seconds2Chinese2(seconds: Long): String {
            val minutes = (seconds / 60.0).toInt()
            val s = seconds % 60
            return (if (minutes < 10) "0$minutes" else minutes).toString() + ":" + if (s < 10) "0$s" else s
        }

        /**
         * 保留几位小数
         *
         * @param fl
         * @param scale
         * @param isRound
         * @param addStr  需要补上的字段
         * @return
         */
        fun toScaleDoubleStr(fl: Double, scale: Int, isRound: Boolean, addStr: String): String {
            return toScale(fl.toString(), scale, isRound).toDouble().toString() + addStr
        }

        /**
         * 保留几位小数
         *
         * @param real    值是浮点型值，以8字节IEEE浮点数存放 字符串
         * @param scale
         * @param isRound
         * @return
         */
        private fun toScale(real: String, scale: Int, isRound: Boolean): BigDecimal {
            return if (isRound)
                BigDecimal(real).setScale(scale, BigDecimal.ROUND_HALF_UP)
            else
                BigDecimal(real).setScale(scale)
        }


        fun string2Int(s: Any?): Int {

            return string2Double(s.toString()).toInt()
        }


        fun string2Double(s: String): Double {
            if (TextUtils.isEmpty(s))
                return 0.0

            try {
                return java.lang.Double.parseDouble(s)
            } catch (e: Exception) {
                return 0.0
            }

        }
    }


}
