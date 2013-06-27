package de.telekom.cldii.view.news.adapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.news.NewsItem;
import de.telekom.cldii.widget.TextViewMultilineEllipse;

/**
 * An extension of a BaseAdapter for the news list ListView
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class NewsListAdapter extends BaseAdapter {
    private Context context;
    private long categoryId;
    private NewsItemLoader imageLoader;
    private LayoutInflater viewInflater;
    private IDataProviderManager dataProviderManager;
    private List<NewsItem> newsItemList;
    private List<NewsListAdapterItem> listData;
    private LruCache<Long, Bitmap> imageCache;

    public NewsListAdapter(Context context, IDataProviderManager dataProviderManager, long categoryId) {
        super();
        this.context = context;
        this.dataProviderManager = dataProviderManager;
        this.viewInflater = LayoutInflater.from(context);
        this.categoryId = categoryId;
        this.listData = new ArrayList<NewsListAdapterItem>();
        this.imageCache = new LruCache<Long, Bitmap>(ApplicationConstants.NEWS_BITMAP_ITEM_CACHESIZE);
        this.imageLoader = new NewsItemLoader(dataProviderManager, imageCache);

        populateList();
    }

    @Override
    public long getItemId(int position) {
        Object item = listData.get(position).getData();
        if (item instanceof Integer)
            return newsItemList.get((Integer) item).getNewsId();
        else
            return -1;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position).getData();
    }

    @Override
    public int getViewTypeCount() {
        return ListItemTypes.values().length;
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItemViewType(position) == ListItemTypes.ENTRY.ordinal());
    }

    @Override
    public int getItemViewType(int position) {
        return listData.get(position).getItemType().ordinal();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        NewsListAdapterItem item = listData.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = viewInflater.inflate(R.layout.news_list_entry, null);

            viewHolder.categorySeperator = (LinearLayout) convertView.findViewById(R.id.seperatorCategory);
            viewHolder.categoryTextView = (TextView) convertView.findViewById(R.id.categoryTextView);
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.newsDate);
            viewHolder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.newsThumbnail);
            viewHolder.titleTextView = (TextViewMultilineEllipse) convertView.findViewById(R.id.newsTitle);
            viewHolder.feedNameTextView = (TextView) convertView.findViewById(R.id.newsFeed);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.feedNameTextView.setText(null);
        viewHolder.titleTextView.setText(null);
        viewHolder.thumbnailImageView.setBackgroundDrawable(null);
        viewHolder.thumbnailImageView.setImageDrawable(null);
        viewHolder.categoryTextView.setText(null);
        viewHolder.dateTextView.setText(null);

        switch (item.getItemType()) {
        case ENTRY:
            viewHolder.categorySeperator.setVisibility(View.GONE);
            viewHolder.categoryTextView.setVisibility(View.GONE);
            viewHolder.dateTextView.setVisibility(View.GONE);
            convertView.findViewById(R.id.newsListEntry).setVisibility(View.VISIBLE);
            if (position > 0 && listData.get(position - 1).getItemType() == ListItemTypes.ENTRY)
                convertView.findViewById(R.id.newsListDivider).setVisibility(View.VISIBLE);
            else
                convertView.findViewById(R.id.newsListDivider).setVisibility(View.GONE);

            NewsItem newsItem = newsItemList.get((Integer) item.getData());
            viewHolder.feedNameTextView.setText(newsItem.getParentFeedName());

            if (viewHolder.titleTextView != null) {
                viewHolder.titleTextView.setText(newsItem.getTitle().length() > 0 ? newsItem.getTitle() : "No Title");

                if (newsItem.isRead()) {
                    android.content.res.TypedArray styled = context
                            .obtainStyledAttributes(new int[] { R.attr.list_text_old });
                    viewHolder.titleTextView.setTextColor(styled.getColor(0, 0));
                    viewHolder.titleTextView.setTypeface(Typeface.DEFAULT);
                    styled.recycle();
                } else {
                    android.content.res.TypedArray styled = context
                            .obtainStyledAttributes(new int[] { R.attr.list_text_new });
                    viewHolder.titleTextView.setTextColor(styled.getColor(0, 0));
                    styled.recycle();
                    viewHolder.titleTextView.setTypeface(Typeface.DEFAULT_BOLD);
                }

                if (imageCache.get((long) newsItem.getNewsId()) == null) {
                    imageLoader.displayImage(Long.valueOf(newsItem.getNewsId()),
                            convertView.findViewById(R.id.newsThumbnailBg));
                } else {
                    convertView.findViewById(R.id.newsThumbnailBg).setBackgroundResource(
                            R.drawable.d_background_thumbnail);
                    viewHolder.thumbnailImageView.setImageBitmap(imageCache.get((long) newsItem.getNewsId()));
                }
            }
            break;
        case CATEGORYNAME:
            viewHolder.categorySeperator.setVisibility(View.VISIBLE);
            viewHolder.categoryTextView.setVisibility(View.VISIBLE);
            viewHolder.dateTextView.setVisibility(View.GONE);
            convertView.findViewById(R.id.newsListDivider).setVisibility(View.GONE);
            convertView.findViewById(R.id.newsListEntry).setVisibility(View.GONE);

            viewHolder.categoryTextView.setText((String) listData.get(position).getData());
            break;
        case DATE:
            viewHolder.categorySeperator.setVisibility(View.GONE);
            viewHolder.categoryTextView.setVisibility(View.GONE);
            viewHolder.dateTextView.setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.newsListDivider).setVisibility(View.GONE);
            convertView.findViewById(R.id.newsListEntry).setVisibility(View.GONE);

            viewHolder.dateTextView.setText((String) listData.get(position).getData());
            break;
        }

        return convertView;
    }

    private void populateList() {
        if (!this.listData.isEmpty())
            this.listData.clear();
        this.newsItemList = dataProviderManager.getNewsDataProvider().getNewsItemsForNewsCategoryOrderedByDate(
                categoryId);
        if (newsItemList.size() > 0) {
            Collections.sort(newsItemList, new NewsDateComparable());

            for (int i = 0; i < newsItemList.size(); i++) {
                if (i == 0)
                    listData.add(new NewsListAdapterItem(ListItemTypes.CATEGORYNAME, dataProviderManager
                            .getNewsDataProvider().getNewsCategoryNameById(this.categoryId)));

                Calendar calendar = Calendar.getInstance();
                int day = 0;
                int month = 0;
                int year = 0;
                if (i > 0) {
                    calendar.setTime(newsItemList.get(i - 1).getCreationDate());
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                    month = calendar.get(Calendar.MONTH);
                    year = calendar.get(Calendar.YEAR);
                }

                calendar.setTime(newsItemList.get(i).getCreationDate());
                if (day != calendar.get(Calendar.DAY_OF_MONTH) || month != calendar.get(Calendar.MONTH)
                        || year != calendar.get(Calendar.YEAR))
                    listData.add(new NewsListAdapterItem(ListItemTypes.DATE, DateFormat
                            .getDateInstance(DateFormat.FULL).format(calendar.getTime())));

                listData.add(new NewsListAdapterItem(ListItemTypes.ENTRY, i));
            }
        }
    }

    public void refreshDataSet() {
        populateList();
        notifyDataSetChanged();
    }

    public List<NewsItem> getNewsItemList() {
        return newsItemList;
    }

    static class ViewHolder {
        LinearLayout categorySeperator;
        TextView categoryTextView;
        TextView dateTextView;

        ImageView thumbnailImageView;
        TextViewMultilineEllipse titleTextView;
        TextView feedNameTextView;
    }

    private class NewsDateComparable implements Comparator<NewsItem> {

        @Override
        public int compare(NewsItem newsItem1, NewsItem newsItem2) {
            return newsItem2.getCreationDate().compareTo(newsItem1.getCreationDate());
        }
    }

    private enum ListItemTypes {
        CATEGORYNAME, DATE, ENTRY
    }

    /**
     * An Object to that stores information for an {@link NewsListAdapter}
     * 
     * @author Jun Chen, jambit GmbH
     * 
     */
    private class NewsListAdapterItem {
        private final ListItemTypes itemType;

        private final Object data;

        public NewsListAdapterItem(ListItemTypes itemType, Object data) {
            this.itemType = itemType;
            this.data = data;
        }

        /**
         * Returns the list item type
         * 
         * @return list item type
         */
        public ListItemTypes getItemType() {
            return this.itemType;
        }

        /**
         * Returns the data of item type
         * 
         * @return data of the item type
         */
        public Object getData() {
            return this.data;
        }

    }
}
