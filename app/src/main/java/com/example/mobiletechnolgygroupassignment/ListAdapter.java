package com.example.mobiletechnolgygroupassignment;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends BaseAdapter {

    private final List<SavedItem> itemList;
    private final LayoutInflater inflater;

    public ListAdapter(Context context, List<SavedItem> itemList) {
        this.itemList = itemList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itemList != null ? itemList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return itemList != null ? itemList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_saved, parent, false);

            holder = new ViewHolder();
            holder.ivImage = convertView.findViewById(R.id.display_image);
            holder.tvReader = convertView.findViewById(R.id.item_read);
            holder.tvText = convertView.findViewById(R.id.item_text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SavedItem item = (SavedItem) getItem(position);

        if (item != null) {
            holder.tvReader.setText(item.getReader());
            holder.tvText.setText(item.getText());

            if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
                try {
                    holder.ivImage.setImageURI(Uri.parse(item.getImageUri()));
                } catch (Exception e) {
                    setDefaultImage(holder.ivImage, item.getReader());
                }
            } else {
                setDefaultImage(holder.ivImage, item.getReader());
            }
        }

        return convertView;
    }

    private void setDefaultImage(ImageView imageView, String reader) {
        if (reader == null) {
            imageView.setImageResource(R.drawable.barcode);
            return;
        }

        if (reader.contains("Barcode")) {
            imageView.setImageResource(R.drawable.barcode);
        } else if (reader.contains("Content")) {
            imageView.setImageResource(R.drawable.content);
        } else if (reader.contains("Text")) {
            imageView.setImageResource(R.drawable.text);
        } else {
            imageView.setImageResource(R.drawable.barcode);
        }
    }

    private static class ViewHolder {
        ImageView ivImage;
        TextView tvReader;
        TextView tvText;
    }
}
