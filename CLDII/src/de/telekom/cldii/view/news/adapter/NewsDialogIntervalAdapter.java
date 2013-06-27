package de.telekom.cldii.view.news.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.telekom.cldii.R;

/**
 * An extension of a BaseAdapter for the news update interval ListView of the
 * add category dialog
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class NewsDialogIntervalAdapter extends BaseAdapter {
    private Context context;
    private String[] intervals;
    private int checked = 7;

    public NewsDialogIntervalAdapter(Context context) {
        super();
        this.context = context;
        this.intervals = this.context.getResources().getStringArray(R.array.dialog_news_updateintervals);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return intervals.length;
    }

    @Override
    public Object getItem(int position) {
        return intervals[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TextView textView = (TextView) LayoutInflater.from(context).inflate(
                R.layout.news_dialog_category_listentry, null);
        textView.setText(intervals[position]);

        if (position == checked)
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox_checked, 0);
        else
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox, 0);
        return textView;
    }

    /**
     * Sets the index of the checked list item
     * 
     * @param position
     *            index of the list item
     */
    public void setChecked(int position) {
        checked = position;
        notifyDataSetChanged();
    }

    /**
     * Returns the index of the checked list item
     * 
     * @return
     */
    public int getChecked() {
        return this.checked;
    }

}
