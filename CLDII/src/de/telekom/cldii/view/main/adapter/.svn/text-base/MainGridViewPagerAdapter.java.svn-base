package de.telekom.cldii.view.main.adapter;

import android.content.Context;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.view.main.adapter.MainGridCellContent.OptionType;
import de.telekom.cldii.widget.grid.GridViewContent;
import de.telekom.cldii.widget.grid.GridViewPagerAdapter;

/**
 * {@link GridViewPagerAdapter} for the main view.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class MainGridViewPagerAdapter extends GridViewPagerAdapter {

    public MainGridViewPagerAdapter(Context context, IDataProviderManager dataProviderManager,
            OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        super(context, dataProviderManager, onItemClickListener, onItemLongClickListener);
    }

    @Override
    public int getNumberOfPages() {
        return 1;
    }

    @Override
    public GridViewContent getPageContentForPageNumber(int pageNumber) {
        GridViewContent content = new GridViewContent();
        content.addCellContent(MainGridCellContent.getOption(OptionType.TYPE_PHONE, context, dataProviderManager,
                onItemClickListener, onItemLongClickListener));
        content.addCellContent(MainGridCellContent.getOption(OptionType.TYPE_SMS, context, dataProviderManager,
                onItemClickListener, onItemLongClickListener));
        content.addCellContent(MainGridCellContent.getOption(OptionType.TYPE_EMAIL, context, dataProviderManager,
                onItemClickListener, onItemLongClickListener));
        content.addCellContent(MainGridCellContent.getOption(OptionType.TYPE_NEWS, context, dataProviderManager,
                onItemClickListener, onItemLongClickListener));
        return content;
    }

    @Override
    public int getNumberOfCells() {
        return 4;
    }
}
