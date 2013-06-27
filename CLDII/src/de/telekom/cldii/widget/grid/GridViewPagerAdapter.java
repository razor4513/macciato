package de.telekom.cldii.widget.grid;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.IGridDataService;

/**
 * A {@link PagerAdapter} extension for the {@link GridViewPager}.
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 * @param <T>
 */
public abstract class GridViewPagerAdapter extends PagerAdapter {
    protected Context context;
    protected IDataProviderManager dataProviderManager;
    protected OnItemClickListener onItemClickListener;
    protected OnItemLongClickListener onItemLongClickListener;
    private int numberOfPages;
    private Map<Integer, GridView> pages;
    
    private static final String TAG = "GridViewPagerAdapter";

    /**
     * An adapter for the {@link GridViewPager} widget.<br>
     * <br>
     * 
     * Layout instructions: The param layoutForSinglePlate needs a layout
     * containing <br>
     * - a {@link TextView} with Id 'plateTextView' for showing the Plate text. <br>
     * - a {@link ImageView} with Id 'plateImageView' for showing the Plate
     * image. <br>
     * - a {@link TextView} with Id 'plateBubbleTextView' for showing an
     * additional information (for example count of something).<br>
     * <br>
     * 
     * Example:<br>
     * 
     * <pre>
     * &lt;?xml version="1.0" encoding="utf-8"?>
     * &lt;RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
     *     android:id="@+id/RelativeLayout1"
     *     android:layout_width="wrap_content"
     *     android:layout_height="wrap_content"
     *     android:orientation="vertical" >
     * 
     *     &lt;TextView
     *         android:id="@+id/plateTextView" ... />
     * 
     *     &lt;TextView
     *         android:id="@+id/plateBubbleTextView" ... />
     * 
     *     &lt;ImageView
     *         android:id="@+id/plateImageView" ... />
     * 
     * &lt;/RelativeLayout>
     * </pre>
     * 
     * @param context
     *            the application context
     * @param dataService
     *            an instance of a data service implementing
     *            {@link IGridDataService}
     * @param onItemClickListener
     *            the {@link OnItemClickListener} for a single grid item
     * @param onItemLongClickListener
     * 
     */
    public GridViewPagerAdapter(Context context, IDataProviderManager dataProviderManager,
            OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        this.context = context;
        this.dataProviderManager = dataProviderManager;
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
        this.numberOfPages = getNumberOfPages();
        this.pages = new HashMap<Integer, GridView>();
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int pageNumber) {
        Log.d(TAG, "instantiateItem");
        GridView gridPageView = null;
        if (!pages.containsKey(pageNumber)) {
            GridViewContent gridpageContent = getPageContentForPageNumber(pageNumber);
            gridPageView = new GridView(context, gridpageContent);
            pages.put(pageNumber, gridPageView);
        } else {
            gridPageView = pages.get(pageNumber);
        }
        ((ViewPager) container).addView(gridPageView);

        return gridPageView;
    }

    @Override
    public void notifyDataSetChanged() {
        Log.d(TAG, "notifyDataSetChanged");

        numberOfPages = getNumberOfPages();
        for (Integer pageNumber : pages.keySet()) {
            if (pageNumber <= numberOfPages) {
                GridView gridPageView = pages.get(pageNumber);
                if (pageNumber < getCount()) {
                    gridPageView.updatePageContent(getPageContentForPageNumber(pageNumber));
                } else {
                    gridPageView.updatePageContent(getPageContentForPageNumber(0));
                }
            }
        }
        super.notifyDataSetChanged();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d(TAG, "destroyItem Page " + position + " removed");
        pages.remove(position);
        ((ViewPager) container).removeView((View) object);
    }

    /**
     * Triggers the incrementation of the indicator counter for the content item
     * with the given id. The incrementation is propagated to the pages.
     * 
     * @param id
     *            id of the content item that has benn updated
     */
    public void increaseCounterForContentItemWithId(long id) {
        for (GridView gridView : pages.values()) {
            gridView.increaseCounterForContentItemWithId(id);
        }
    }

    /**
     * Returns the number of Grid-View pages depending of the current
     * configuration of the service
     * 
     * @return number of pages
     */
    public abstract int getNumberOfPages();

    /**
     * Returns the content for a certain page identified by the page number
     * 
     * @param context
     *            the application context
     * @param pageNumber
     *            the page number of the requested page
     * @return the page content of the requested page
     */
    public abstract GridViewContent getPageContentForPageNumber(int pageNumber);

    /**
     * Returns the number of items
     * 
     * @return number of items
     */
    public abstract int getNumberOfCells();
}
