package de.telekom.cldii.view.news;

import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CATEGORYID;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.R;
import de.telekom.cldii.data.news.NewsCategory;
import de.telekom.cldii.service.UpdateInterval;
import de.telekom.cldii.service.newsupdate.NewsUpdateService;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelNewsCategories;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.news.NewsDialogCategory.CategoryImageSelectListener;
import de.telekom.cldii.view.news.adapter.NewsDialogIntervalAdapter;
import de.telekom.cldii.view.news.adapter.NewsGridViewPagerAdapter;
import de.telekom.cldii.view.util.NetworkCheck;
import de.telekom.cldii.widget.grid.GridViewPager;

/**
 * Activity for showing news categories in a paged grid.
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 */
public class NewsCategoriesActivity extends AbstractActivity implements CategoryImageSelectListener,
        OnItemClickListener {
    private final String TAG = "NewsCategoriesActivity";
    private GridViewPager gridViewPager;

    private View tempAlertDialogView;

    private final BroadcastReceiver newsCategoryUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ApplicationConstants.CATEGORY_UPDATED)) {
                if (intent.hasExtra(ApplicationConstants.EXTRAS_KEY_CATEGORYID)) {
                    long cId = intent.getLongExtra(ApplicationConstants.EXTRAS_KEY_CATEGORYID, -1L);
                    gridViewPager = (GridViewPager) findViewById(R.id.gridviewpager);
                    gridViewPager.increaseCounterForContentItemWithId(cId);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_categories);
        setTopBarName(getString(R.string.section_news));

        // notificate user if an internet connection is not available
        if (!NetworkCheck.isOnline(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_you_need_a_internet_connection).setCancelable(true)
                    .setPositiveButton(getString(R.string.confirm_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateGridViewPager();
        IntentFilter categoryUpdateIntentFilter = new IntentFilter(ApplicationConstants.CATEGORY_UPDATED);
        this.registerReceiver(newsCategoryUpdateReceiver, categoryUpdateIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(newsCategoryUpdateReceiver);
    }

    private void updateGridViewPager() {
        gridViewPager = (GridViewPager) findViewById(R.id.gridviewpager);
        if (!gridViewPager.hasAdapter()) {
            OnItemClickListener onItemClickListener = new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    Intent intent = new Intent(NewsCategoriesActivity.this, NewsListActivity.class);
                    intent.putExtra(EXTRAS_KEY_CATEGORYID, id);
                    startActivity(intent);
                }
            };

            OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, final long arg3) {
                    showItemLongClickMenu(arg3);
                    return true;
                }
            };

            gridViewPager.setAdapter(new NewsGridViewPagerAdapter(NewsCategoriesActivity.this,
                    getDataProviderManager(), onItemClickListener, onItemLongClickListener, this));
        } else {
            gridViewPager.notifyDataSetChanged();
        }
    }

    private void showItemLongClickMenu(final long categoryId) {

        final CharSequence[] items = { getString(R.string.dialog_news_edit_newsfeed),
                getString(R.string.dialog_news_categorysetting), getString(R.string.dialog_delete),
                getString(R.string.dialog_news_newcategory) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_options));
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String categoryName = getDataProviderManager().getNewsDataProvider()
                        .getNewsCategoryNameById(categoryId);
                switch (item) {
                case 0:
                    editFeeds(categoryId);
                    break;
                case 1:
                    editCategory(categoryId);
                    break;
                case 2:
                    deleteCategory(categoryId, categoryName);
                    break;
                case 3:
                    createNewCategory();
                    break;
                default:
                    showPrompt(getString(R.string.notavailable));
                }
            }
        });
        builder.create().show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionMenu = menu;

        // Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.news_categories, menu);
        setMenuBackground();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (item.getItemId()) {
        case R.id.refreshCategories:
            startService(NewsUpdateService.updateAllCategoriesIntent(this));
            break;
        case R.id.deleteCategory:
            final List<NewsCategory> categoryList = getDataProviderManager().getNewsDataProvider()
                    .getNewsCategoriesOrderedByName();
            CharSequence[] categoryNames = new CharSequence[categoryList.size()];
            for (int i = 0; i < categoryList.size(); i++) {
                categoryNames[i] = categoryList.get(i).getText();
            }

            builder.setTitle(getString(R.string.category));
            builder.setIcon(android.R.drawable.ic_menu_delete);
            builder.setItems(categoryNames, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    deleteCategory(categoryList.get(item).getId(), categoryList.get(item).getText());
                    updateGridViewPager();
                }
            });
            builder.create().show();
            break;
        case R.id.editCategory:
            final List<NewsCategory> editCategoryList = getDataProviderManager().getNewsDataProvider()
                    .getNewsCategoriesOrderedByName();
            CharSequence[] editCategoryNames = new CharSequence[editCategoryList.size()];
            for (int i = 0; i < editCategoryList.size(); i++) {
                editCategoryNames[i] = editCategoryList.get(i).getText();
            }

            builder.setTitle(getString(R.string.category));
            builder.setIcon(android.R.drawable.ic_menu_edit);
            builder.setItems(editCategoryNames, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    editCategory(editCategoryList.get(item).getId());
                }
            });

            builder.create().show();
            break;
        case R.id.newCategory:
            createNewCategory();
            break;
        case R.id.editFeeds:
            editFeeds(null);
            break;
        default:
            showPrompt(getString(R.string.notavailable));
        }
        return true;
    }

    private void createNewCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View categoryAddView = new NewsDialogCategory(this, getDataProviderManager(), this);
        builder.setTitle(getString(R.string.dialog_news_add_newscategory));
        builder.setIcon(android.R.drawable.ic_menu_add);
        builder.setView(categoryAddView);
        builder.setPositiveButton(R.string.confirm_ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = ((EditText) categoryAddView.findViewById(R.id.categoryName)).getText().toString();
                if (name.length() > 0) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_rss);
                    View iconView = categoryAddView.findViewById(R.id.categoryIconLayout);
                    UpdateInterval interval = UpdateInterval.values()[((NewsDialogIntervalAdapter) ((ListView) categoryAddView
                            .findViewById(R.id.intervalList)).getAdapter()).getChecked()];
                    if (iconView.getBackground() != null && !(iconView.getBackground() instanceof ColorDrawable)) {
                        bitmap = ((BitmapDrawable) iconView.getBackground()).getBitmap();
                    }
                    Log.i(TAG, "Create category: Name: " + name + " | bitmap: " + bitmap + " | interval: " + interval);
                    getDataProviderManager().getNewsDataProvider().createNewCategory(name, interval, bitmap);
                    // release resource for gc
                    tempAlertDialogView = null;
                    updateGridViewPager();
                    dialog.dismiss();

                } else {
                    Log.v(TAG, "No Name -> No new Category");
                }

            }

        });
        builder.setNegativeButton(R.string.confirm_cancel, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // release resource for gc
                tempAlertDialogView = null;
                dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = builder.create();
        ((EditText) categoryAddView.findViewById(R.id.categoryName)).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0)
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
        this.tempAlertDialogView = categoryAddView;
    }

    private void editFeeds(Long categoryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View feedsEditView;
        if (categoryId != null)
            feedsEditView = new NewsDialogNewsFeed(this, getDataProviderManager(), categoryId);
        else
            feedsEditView = new NewsDialogNewsFeed(this, getDataProviderManager());
        builder.setTitle(getString(R.string.dialog_news_edit_newsfeed));
        builder.setIcon(android.R.drawable.ic_menu_edit);
        builder.setView(feedsEditView);
        builder.setPositiveButton(R.string.confirm_ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // release resource for gc
                tempAlertDialogView = null;
                updateGridViewPager();
            }
        });
        this.tempAlertDialogView = feedsEditView;
        builder.create().show();
    }

    private void editCategory(final Long categoryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View categoryEditView = new NewsDialogCategory(NewsCategoriesActivity.this, getDataProviderManager(),
                this, categoryId);
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
                    Log.i(TAG, "Update category: Name: " + name + " | bitmap: " + bitmap + " | interval: " + interval);
                    getDataProviderManager().getNewsDataProvider().updateNewsCategory(categoryId, name, interval,
                            bitmap);
                    // release resource for gc
                    tempAlertDialogView = null;
                    dialog.dismiss();
                    updateGridViewPager();
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
    }

    /* Private method to delete a category */
    private void deleteCategory(final long categoryId, final String categoryName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getString(R.string.confirm_delete_category), categoryName))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDataProviderManager().getNewsDataProvider().deleteCategory(categoryId);
                        updateGridViewPager();
                    }
                }).setNegativeButton(getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();

    }

    @Override
    public StateModel getStateModel() {
        return new StateModelNewsCategories(NewsCategoriesActivity.this, getDataProviderManager());
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

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        createNewCategory();

    }
}