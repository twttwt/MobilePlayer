package com.example.twt.mobileplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.domain.MediaItem;
import com.example.twt.mobileplayer.utils.ThumbnailUtil;
import com.example.twt.mobileplayer.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class VideoPagerAdapter extends BaseAdapter {
    private boolean isAudio;
    private static final String TAG = "VideoPagerAdapter";
    private Context mContext;
    private List<MediaItem> mediaItemList;
    private List<Bitmap> thumbnailList;
    /**
     * 是否是视频媒体
     */
    private Utils utils;
    public VideoPagerAdapter(List<MediaItem> mediaItemList, Context context,boolean isAudio) {
        this.mContext=context;
        this.mediaItemList=mediaItemList;
        utils=new Utils();
        this.isAudio=isAudio;
    }

    @Override
    public int getCount() {
        return mediaItemList.size();
    }

    @Override
    public MediaItem getItem(int position) {
        return mediaItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if (convertView==null){
            holder=new ViewHolder();
            convertView=View.inflate(mContext, R.layout.item_video_pager,null);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
           holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        MediaItem mediaItem = mediaItemList.get(position);
        holder.tv_name.setText(mediaItem.getDisplay_name());
        holder.tv_time.setText(utils.stringForTime((int) mediaItem.getDuration()));
        holder.tv_size.setText(Formatter.formatFileSize(mContext, mediaItem.getSize()));

            setThumbnail(holder.iv_icon, position);//设置缩略图
        //holder.iv_icon.setImageResource(R.drawable.actionbar_music_normal);
        return convertView;
    }
    /**
     * 设置缩略图
     *
     * @param imgView  需要设置缩略图的ImageView控件
     * @param position 该控件在列表中的位置
     */
    private void setThumbnail(ImageView imgView, int position) {
        //视频
        if (!isAudio) {
            if (thumbnailList == null) {
                thumbnailList = new ArrayList<>(mediaItemList.size());
            }
            Bitmap bmp;
            try {
                bmp = thumbnailList.get(position);
            } catch (IndexOutOfBoundsException e) {
                bmp = ThumbnailUtil.getVideoThumbnail(mediaItemList.get(position).getData(), ThumbnailUtil.MICRO_KIND);
                thumbnailList.add(position, bmp);
            }
            //获取视频缩略图后设置上去
            imgView.setImageBitmap(bmp);
        } else {
            //没有专辑封面则设置默认图片
            imgView.setImageResource(R.drawable.actionbar_music_normal);
        }
    }
    static class ViewHolder{
        private TextView tv_name,tv_time,tv_size;
        private ImageView iv_icon;
    }
}
