package de.telekom.cldii.view.news;

import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CATEGORYID;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CATEGORYNAME;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_NEWSID;
import static de.telekom.cldii.statemachine.StateMachineConstants.SECTION2NEWSLOOP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATEMACHINE_WILL_CHANGE_NEWSTATEID;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATEMACHINE_WILL_CHANGE_STATE;

import java.text.DateFormat;
import java.util.List;

import org.xml.sax.XMLReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.GestureLibraries;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.R;
import de.telekom.cldii.data.news.NewsItem;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelNewsDetails;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.news.adapter.NewsItemLoader;
import de.telekom.cldii.view.util.FlingGestureDetector;
import de.telekom.cldii.view.util.OnFlingListener;
import de.telekom.cldii.widget.DetailSeekBar;
import de.telekom.cldii.widget.DetailSeekBar.SeekBarAdapter;

/**
 * News detail activity to show more detailed information of a news item
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class NewsDetailsActivity extends AbstractActivity implements OnFlingListener, OnSeekBarChangeListener {
    private final String TAG = "NewsDetailsActivity";
    private String newsContent;
    private long newsId;
    private long incomingCategoryId;
    private int currentNewsIndex;
    private DetailSeekBar seekBar;
    private List<NewsItem> newsList;
    private Button prevButton;
    private Button nextButton;
    private boolean isTracking;
    private NewsItemLoader imageLoader;

    private GestureDetector flingDetector;
    private View.OnTouchListener gestureListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_details);
        setTopBarName(getString(R.string.section_news));

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRAS_KEY_NEWSID)) {
            this.newsId = extras.getLong(EXTRAS_KEY_NEWSID);

            if (extras.containsKey(EXTRAS_KEY_CATEGORYID)) {
                this.incomingCategoryId = extras.getLong(EXTRAS_KEY_CATEGORYID);
            }

            if (extras.containsKey(EXTRAS_KEY_CATEGORYNAME)) {
                ((TextView) findViewById(R.id.newsDetailCategoryTextView)).setText(extras
                        .getString(EXTRAS_KEY_CATEGORYNAME));
            }

            Log.d(TAG, "newsId: " + newsId);
        } else {
            Log.e(TAG, "newsId doesn't exist.");
            finish();
        }

        imageLoader = new NewsItemLoader(getDataProviderManager());
        newsList = getDataProviderManager().getNewsDataProvider().getNewsItemsForNewsCategoryOrderedByDate(
                incomingCategoryId);

        LinearLayout seekBarCombinedLayout = (LinearLayout) findViewById(R.id.seekBarCombinedLayout);
        LinearLayout seekBarLayout = (LinearLayout) seekBarCombinedLayout.findViewById(R.id.seekBarLayout);
        seekBar = (DetailSeekBar) seekBarLayout.findViewById(R.id.seekBar);
        seekBar.setAdapter(new SeekBarAdapter() {

            @Override
            public void onItemSelected(int position) {
                getDetailNewsItem(newsList.get(position).getNewsId());
                // Reset scroll position
                ((ScrollView) findViewById(R.id.newsScrollView)).scrollTo(0, 0);
            }

            @Override
            public String getItem(int position) {
                if (position < newsList.size()) {
                    return newsList.get(position).getTitle();
                } else
                    return "No News";
            }

            @Override
            public int getCount() {
                return newsList.size();
            }
        });
        seekBar.setOnSeekBarChangeListener(this);

        initButtonListeners();

        getDetailNewsItem(this.newsId);

        OnClickListener urlClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                NewsItem newsItem = newsList.get(currentNewsIndex);
                if (newsItem.getUrl() != null) {
                    Log.d(TAG, " Loading URL in Browser: " + newsItem.getUrl());
                    intent.setData(Uri.parse(newsList.get(currentNewsIndex).getUrl()));
                    startActivity(intent);
                }
            }
        };
        ((TextView) findViewById(R.id.titleTextView)).setOnClickListener(urlClickListener);
        ((TextView) findViewById(R.id.urlTextView)).setOnClickListener(urlClickListener);

        // Gesture detection
        flingDetector = new GestureDetector(new FlingGestureDetector(this));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return flingDetector.onTouchEvent(event);
            }
        };

        findViewById(R.id.newsScrollView).setOnTouchListener(gestureListener);
        findViewById(R.id.contentTextView).setOnTouchListener(gestureListener);
    }

    static TagHandler tagHandler = new Html.TagHandler() {
        boolean first = true;

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if (tag.equals("li")) {
                char lastChar = 0;
                if (output.length() > 0)
                    lastChar = output.charAt(output.length() - 1);
                if (first) {
                    if (lastChar == '\n')
                        output.append("\t¥  ");
                    else
                        output.append("\n\t¥  ");
                    first = false;
                } else {
                    first = true;
                }
            }
        }
    };

    ImageGetter imgGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = null;
            drawable = getDataProviderManager().getNewsDataProvider().getImageForUrl(newsId, source);
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
            return drawable;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // register receiver
        IntentFilter intentToReceiveFilter = new IntentFilter(STATEMACHINE_WILL_CHANGE_STATE);
        this.registerReceiver(intentReceiver, intentToReceiveFilter);
        intentToReceiveFilter = new IntentFilter(ApplicationConstants.IMAGE_LOADED);
        this.registerReceiver(intentReceiver, intentToReceiveFilter);
        receiversRegistered = true;

    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister receivers activity paused
        if (receiversRegistered) {
            unregisterReceiver(intentReceiver);
            receiversRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initButtonListeners() {
        prevButton = (Button) findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                prevButtonClicked();
            }
        });

        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButtonClicked();
            }
        });

    }

    public void prevButtonClicked() {
        if (currentNewsIndex > 0) {
            getDetailNewsItem(newsList.get(currentNewsIndex - 1).getNewsId());
        }
    }

    public void nextButtonClicked() {
        if (currentNewsIndex < newsList.size() - 1) {
            getDetailNewsItem(newsList.get(currentNewsIndex + 1).getNewsId());
        }
    }

    // flag true when receiver is registered
    private boolean receiversRegistered = false;

    private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(STATEMACHINE_WILL_CHANGE_STATE)) {
                if (intent.hasExtra(STATEMACHINE_WILL_CHANGE_NEWSTATEID)) {
                    String newStateId = intent.getStringExtra(STATEMACHINE_WILL_CHANGE_NEWSTATEID);
                    if (newStateId.contains(SECTION2NEWSLOOP) && !newStateId.equals("")) {
                        getDetailNewsItem(Long.valueOf(newStateId.substring(SECTION2NEWSLOOP.length())));
                    }
                }
            } else if (intent.getAction().equals(ApplicationConstants.IMAGE_LOADED)) {
                if (intent.hasExtra(ApplicationConstants.EXTRAS_KEY_BROADCASTID)) {
                    long nId = Long.valueOf(intent.getStringExtra(ApplicationConstants.EXTRAS_KEY_BROADCASTID));
                    if (nId == newsId) {
                        getDetailNewsItem(newsId);
                        Log.v(TAG, "Image loaded and News details refreshed!");
                    }
                }
            }
        }
    };

    /**
     * Fills the layout with news item data for a given newsId
     * 
     * @param newsId
     *            news identifier
     */
    public void getDetailNewsItem(long newsId) {
        this.newsId = newsId;

        for (int i = 0; i < newsList.size(); i++) {
            if (newsList.get(i).getNewsId() == newsId) {
                this.currentNewsIndex = i;

                if (!isTracking)
                    this.seekBar.setProgress((int) (2 * currentNewsIndex + 1) / 2);

                this.prevButton.setEnabled(true);
                this.nextButton.setEnabled(true);
                if (currentNewsIndex == 0) {
                    this.prevButton.setEnabled(false);
                } else if (currentNewsIndex == newsList.size() - 1) {
                    this.nextButton.setEnabled(false);
                }
                break;
            }
        }

        // mark news item as read
        getDataProviderManager().getNewsDataProvider().markIsRead(newsId);

        NewsItem newsItem = newsList.get(currentNewsIndex);
        String newsTime = "";
        try {
            newsTime = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(
                    newsItem.getCreationDate());

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        this.newsContent = newsItem.getContent();

        if (this.newsContent == null || this.newsContent.length() < 1)
            this.newsContent = newsItem.getSummary();

        imageLoader.displayImage(Long.valueOf(newsItem.getNewsId()), findViewById(R.id.newsThumbnailBg));

        Drawable thumbnail = newsItem.getImage();
        ((ImageView) findViewById(R.id.newsThumbnail)).setImageDrawable(thumbnail);
        ((TextView) findViewById(R.id.titleTextView)).setText(newsItem.getTitle());
        ((TextView) findViewById(R.id.timeTextView)).setText(newsTime);
        ((TextView) findViewById(R.id.feedTextView)).setText(newsItem.getParentFeedName());

        if (this.newsContent != null) {
            int i;
            if ((i = this.newsContent.indexOf("<div class=\"mf-", 0)) > -1) {
                this.newsContent = this.newsContent.substring(0, i);
            }
            TextView content = ((TextView) findViewById(R.id.contentTextView));
            content.setText(Html.fromHtml(this.newsContent, imgGetter, tagHandler));
            content.setMovementMethod(LinkMovementMethod.getInstance());
        }

    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();

    }

    @Override
    public StateModel getStateModel() {
        return new StateModelNewsDetails(NewsDetailsActivity.this, getDataProviderManager());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTracking = false;
    }

    public long getIncomingCategoryId() {
        return incomingCategoryId;
    }

    public List<NewsItem> getNewsList() {
        return newsList;
    }

    public int getCurrentNewsIndex() {
        return currentNewsIndex;
    }

    @Override
    public void onLeftFling() {
        nextButtonClicked();
    }

    @Override
    public void onRightFling() {
        prevButtonClicked();
    }

}
