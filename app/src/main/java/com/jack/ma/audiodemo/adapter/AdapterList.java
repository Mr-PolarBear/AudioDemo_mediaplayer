package com.jack.ma.audiodemo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jack.ma.audiodemo.AudioBean;
import com.jack.ma.audiodemo.base.BaseActivity;
import com.jack.ma.audiodemo.util.AudioUtil;
import com.jack.ma.audiodemo.R;
import com.jack.ma.audiodemo.util.Helper_PlayAudio;
import com.jack.ma.audiodemo.view.FrequencyView;

import java.util.List;

/**
 * Class for:
 * Created by   jack.马
 * Date: 2019/10/26
 * Time: 17:42
 */
public class AdapterList extends RecyclerView.Adapter<AdapterList.ViewHoder> {


    private BaseActivity mActivity;
    private List<AudioBean> data;


    public AudioUtil mAudioUtil;

    public AdapterList(BaseActivity context, List<AudioBean> data) {
        this.mActivity = context;
        this.data = data;
        mAudioUtil = new AudioUtil(AudioUtil.Companion.getTYPE_ASSETS());
    }


    @NonNull
    @Override
    public ViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.item_list1, parent, false);
        return new ViewHoder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoder holder, int position) {

        AudioBean audioBean = data.get(position);

        //1.初始化 相关
        holder.time2.setText("/" + audioBean.getDuration());
        boolean inPlayInMe = Helper_PlayAudio.Companion.getMPlayingId() == audioBean.getId();//当前播放的是否是自己
        holder.setImage(inPlayInMe, mAudioUtil);

        holder.time1.setText(audioBean.getNowDuration());
        holder.seekbar.setProgress(audioBean.getNowDurationStamp() <= 0 ? 0 : audioBean.getNowDurationStamp() / audioBean.getDurationStamp() * 100);
        //作标识的方法之一   避免影响其他的精度条  必须有标识
        holder.seekbar.setTag(audioBean.getId());

        //2.播放与暂停的回调
        AudioUtil.onPlayListener listener = isPlay -> {
            holder.setImage2(isPlay);
        };

        //3.点击事件
        holder.playView.setOnClickListener(v -> {

            //4.初始化并播放
            Helper_PlayAudio.Companion.toPlayer(
                    mAudioUtil,
                    mActivity,
                    audioBean,
                    true,
                    holder.playView,
                    holder.bBview,
                    holder.time1,
                    holder.time2,
                    holder.seekbar,
                    listener
            );

        });


    }

    @Override
    public int getItemCount() {
        if (data == null)
            return 0;
        return data.size();
    }


    class ViewHoder extends RecyclerView.ViewHolder {


        public static final int PLAYING_IMG = R.mipmap.hearing_stop;//反着来  播放着显示暂停图标
        public static final int PAUSE_IMG = R.mipmap.hearing_play;//反着来   暂时中显示播放图标

        ImageView playView;
        SeekBar seekbar;
        FrequencyView bBview;
        TextView time1;
        TextView time2;


        ViewHoder(@NonNull View itemView) {
            super(itemView);
            playView = itemView.findViewById(R.id.iv_item_homework3_play);
            seekbar = itemView.findViewById(R.id.sb_item_homework3);
            bBview = itemView.findViewById(R.id.audio_item_homework3);
            time1 = itemView.findViewById(R.id.tv_item_homework3_time1);
            time2 = itemView.findViewById(R.id.tv_item_homework3_time2);
        }

        //根据当前播放是不是本条 再图标变换
        public void setImage(boolean isMe, AudioUtil util) {
            playView.setImageResource(PAUSE_IMG);
            if (isMe) {
                setImage2(util.isPlaying());
            } else
                setImage2(false);


        }

        //单纯图标变换
        private void setImage2(boolean isplayig) {
            if (isplayig) {
                playView.setImageResource(PLAYING_IMG);
                bBview.start(10);
            } else {
                playView.setImageResource(PAUSE_IMG);
                bBview.stop();
            }
        }
    }
}
