package de.telekom.cldii.view.news.adapter;

import java.util.ArrayList;
import java.util.List;

import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.news.NewsFeed;
import de.telekom.cldii.service.NewsRssUrlCheckTask;
import de.telekom.cldii.service.NewsRssUrlCheckTask.NewsRssUrlCheckTaskListener;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * An extension of a BaseAdapter for the {@link ListView} to edit the news feeds
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class NewsDialogNewsFeedAdapter extends BaseAdapter implements NewsRssUrlCheckTaskListener {
    private Context context;
    private final IDataProviderManager dataProviderManager;
    private LayoutInflater viewInflater;

    private List<NewsDialogNewsFeedAdapterItem> listData;

    private View tempAlertDialogView;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private int insertPosition;
    private String checkUrl;

    public NewsDialogNewsFeedAdapter(Context context, IDataProviderManager dataProviderManager,
            List<NewsDialogNewsFeedItem> newsFeedsItems) {
        super();
        this.context = context;
        this.dataProviderManager = dataProviderManager;
        this.viewInflater = LayoutInflater.from(context);
        this.listData = new ArrayList<NewsDialogNewsFeedAdapterItem>();

        /*
         * Builds the common data structure for the listview. Starting with
         * category name (viewType = 1)
         * 
         * Basic structure: START -> category name -> news feed -> ... -> news
         * feed -> addentry -> category name -> ... -> add entry -> END
         */
        for (NewsDialogNewsFeedItem item : newsFeedsItems) {
            // Feed count + 2 (category name and add item)
            listData.add(new NewsDialogNewsFeedAdapterItem(ListItemTypes.CATEGORYNAME, item.getCategory().getText()));
            for (NewsFeed feedItem : item.getNewsFeeds()) {
                listData.add(new NewsDialogNewsFeedAdapterItem(ListItemTypes.FEEDNAME, feedItem));
            }
            listData.add(new NewsDialogNewsFeedAdapterItem(ListItemTypes.ADDENTRY, item.getCategory().getId()));
        }

    }

    @Override
    public long getItemId(int position) {
        NewsDialogNewsFeedAdapterItem item = listData.get(position);
        if (item.getItemType() == ListItemTypes.FEEDNAME)
            return ((NewsFeed) item.getData()).getFeedId();
        return -1;
    }

    @Override
    public int getCount() {
        return this.listData.size();
    }

    @Override
    public Object getItem(int position) {
        return this.listData.get(position);
    }

    public void removeItem(int position) {
        this.listData.remove(position);
    }

    private void popupInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        final View feedAddView = viewInflater.inflate(R.layout.news_dialog_feed_dialoginput, null);

        builder.setTitle(this.context.getString(R.string.dialog_news_add_feed));
        builder.setIcon(android.R.drawable.ic_menu_add);
        builder.setView(feedAddView);
        builder.setPositiveButton(R.string.confirm_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                final EditText feedUrl = ((EditText) feedAddView.findViewById(R.id.feedUrl));

                if (feedUrl.getText().length() > 0) {
                    checkUrl = feedUrl.getText().toString();
                    new NewsRssUrlCheckTask(NewsDialogNewsFeedAdapter.this).execute(feedUrl.getText().toString());
                    progressDialog = ProgressDialog.show(context,
                            context.getString(R.string.dialog_news_urlcheck_title),
                            context.getString(R.string.dialog_news_urlcheck_text), true, true);
                }
            }
        });
        builder.setNegativeButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        this.alertDialog = builder.create();

        ((EditText) feedAddView.findViewById(R.id.feedUrl)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && URLUtil.isNetworkUrl(s.toString()))
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                else
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.tempAlertDialogView = feedAddView;

    }

    @Override
    public void taskFinished(NewsRssUrlCheckTask finishedTask, boolean result) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();

            // reopen the alertDialog when the input is incorrect
            if (!result) {
                EditText feedUrl = ((EditText) tempAlertDialogView.findViewById(R.id.feedUrl));
                feedUrl.setText(checkUrl);
                feedUrl.setSelection(feedUrl.getText().length());
                SpannableString errorMsg = new SpannableString(this.context.getString(R.string.dialog_news_add_feedurl_error));
                errorMsg.setSpan(new ForegroundColorSpan(Color.BLACK), 0, errorMsg.length(), 0);
                errorMsg.setSpan(new BackgroundColorSpan(Color.WHITE), 0, errorMsg.length(), 0);
                feedUrl.setError(errorMsg);
                this.alertDialog.show();
            } else {
                final EditText feedUrl = ((EditText) tempAlertDialogView.findViewById(R.id.feedUrl));
                long categoryId = (Long) listData.get(this.insertPosition).getData();
                Log.i("NewsDialogNewsFeedAdapter", feedUrl.getText().toString() + " added to category " + categoryId
                        + "!");
                long feedId = dataProviderManager.getNewsDataProvider().createNewNewsFeed(feedUrl.getText().toString(),
                        (Long) listData.get(this.insertPosition).getData());
                NewsFeed newsFeed = new NewsFeed();
                newsFeed.setFeedId((int) feedId);
                newsFeed.setIsActivated(true);
                newsFeed.setParentCategoryId(categoryId);
                newsFeed.setUrl(feedUrl.getText().toString());

                // Add the new item to the data structure
                listData.add(this.insertPosition, new NewsDialogNewsFeedAdapterItem(ListItemTypes.FEEDNAME, newsFeed));

                notifyDataSetChanged();
            }
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        NewsDialogNewsFeedAdapterItem item = listData.get(position);

        if (convertView == null) {
            // inflate layout when necessary
            holder = new ViewHolder();
            switch (item.getItemType()) {
            case CATEGORYNAME:
                convertView = viewInflater.inflate(R.layout.news_dialog_feed_listcategory, null);
                holder.view = (LinearLayout) convertView.findViewById(R.id.categoryNameLayout);
                break;
            case ADDENTRY:
                convertView = viewInflater.inflate(R.layout.news_dialog_feed_listadd, null);
                holder.view = (TextView) convertView.findViewById(R.id.addFeedListItem);
                break;
            case INPUTENTRY:
                convertView = viewInflater.inflate(R.layout.news_dialog_feed_listinput, null);
                holder.view = (RelativeLayout) convertView.findViewById(R.id.inputFeedLayout);
                break;
            default:
                convertView = viewInflater.inflate(R.layout.news_dialog_feed_listentry, null);
                holder.view = (TextView) convertView.findViewById(R.id.newsFeedName);
            }
            convertView.setTag(holder);
        } else {
            // reuse when existing
            holder = (ViewHolder) convertView.getTag();
        }

        String text = "";

        // Fill list view item with content and listeners
        switch (item.getItemType()) {
        case CATEGORYNAME:
            TextView categoryNameTextView = (TextView) ((LinearLayout) holder.view).findViewById(R.id.categoryName);
            categoryNameTextView.setText(((String) item.getData()));
            break;
        case INPUTENTRY:
            initInputEntryListeners(holder.view, position, (Long) item.getData());
            break;
        case ADDENTRY:
            text = this.context.getString(R.string.dialog_news_add_feed);
            ((TextView) holder.view).setText(text);
            break;
        default:
            TextView feedTextView = ((TextView) holder.view);
            if (((NewsFeed) item.getData()).isActivated())
                feedTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox_checked, 0);
            else
                feedTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.d_checkbox, 0);

            text = ((NewsFeed) item.getData()).getName();
            if (text == null || text.length() < 1)
                text = ((NewsFeed) item.getData()).getUrl();
            feedTextView.setText(text);
        }

        return convertView;
    }

    private void initInputEntryListeners(View layout, final int position, final Long dataReference) {
        layout.findViewById(R.id.buttonCancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                listData.set(position, new NewsDialogNewsFeedAdapterItem(ListItemTypes.ADDENTRY, listData.get(position)
                        .getData()));
                notifyDataSetChanged();
            }
        });

        final Button buttonOk = (Button) layout.findViewById(R.id.buttonOk);
        final EditText feedUrl = ((EditText) layout.findViewById(R.id.feedUrl));

        final String url = feedUrl.getText().toString();
        buttonOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i("NewsDialogNewsFeedAdapter", url + " added!");
                long categoryId = (Long) listData.get(position).getData();
                long feedId = dataProviderManager.getNewsDataProvider().createNewNewsFeed(url, categoryId);
                NewsFeed newsFeed = new NewsFeed();
                newsFeed.setFeedId((int) feedId);
                newsFeed.setIsActivated(true);
                newsFeed.setParentCategoryId(categoryId);
                newsFeed.setUrl(url);
                // Reset back to add entry
                listData.set(position, new NewsDialogNewsFeedAdapterItem(ListItemTypes.ADDENTRY, listData.get(position)
                        .getData()));
                // Add the new item to the data structure
                listData.add(position, new NewsDialogNewsFeedAdapterItem(ListItemTypes.FEEDNAME, newsFeed));

                notifyDataSetChanged();
            }
        });

        feedUrl.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    Log.i("NewsDialogNewsFeedAdapter", "Focused!");

            }
        });
        feedUrl.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            }
        });

        feedUrl.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0)
                    buttonOk.setEnabled(true);
                else
                    buttonOk.setEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });
        feedUrl.requestFocus();
    }

    public void onItemClick(int position) {
        NewsDialogNewsFeedAdapterItem item = listData.get(position);
        switch (item.getItemType()) {
        case CATEGORYNAME:
            break;
        case ADDENTRY:
            Log.v("FeedAdapt", "AddEntry of " + item.getData());
            // listData.set(position, new
            // NewsDialogNewsFeedAdapterItem(ListItemTypes.INPUTENTRY,
            // listData.get(position)
            // .getData()));
            // notifyDataSetChanged();
            this.insertPosition = position;
            popupInput();
            break;
        case INPUTENTRY:

            break;
        default:
            changeActivationStatus(position);
        }
    }

    private void changeActivationStatus(int position) {
        NewsFeed newsFeed = (NewsFeed) this.listData.get(position).getData();
        newsFeed.setIsActivated(!newsFeed.isActivated());
        dataProviderManager.getNewsDataProvider().changeNewsFeedActivationStatus(newsFeed.getFeedId(),
                newsFeed.isActivated());
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        NewsDialogNewsFeedAdapterItem item = listData.get(position);
        switch (item.getItemType()) {
        case CATEGORYNAME:
            return 1;
        case ADDENTRY:
            return 2;
        case INPUTENTRY:
            return 3;
        default:
            return 0;
        }
    }

    @Override
    public int getViewTypeCount() {
        return ListItemTypes.values().length;
    }

    static class ViewHolder {
        public View view;
    }

    private enum ListItemTypes {
        CATEGORYNAME, FEEDNAME, ADDENTRY, INPUTENTRY
    }

    /**
     * An Object to that stores information for an
     * {@link NewsDialogNewsFeedAdapter}
     * 
     * @author Jun Chen, jambit GmbH
     * 
     */
    private class NewsDialogNewsFeedAdapterItem {
        private final ListItemTypes itemType;

        private final Object data;

        public NewsDialogNewsFeedAdapterItem(ListItemTypes itemType, Object data) {
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
