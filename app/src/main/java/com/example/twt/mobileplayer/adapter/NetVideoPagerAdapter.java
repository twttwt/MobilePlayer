package com.example.twt.mobileplayer.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.domain.MediaItem;

import java.util.List;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class NetVideoPagerAdapter extends BaseAdapter {
    private static final String TAG = "NetVideoPagerAdapter";
    private Context mContext;
    private List<MediaItem> mediaItemList;
    public NetVideoPagerAdapter(List<MediaItem> mediaItemList, Context context) {
        this.mContext = context;
        this.mediaItemList = mediaItemList;
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
            convertView=View.inflate(mContext, R.layout.item_netvideo_pager,null);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_desc=(TextView)convertView.findViewById(R.id.tv_desc);
            holder.iv_icon=(ImageView)convertView.findViewById(R.id.iv_icon);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        MediaItem mediaItem = mediaItemList.get(position);
        holder.tv_name.setText(mediaItem.getDisplay_name());
        holder.tv_desc.setText(mediaItem.getSummary());
        Glide.with(mContext).load(Uri.parse(mediaItem.getImgUrl())).into(holder.iv_icon);


        return convertView;
    }
    static class ViewHolder{
        private TextView tv_name,tv_desc;
        private ImageView iv_icon;
    }
}
