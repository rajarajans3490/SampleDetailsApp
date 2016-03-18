package com.android.test.sampledetailsapp;

/**
 * This Adapter used to Customize the ListView Adapter
 */
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;

public class CustomListViewAdapter extends SimpleAdapter {

   Context context;
   List<HashMap<String, Object>> rows = null;
   private String[] mFrom = null;

    public CustomListViewAdapter(Context context, List<HashMap<String, Object>> data,
            int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.context = context;
        this.rows = data;
        this.mFrom = from;
    }

    @Override
	public int getCount() {
		return rows.size();
	}

	@Override
	public Object getItem(int position) {
		return rows.get(position);
	}

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder ;
        String mImageSrc ="";
        Bitmap mImagebm ;
        int mTextdescht = 0;

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_row, null);
            holder = new ViewHolder();
            holder.txtDesc = (TextView) convertView.findViewById(R.id.description);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        if(rows != null && rows.get(position).get(mFrom[2])!= null)
            mImageSrc = rows.get(position).get(mFrom[2]).toString();

        holder.txtTitle.setText(rows.get(position).get(mFrom[0]).toString());
        holder.txtDesc.setText(rows.get(position).get(mFrom[1]).toString());
        mTextdescht = holder.txtDesc.length();
        if(mImageSrc != "" && mTextdescht > 0) {
                mImagebm = BitmapFactory.decodeFile(mImageSrc);


                if(mImagebm !=null && mImagebm.getHeight() > 0 && mImagebm.getWidth() > 0) {
                    holder.imageView.setMaxHeight(120);
                    holder.imageView.setMaxWidth(160);
                    holder.imageView.setAdjustViewBounds(true);
                    holder.imageView.setImageBitmap(mImagebm);
               } else {
                    holder.imageView.setMaxHeight(0);
                    holder.imageView.setMaxWidth(0);
                    holder.imageView.setImageResource(R.drawable.empty);
                }

        }else {
                    holder.imageView.setMaxHeight(0);
                    holder.imageView.setMaxWidth(0);
                    holder.imageView.setImageResource(R.drawable.empty);
        }

        return convertView;
    }
}
