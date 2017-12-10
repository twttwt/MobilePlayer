package com.example.twt.mobileplayer.domain;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class LrcContent {
    private String lrcStr;  //歌词内容
    private int lrcTime;  //歌词时间
    /**
     * 休眠时间或者高亮显示的时间
     */
    private long sleepTime;

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public String getLrcStr() {
        return lrcStr;
    }

    public void setLrcStr(String lrcStr) {
        this.lrcStr = lrcStr;
    }

    public int getLrcTime() {
        return lrcTime;
    }

    public void setLrcTime(int lrcTime) {
        this.lrcTime = lrcTime;
    }
}
