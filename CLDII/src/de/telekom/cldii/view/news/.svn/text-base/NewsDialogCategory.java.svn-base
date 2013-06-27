package de.telekom.cldii.view.news;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.news.NewsCategory;
import de.telekom.cldii.view.news.adapter.NewsDialogIntervalAdapter;

/**
 * This view is designed to be filled into a alert dialog in order to create or
 * edit a news category
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class NewsDialogCategory extends LinearLayout {
    private View layout;
    private Context context;
    private IDataProviderManager dataProviderManager;
    private CategoryImageSelectListener categoryImageSelectListener;

    private Long categoryId;

    public NewsDialogCategory(Context context, IDataProviderManager dataProviderManager,
            CategoryImageSelectListener categoryImageSelectListener) {
        super(context);
        this.context = context;
        this.dataProviderManager = dataProviderManager;

        this.categoryImageSelectListener = categoryImageSelectListener;
        inflateLayout();
    }

    public NewsDialogCategory(Context context, IDataProviderManager dataProviderManager,
            CategoryImageSelectListener categoryImageSelectListener, Long categoryId) {
        super(context);
        this.context = context;
        this.dataProviderManager = dataProviderManager;
        this.categoryId = categoryId;
        this.categoryImageSelectListener = categoryImageSelectListener;
        inflateLayout();
        getCategoryProperties();
    }

    /* Private method to inflate the xml */
    private final void inflateLayout() {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.layout = inflater.inflate(R.layout.news_dialog_category, null, false);
        this.addView(layout);

        initListeners();
    }

    /* Private method to fill the form with existing data for editing mode */
    private void getCategoryProperties() {
        NewsCategory newsCategory = dataProviderManager.getNewsDataProvider().getNewsCategoryById(this.categoryId);

        ((EditText) layout.findViewById(R.id.categoryName)).setText(newsCategory.getText());
        ((NewsDialogIntervalAdapter) ((ListView) this.layout.findViewById(R.id.intervalList)).getAdapter())
                .setChecked(newsCategory.getUpdateInterval().ordinal());
        if (newsCategory.getImage() != null) {
            TextView iconTextView = (TextView) layout.findViewById(R.id.categoryIcon);
            iconTextView.setVisibility(View.GONE);
            layout.findViewById(R.id.categoryIconLayout).setBackgroundDrawable(
                    new BitmapDrawable(newsCategory.getImage()));
        }
    }

    private void initListeners() {
        View intervalLayout = this.layout.findViewById(R.id.intervalLayout);
        final ListView listView = (ListView) intervalLayout.findViewById(R.id.intervalList);
        layout.findViewById(R.id.categoryIconLayout).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                CharSequence[] categoryNames = { context.getString(R.string.dialog_news_source_predefined),
                        context.getString(R.string.dialog_news_source_gallery) };

                builder.setTitle(context.getString(R.string.dialog_news_imagesource));
                builder.setIcon(android.R.drawable.ic_menu_more);
                builder.setItems(categoryNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                        case 0:
                            categoryImageSelectListener.categoryImageSelectionRequested(true);
                            break;
                        case 1:
                            categoryImageSelectListener.categoryImageSelectionRequested(false);
                            break;
                        }
                    }
                });
                builder.create().show();
            }
        });

        listView.setAdapter(new NewsDialogIntervalAdapter(this.context));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ((NewsDialogIntervalAdapter) arg0.getAdapter()).setChecked(arg2);
            }
        });

    }

    /**
     * This interface connects an activity with a view of an alert dialog in
     * order to select a image from the gallery
     * 
     * @author Jun Chen, jambit GmbH
     * 
     */
    protected interface CategoryImageSelectListener {
        public void categoryImageSelectionRequested(boolean predefined);
    }
}
