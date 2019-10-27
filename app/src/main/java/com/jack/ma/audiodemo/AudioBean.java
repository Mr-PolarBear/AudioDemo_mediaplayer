package com.jack.ma.audiodemo;

/**
 * Class for:
 * Created by   jack.马
 * Date: 2019/10/26
 * Time: 17:58
 */
public class AudioBean {
    private int id;
    private String url;  //网络路径
    private String localPath; //本地路径
    private String duration="00:00";  //总时间中文  00:01
    private String nowDuration;//当前时间中文 00:01
    private int durationStamp;  //总时间时间戳  单位为秒
    private int nowDurationStamp;//当前播放进度的时间戳  单位秒

    public AudioBean() {

    }

    public AudioBean(int id, String url, String localPath, String duration, String nowDuration) {
        this.id = id;
        this.url = url;
        this.localPath = localPath;
        this.duration = duration;
        this.nowDuration = nowDuration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNowDurationStamp() {
        return nowDurationStamp;
    }

    public void setNowDurationStamp(int nowDurationStamp) {
        this.nowDurationStamp = nowDurationStamp;
    }

    public int getDurationStamp() {
        return durationStamp;
    }

    public void setDurationStamp(int durationStamp) {
        this.durationStamp = durationStamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNowDuration() {
        return nowDuration;
    }

    public void setNowDuration(String nowDuration) {
        this.nowDuration = nowDuration;
    }
}
