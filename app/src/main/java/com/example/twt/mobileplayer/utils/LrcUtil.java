package com.example.twt.mobileplayer.utils;

import com.example.twt.mobileplayer.domain.LrcContent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class LrcUtil {
    private static final String TAG = "LrcUtil";
    private List<LrcContent> mLrcContents;
    private LrcContent mLrcContent;
    public LrcUtil(){
        mLrcContents=new ArrayList<>();
        mLrcContent=new LrcContent();
    }
    /**
     * 读取歌词
     * @param path
     * @return
     */
    public void readFile(String path){

        try {
            File file=new File(path.replace(".mp3", ".lrc"));
            if (!file.exists()){
                return;
            }

            FileInputStream  fis = new FileInputStream(file);
            InputStreamReader reader=new InputStreamReader(fis);
            BufferedReader br=new BufferedReader(reader);


            String str="";
            while ((str=br.readLine())!=null){
                str=str.replace("[","");
                str=str.replace("]","@");

                String[] split = str.split("@");
                if (split.length>1){
                    mLrcContent.setLrcStr(split[1]);

                    int time = time2String(split[0]);
                    mLrcContent.setLrcTime(time);

                    mLrcContents.add(mLrcContent);

                    mLrcContent=new LrcContent();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    /**
     * 解析歌词时间
     * 歌词内容格式如下：
     * [00:02.32]陈奕迅
     * [00:03.43]好久不见
     * [00:05.22]歌词制作  王涛
     * @param timeStr
     * @return
     */
    public int time2String(String timeStr){
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "@");
        String[] split = timeStr.split("@");

        //分离出分、秒并转换为整型

            int minute = Integer.parseInt(split[0]);
            int second = Integer.parseInt(split[1]);
            int millisecond = Integer.parseInt(split[2]);



        //计算上一行与下一行的时间转换为毫秒数  
        int currentTime = (minute*60+second) * 1000 + millisecond * 10;
        return currentTime;
    }
    public List<LrcContent> getLrcList() {
        return mLrcContents;
    }
}
