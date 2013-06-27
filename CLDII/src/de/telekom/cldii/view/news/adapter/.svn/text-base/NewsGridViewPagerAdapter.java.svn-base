package de.telekom.cldii.view.news.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.widget.grid.AbstractGridCellContent;
import de.telekom.cldii.widget.grid.GridView;
import de.telekom.cldii.widget.grid.GridViewContent;
import de.telekom.cldii.widget.grid.GridViewPagerAdapter;
import de.telekom.cldii.widget.grid.IGridCellContent;

/**
 * {@link GridViewPagerAdapter} for the news module.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class NewsGridViewPagerAdapter extends GridViewPagerAdapter {
    private OnItemClickListener addCategoryOnItemClickListener;

    public NewsGridViewPagerAdapter(Context context, IDataProviderManager dataProviderManager,
            OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener,
            OnItemClickListener addCategoryOnItemClickListener) {
        super(context, dataProviderManager, onItemClickListener, onItemLongClickListener);
        this.addCategoryOnItemClickListener = addCategoryOnItemClickListener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int pageNumber) {
        GridView gridView = (GridView) super.instantiateItem(container, pageNumber);
        gridView.setMaxIndicatorCount(50);
        gridView.setReplacementAfterMaxIndicatorCount("50");
        gridView.synchronizeIndicatorCount();
        return gridView;
    }

    @Override
    public int getNumberOfPages() {
        // +1 for add grid item
        int numberOfCategories = getNumberOfCells() + 1;
        return (numberOfCategories / GridViewContent.PAGE_SIZE)
                + (numberOfCategories % GridViewContent.PAGE_SIZE > 0 ? 1 : 0);
    }

    @Override
    public GridViewContent getPageContentForPageNumber(int pageNumber) {
        GridViewContent gridViewContent = dataProviderManager.getNewsDataProvider().getNewsCategoryPageOrderedByName(
                pageNumber);
        for (int i = 0; i < gridViewContent.getNoOfCells(); i++) {
            IGridCellContent cellContent = gridViewContent.getCellContent(i);
            cellContent.setOnItemClickListener(this.onItemClickListener);
            cellContent.setOnItemLongClickListener(this.onItemLongClickListener);
        }

        if ((pageNumber + 1) == getNumberOfPages()) {
            IGridCellContent addCellContent = new AbstractGridCellContent() {

                @Override
                public String getText() {
                    return context.getString(R.string.news_add_category);
                }

                @Override
                public Integer getIndicatorCount() {
                    return null;
                }

                @Override
                public Bitmap getImage() {
                    return null;
                }

                @Override
                public long getId() {
                    return -1;
                }

                @Override
                public int getLayoutId() {
                    return R.layout.news_categories_rowadd;
                }
            };
            addCellContent.setOnItemClickListener(addCategoryOnItemClickListener);
            gridViewContent.addCellContent(addCellContent);
        }

        return gridViewContent;

    }

    @Override
    public int getNumberOfCells() {
        return dataProviderManager.getNewsDataProvider().getNumberOfCategories();
    }
}
