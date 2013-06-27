package de.telekom.cldii.view.news;

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.news.NewsCategory;
import de.telekom.cldii.view.news.adapter.NewsDialogNewsFeedAdapter;
import de.telekom.cldii.view.news.adapter.NewsDialogNewsFeedItem;

public class NewsDialogNewsFeed extends LinearLayout {
    private final Context context;
    private View layout;
    private IDataProviderManager dataProviderManager;
    private List<NewsDialogNewsFeedItem> newsDialogItems;

    public NewsDialogNewsFeed(Context context, IDataProviderManager dataProviderManager) {
        super(context);
        this.context = context;
        this.dataProviderManager = dataProviderManager;
        this.newsDialogItems = new LinkedList<NewsDialogNewsFeedItem>();

        List<NewsCategory> newsCategories = dataProviderManager.getNewsDataProvider().getNewsCategoriesOrderedByName();
        for (NewsCategory category : newsCategories) {
            NewsDialogNewsFeedItem item = new NewsDialogNewsFeedItem(category, dataProviderManager
                    .getNewsDataProvider().getFeedsForCategoryId(category.getId()));
            newsDialogItems.add(item);
        }

        inflateLayout();
    }

    public NewsDialogNewsFeed(Context context, IDataProviderManager dataProviderManager, Long categoryId) {
        super(context);
        this.context = context;
        this.dataProviderManager = dataProviderManager;

        this.newsDialogItems = new LinkedList<NewsDialogNewsFeedItem>();

        NewsDialogNewsFeedItem item = new NewsDialogNewsFeedItem(dataProviderManager.getNewsDataProvider()
                .getNewsCategoryById(categoryId), dataProviderManager.getNewsDataProvider().getFeedsForCategoryId(
                categoryId));
        newsDialogItems.add(item);
        inflateLayout();
    }

    private final void inflateLayout() {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.layout = inflater.inflate(R.layout.news_dialog_feed, null, false);
        this.addView(layout);

        initListeners();
    }

    private void initListeners() {
        final ListView listView = (ListView) this.layout.findViewById(R.id.feedList);
        listView.setAdapter(new NewsDialogNewsFeedAdapter(context, dataProviderManager, newsDialogItems));
        listView.setEmptyView(findViewById(R.id.nonews));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ((NewsDialogNewsFeedAdapter) arg0.getAdapter()).onItemClick(arg2);
            }
        });
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> arg0, View arg1, final int arg2, final long arg3) {
                if (arg3 != -1) {
                    final CharSequence[] items = { context.getString(R.string.dialog_delete) };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            switch (item) {
                            case 0:
                                dataProviderManager.getNewsDataProvider().removeNewsFeed(arg3);
                                ((NewsDialogNewsFeedAdapter) arg0.getAdapter()).removeItem(arg2);
                                ((NewsDialogNewsFeedAdapter) arg0.getAdapter()).notifyDataSetChanged();
                                break;
                            }
                        }
                    });
                    builder.create().show();
                    return true;
                }
                return false;
            }
        });
    }
}
