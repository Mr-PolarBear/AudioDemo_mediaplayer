package com.jack.ma.audiodemo.util

import android.media.MediaPlayer
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.jack.ma.audiodemo.AudioBean
import com.jack.ma.audiodemo.R
import com.jack.ma.audiodemo.base.BaseActivity
import com.jack.ma.audiodemo.view.FrequencyView

/**
 * Class for:播放音乐帮助
 * Created by   jack.马
 * Date: 2019/5/29
 * Time: 0:25
 */
class Helper_PlayAudio {

    companion object {


        var mDialog: AlertDialog? = null

        private var mPlayingView: ImageView? = null//当前正在播放的那一个播放按钮
        private var mAnimview: FrequencyView? = null//当前正在播放的那一个动画
        private var mSeekBar: SeekBar? = null//当前正在播放的进度条
        private var mTvLeftTime: TextView? = null//当前正在播放的时间
        private var mTvRightTime: TextView? = null//当前音频的总时间

        var mIsManual = false//是否手动拖动

        var mPlayingId: Int = 0//当前正在播放的id

        /**
         * 1.  初始化音频播放
         * @param mAudioUtil //播放工具
         * @param activity  //可以是fragment或者activity
         * @param item  //实体类
         * @param isPlay //是否直接播放
         * @param path  //播放的路径   建议先下载完成之后
         * @param playview
         * @param bview//动画view
         * @param mTvDurationLeft
         * @param mTvDurationRight
         * @param mTvProbar
         * @param playListener
         */
        fun toPlayer(mAudioUtil: AudioUtil?, activity: BaseActivity?, item: AudioBean?, isPlay: Boolean,
                     playview: ImageView?, bview: FrequencyView, mTvDurationLeft: TextView?, mTvDurationRight: TextView?, mTvProbar: SeekBar?,
                     playListener: AudioUtil.onPlayListener?) {

            if (mAudioUtil == null) return

            val mPauseImgRes = R.mipmap.hearing_play

            //判断
            if (mPlayingId == item?.id) {
                if (mAudioUtil.isPlaying()) {
                    mAudioUtil.pause()
                    playview?.setImageResource(mPauseImgRes)
                    bview.stop()
                } else {
                    //播放
                    mAudioUtil.play(item.durationStamp, item.nowDurationStamp, playListener)
                }
                return
            }

            if (mDialog == null || mDialog?.context != activity) {
                mDialog = AlertDialog.Builder(activity!!).setMessage("请稍候..").setCancelable(false).create()
                mDialog?.setCanceledOnTouchOutside(false)
            }

            mDialog?.show()

            //如果是首次播放
            mPlayingView?.setImageResource(mPauseImgRes)
            mAnimview?.stop()

            mPlayingView = playview
            mSeekBar = mTvProbar
            mTvLeftTime = mTvDurationLeft
            mTvRightTime = mTvDurationRight
            mAnimview = bview
            mPlayingId = item?.id ?: 0
            mPlayingView?.isEnabled = false//开始操作则禁止按钮点击


            //1
            mAudioUtil.ready(activity, true, item?.url ?: "", object : AudioUtil.onReadyListener {

                //准备完毕
                override fun isReady(duration: Int, chinese: String, durationTime: String) {

                    mTvRightTime?.run { text = "/$durationTime" }
                    item?.duration = durationTime
                    item?.durationStamp = duration

                    //4 如果是直接播放  则播放  需要有方法可以判断activity或者fragment是否pause了
                    if (activity?.mIsPause == false && isPlay)
                        mAudioUtil.play(item?.durationStamp ?: 0,
                                item?.nowDurationStamp ?: 0,
                                object : AudioUtil.onPlayListener {
                                    override fun onPlay(isPlay: Boolean) {
                                        playListener?.onPlay(isPlay)
                                    }

                                })

                    onOver(mPlayingView)

                }

                //出现异常
                override fun onError() {
                    onOver(mPlayingView)

                }

                //播放结束
                override fun onPlayOver() {
                    //播放结束做什么
                    onOver(mPlayingView)
                }
            })


            //2 轮询回调
            mAudioUtil.setTimer(500, object : AudioUtil.onTimeListener {
                override fun onTimeSleep(mMediaPlayer: MediaPlayer?, progress: Int, showTime: String, showTimeStamp: Int) {

                    if (activity?.mIsPause == false && mMediaPlayer?.isPlaying()!!) {
                        mSeekBar?.progress = progress
                        item?.nowDuration = showTime
                        item?.nowDurationStamp = showTimeStamp
                        mTvLeftTime?.text = showTime
                    }

                    if (activity?.mIsPause == true) {
                        mAudioUtil.pause()
                        playListener?.onPlay(false)
                    }
                }

            })


            //3设置seekbar
            item?.let { initProBar(mAudioUtil, mSeekBar, it, mTvLeftTime) }


        }

        private fun onOver(view: ImageView?) {

            view?.isEnabled = true//开始操作则禁止按钮点击
            mDialog?.cancel()

        }


        // 滑动条
        fun initProBar(audioUtil: AudioUtil, seekBar: SeekBar?, audioBean: AudioBean, mTimeLeft: TextView?) {
            if (seekBar == null) return

            val id = AudioUtil.string2Int(seekBar.tag.toString())
            var mediap = audioUtil.mMediaPlayer

            //设置seekbar的拖动事件
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (mediap == null || seekBar == null || audioUtil.mIsError || !audioUtil.mIsReady) return
                    //数值改变
                    if (mIsManual && id == mPlayingId)
                        mediap.seekTo(mediap.duration / 100 * progress)
                    var msec = 0

                    if (id == mPlayingId) {
                        msec = mediap.currentPosition / 1000
                    } else {
                        msec = audioBean.durationStamp / 100 * progress
                    }
                    val strTime = AudioUtil.seconds2Chinese2(msec.toLong())// 00:12 中文时间
                    audioBean.nowDuration = strTime
                    audioBean.nowDurationStamp = msec

                    mTimeLeft?.text = strTime
                    Log.d("当前进度", progress.toString() + "中文" + strTime)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    if (mediap == null || seekBar == null || id != mPlayingId) return
                    mIsManual = true
                    //开始拖动

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (mediap == null || seekBar == null || id != mPlayingId) return
                    //停止拖动
                    mIsManual = false

                }
            })
        }
    }


}