package de.telekom.cldii.view.news;

import static de.telekom.cldii.ApplicationConstants.CATEGORY_UPDATED;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_BROADCASTID;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_BROADCASTINFO;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CATEGORYID;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CATEGORYNAME;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_NEWSID;
import static de.telekom.cldii.ApplicationConstants.IMAGE_LOADED;

import java.io.FileNotFoundException;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.GestureLibraries;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.R;
import de.telekom.cldii.data.news.NewsItem;
import de.telekom.cldii.service.UpdateInterval;
import de.telekom.cldii.service.newsupdate.NewsUpdateService;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelNewsList;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.news.NewsDialogCategory.CategoryImageSelectListener;
import de.telekom.cldii.view.news.adapter.NewsDialogIntervalAdapter;
import de.telekom.cldii.view.news.adapter.NewsListAdapter;

/**
 * News list activity to display news items of a category
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class NewsListActivity extends AbstractActivity implements CategoryImageSelectListener {
    private static final String TAG = "NewsListActivity";
    private ListView listView;
    private long categoryId;
    private String categoryName;

    private View tempAlertDialogView;

    // flag true when receiver is registered
    private boolean receiversRegistered = false;
    private boolean isInteracting = false;
    private boolean refreshPending = false;

    private final BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CATEGORY_UPDATED)) {
                if (intent.hasExtra(EXTRAS_KEY_CATEGORYID)) {
                    long cId = intent.getLongExtra(EXTRAS_KEY_CATEGORYID, -1L);
                    if (cId == categoryId) {
                        if (intent.hasExtra(EXTRAS_KEY_NEWSID)) {
                            populateListView(intent.getLongExtra(EXTRAS_KEY_NEWSID, -1L));
                        }
                        Log.v(TAG, "News updated and ListView refreshed!");
                    }
                }
            } else if (intent.getAction().equals(IMAGE_LOADED)) {
                if (intent.hasExtra(EXTRAS_KEY_BROADCASTID)) {
                    long cId = Long.valueOf(intent.getStringExtra(EXTRAS_KEY_BROADCASTID));
                    if (cId == categoryId) {
                        if (intent.hasExtra(EXTRAS_KEY_BROADCASTINFO)) {
                            Long nId = Long.valueOf(intent.getStringExtra(EXTRAS_KEY_BROADCASTINFO));
                            if (nId != null) {
                                List<NewsItem> newsItemList = ((NewsListAdapter) listView.getAdapter())
                                        .getNewsItemList();
                                for (int i = 0; i < newsItemList.size(); i++) {
                                    if (newsItemList.get(i).getNewsId() == nId) {
                                        newsItemList.remove(i);
                                        break;
                                    }
                                }
                                populateListView(nId);
                                Log.v(TAG, "Image loaded and ListView refreshed!");
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list);
        setTopBarName(getString(R.string.section_news));

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRAS_KEY_CATEGORYID)) {
            long categoryId = extras.getLong(EXTRAS_KEY_CATEGORYID);
            this.categoryId = categoryId;
            this.categoryName = getDataProviderManager().getNewsDataProvider().getNewsCategoryNameById(categoryId);
        } else {
            Log.e(TAG, "CategoryId doesn't exist.");
            finish();
        }
    }

    protected void onResume() {
        // register receiver
        IntentFilter intentToReceiveFilter = new IntentFilter(CATEGORY_UPDATED);
        this.registerReceiver(intentReceiver, intentToReceiveFilter);
        intentToReceiveFilter = new IntentFilter(IMAGE_LOADED);
        this.registerReceiver(intentReceiver, intentToReceiveFilter);
        receiversRegistered = true;

        populateListView();

        // has to be here AFTER populateListView()... otherwise we get an nullpointer for the newslist in gesture mode
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionMenu = menu;

        // Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.news_list, menu);
        setMenuBackground();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (item.getItemId()) {
        case R.id.refreshCategories:
            startService(NewsUpdateService.updateCategoryWithIdIntent(this, this.categoryId));
            break;
        case R.id.deleteCategory:
            final long finalCategoryId = this.categoryId;
            builder.setMessage(String.format(getString(R.string.confirm_delete_category), this.categoryName))
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getDataProviderManager().getNewsDataProvider().deleteCategory(finalCategoryId);
                            finish();
                        }
                    }).setNegativeButton(getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
            break;
        case R.id.editCategory:
            final View categoryEditView = new NewsDialogCategory(this, getDataProviderManager(), this, categoryId);
            builder.setTitle(getString(R.string.dialog_news_edit_newscategory));
            builder.setIcon(android.R.drawable.ic_menu_edit);
            builder.setView(categoryEditView);
            builder.setPositiveButton(R.string.confirm_ok, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = ((EditText) categoryEditView.findViewById(R.id.categoryName)).getText().toString();
                    if (name.length() > 0) {
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_rss);
                        View iconView = categoryEditView.findViewById(R.id.categoryIconLayout);
                        UpdateInterval interval = UpdateInterval.values()[((NewsDialogIntervalAdapter) ((ListView) categoryEditView
                                .findViewById(R.id.intervalList)).getAdapter()).getChecked()];
                        if (iconView.getBackground() != null && !(iconView.getBackground() instanceof ColorDrawable)) {
                            bitmap = ((BitmapDrawable) iconView.getBackground()).getBitmap();
                        }
                        getDataProviderManager().getNewsDataProvider().updateNewsCategory(categoryId, name, interval,
                                bitmap);
                        // release resource for gc
                        tempAlertDialogView = null;
                        dialog.dismiss();
                    } else {
                        Log.v(TAG, "No Name -> No new Category");
                    }

                }

            });
            builder.setNegativeButton(R.string.confirm_cancel, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            this.tempAlertDialogView = categoryEditView;
            builder.create().show();
            break;
        case R.id.editFeeds:
            View feedEditView = new NewsDialogNewsFeed(this, getDataProviderManager(), categoryId);
            builder.setTitle(getString(R.string.dialog_news_edit_newsfeed));
            builder.setIcon(android.R.drawable.ic_menu_edit);
            builder.setView(feedEditView);
            builder.setPositiveButton(getString(R.string.confirm_ok), new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    populateListView();
                }
            });
            builder.create().show();
            break;
        default:
            showPrompt(getString(R.string.notavailable));
        }
        return true;
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

    private void populateListView() {
        populateListView(null);
    }

    private void populateListView(Long newsId) {
        if (listView == null || listView.getAdapter() == null) {
            this.listView = (ListView) findViewById(R.id.listofnews);
            findViewById(R.id.topBarName).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listView.setSelection(0);
                }
            });

            NewsListAdapter adapter = new NewsListAdapter(this, getDataProviderManager(), this.categoryId);
            listView.setAdapter(adapter);
            listView.setEmptyView((TextView) findViewById(R.id.nonews));

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    Intent newsDetails = new Intent(NewsListActivity.this, NewsDetailsActivity.class);
                    if (arg3 != -1) {
                        newsDetails.putExtra(EXTRAS_KEY_NEWSID, Long.valueOf(arg3));
                        newsDetails.putExtra(EXTRAS_KEY_CATEGORYID, categoryId);
                        newsDetails.putExtra(EXTRAS_KEY_CATEGORYNAME, categoryName);
                        startActivity(newsDetails);
                    }
                }
            });
            listView.setOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        isInteracting = false;
                        if (refreshPending) {
                            ((NewsListAdapter) listView.getAdapter()).refreshDataSet();
                            refreshPending = false;
                        }
                    } else
                        isInteracting = true;
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                }
            });
        } else {
            List<NewsItem> newsItemList = ((NewsListAdapter) listView.getAdapter()).getNewsItemList();
            if (newsId != null) {
                NewsItem newsItem = getDataProviderManager().getNewsDataProvider().getNewsItemForNewsId(newsId);
                if (!newsItemList.contains(newsItem))
                    newsItemList.add(newsItem);
            }

            Log.d(TAG, "Got " + newsItemList.size() + " news.");

            if (!isInteracting)
                ((NewsListAdapter) listView.getAdapter()).refreshDataSet();
            else
                refreshPending = true;
        }

    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();

    }

    @Override
    public StateModel getStateModel() {
        return new StateModelNewsList(NewsListActivity.this, getDataProviderManager());
    }

    public long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public List<NewsItem> getNewsItemList() {
        return ((NewsListAdapter) listView.getAdapter()).getNewsItemList();
    }

    @Override
    public void categoryImageSelectionRequested(boolean predefined) {
        if (predefined) {
            Intent intent = new Intent(this, NewsPredefinedIconsGallery.class);
            startActivityForResult(intent, R.id.requestCode_predefinedGallery);
        } else {

            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, R.id.requestCode_photoGallery);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        View categoryIconLayout = tempAlertDialogView.findViewById(R.id.categoryIconLayout);
        switch (requestCode) {
        case R.id.requestCode_photoGallery:
            if (data != null) {
                Uri targetUri = data.getData();
                Bitmap bitmap;
                try {
                    // Scale icon/photo to the desired dimensions
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                            displayMetrics);
                    float dimension = ApplicationConstants.NEWSCATEGORY_ICON_DIMENSION * displayMetrics.density;

                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri), null, opts);
                    int height = opts.outHeight;
                    int width = opts.outWidth;
                    Log.i(TAG, "Image size: " + width + "x" + height);
                    
                    int inSampleSize = 1;
                    if (height > dimension || width > dimension) {
                        if (width > height) {
                            inSampleSize = Math.round((float) height / dimension);
                        } else {
                            inSampleSize = Math.round((float) width / dimension);
                        }
                    }
                    opts.inJustDecodeBounds = false;
                    opts.inSampleSize = inSampleSize;
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri), null, opts);
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                    if (width > dimension || height > dimension) {
                        // create a matrix for scaling
                        Matrix matrix = new Matrix();
                        matrix.postScale(dimension / width, dimension / height);

                        // recreate the new Bitmap
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                    }

                    categoryIconLayout.setBackgroundDrawable(new BitmapDrawable(bitmap));
                    categoryIconLayout.findViewById(R.id.categoryIcon).setVisibility(View.GONE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            break;
        case R.id.requestCode_predefinedGallery:
            if (data != null && data.getData() != null) {
                Drawable icon = null;
                try {
                    icon = Drawable.createFromStream(getContentResolver().openInputStream(data.getData()), null);
                    categoryIconLayout.setBackgroundDrawable(icon);
                    categoryIconLayout.findViewById(R.id.categoryIcon).setVisibility(View.GONE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            break;

        }

    }
}
