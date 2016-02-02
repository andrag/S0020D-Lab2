package com.example.anders.laboration2;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Anders on 2015-07-07.
 * This class is not used in the program.
 *
 * Kommentar 2015-10-28
 * Seems like this class only fetch images from resource folder
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    

    public Integer mThumbIds[] = {
            //R.drawable.thelostarc,
            R.drawable.rocky4, R.drawable.jackie,
            R.drawable.hobo6, R.drawable.per, R.drawable.gammal_tant
    };

    //Constructor
    public ImageAdapter(Context c){
        mContext = c;
    }


    @Override
    public int getCount() {
        return mThumbIds.length;
    }

    @Override
    public Object getItem(int position) {
        return mThumbIds[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(mThumbIds[position]);//På något sätt hämta från en cursor här i framtiden
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(70,70));
        return imageView;

    }
}
