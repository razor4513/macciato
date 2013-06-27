package de.telekom.cldii.widget.grid;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import de.telekom.cldii.R;

/**
 * A widget to show data in several {@link GridView}s nested in a
 * {@link ViewPager}.<br>
 * <br>
 * 
 * You can use it from XML like: <de.telekom.cldii.widget.grid.GridViewPager
 * .../><br>
 * <br>
 * 
 * A {@link GridViewPager} needs a {@link GridViewPagerAdapter}. You can set it
 * on your GridPager instance using
 * {@link GridViewPager#setAdapter(GridViewPagerAdapter)}.<br>
 * <br>
 * 
 * It has a indicator view for the visualization of current/maximal pages. If
 * the passed data doesn't fill more than one page, it won't show the indicator.
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 */
public class GridViewPager extends RelativeLayout {
    private Context context;
    private int pagesCount = 0;
    private int indicatorContainerId;
    private ViewPager viewPager;
    private LinearLayout pageIndicatorView;
    private GridViewPagerAdapter adapter;

    public GridViewPager(Context context) {
        super(context);
        this.context = context;
        initializeGridViewPager();
    }

    public GridViewPager(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        this.context = context;

        inflate(context, R.layout.gridview_layout, this);

        initializeGridViewPager();
    }

    /**
     * Initialization method. Generates and configurates a {@link ViewPager} and
     * a {@link LinearLayout} for page indication.
     */
    private void initializeGridViewPager() {
        // Configuration of the indicator view.
        pageIndicatorView = (LinearLayout) inflate(context, R.layout.gridview_pageindicator, null);

        // Configuration of the ViewPager
        viewPager = new android.support.v4.view.ViewPager(context);
        viewPager.setOnPageChangeListener(new PageChangeListener());

        ((FrameLayout) findViewById(R.id.gridTop)).addView(viewPager);
        ((FrameLayout) findViewById(R.id.gridBottom)).addView(pageIndicatorView);
    }

    /**
     * Using this method you can set a {@link GridViewPagerAdapter} for the
     * {@link GridViewPager}.
     * 
     * @param gridPagerAdapter
     */
    public void setAdapter(GridViewPagerAdapter gridPagerAdapter) {
        this.adapter = gridPagerAdapter;
        viewPager.setAdapter(adapter);

        if (adapter != null) {
            if (adapter.getCount() <= 1) {
                pageIndicatorView.setVisibility(View.INVISIBLE);
            } else {
                pageIndicatorView.setVisibility(View.VISIBLE);
            }
        }

        // set initial page for the indicator view
        setIndicator(0);
    }

    public boolean hasAdapter() {
        return (viewPager.getAdapter() != null);
    }

    public void notifyDataSetChanged() {
        if (hasAdapter()) {
            viewPager.getAdapter().notifyDataSetChanged();
        }
        if (adapter != null) {
            setIndicator(viewPager.getCurrentItem());
            if (adapter.getCount() <= 1) {
                pageIndicatorView.setVisibility(View.INVISIBLE);
            } else {
                pageIndicatorView.setVisibility(View.VISIBLE);
            }
        }
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public int getIndicatorContainerId() {
        return indicatorContainerId;
    }

    public void setIndicatorContainerId(int indicatorContainerId) {
        this.indicatorContainerId = indicatorContainerId;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }

    private void setIndicator(int currentPage) {
        int maxPages = viewPager.getAdapter().getCount();

        pageIndicatorView.removeAllViews();
        for (int i = 0; i < maxPages; i++) {
            ImageView dotImageView = (ImageView) inflate(context, R.layout.gridview_pagedot, null);

            if (currentPage == i) {
                android.content.res.TypedArray styled = context
                        .obtainStyledAttributes(new int[] { R.attr.gridPageIndicator_active });
                dotImageView.setImageDrawable(styled.getDrawable(0));
                styled.recycle();
            }

            pageIndicatorView.addView(dotImageView);
        }
    }

    private class PageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            setIndicator(position);
        }
    }

    public void increaseCounterForContentItemWithId(long id) {
        adapter.increaseCounterForContentItemWithId(id);
    }
}
