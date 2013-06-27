/**
 * 
 */
package de.telekom.cldii.view.phone.adapter;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.widget.grid.AbstractGridCellContent;
import de.telekom.cldii.widget.grid.GridViewContent;
import de.telekom.cldii.widget.grid.GridViewPagerAdapter;
import de.telekom.cldii.widget.grid.IGridCellContent;

/**
 * {@link GridViewPagerAdapter} for the phone module.
 * 
 * @author Christoph HŸbner
 * 
 */
public class PhoneGridViewPagerAdapter extends GridViewPagerAdapter {
    private OnItemClickListener addFavoriteOnItemClickListener;

    public PhoneGridViewPagerAdapter(Context context, IDataProviderManager dataProviderManager,
            OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener,
            OnItemClickListener addFavoriteOnItemClickListener) {
        super(context, dataProviderManager, onItemClickListener, onItemLongClickListener);
        this.addFavoriteOnItemClickListener = addFavoriteOnItemClickListener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.telekom.cldii.data.IGridDataService#getPageContentForPageNumber(int)
     */
    @Override
    public GridViewContent getPageContentForPageNumber(int pageNumber) {
        GridViewContent gridViewContent = null;
        if ((pageNumber + 1) == getNumberOfPages() && (getNumberOfCells() % GridViewContent.PAGE_SIZE == 0)) {
            gridViewContent = new GridViewContent();
        } else {
            gridViewContent = this.getFavoritesPageOrderedByName(pageNumber);
            if (gridViewContent == null)
                gridViewContent = new GridViewContent();
        }

        for (int i = 0; i < gridViewContent.getNoOfCells(); i++) {
            IGridCellContent cellContent = gridViewContent.getCellContent(i);
            cellContent.setOnItemClickListener(this.onItemClickListener);
            cellContent.setOnItemLongClickListener(this.onItemLongClickListener);
        }
        if ((pageNumber + 1) == getNumberOfPages()) {
            IGridCellContent addCellContent = new AbstractGridCellContent() {

                @Override
                public String getText() {
                    return context.getString(R.string.phone_add_favorite);
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
                    return R.layout.phone_fav_entryadd;
                }
            };
            addCellContent.setOnItemClickListener(addFavoriteOnItemClickListener);
            gridViewContent.addCellContent(addCellContent);
        }
        return gridViewContent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.telekom.cldii.data.phone.IPhoneDataService#getNumberOfPages()
     */
    @Override
    public int getNumberOfPages() {
        // +1 for add grid item
        int numberOfFavorites = getNumberOfCells() + 1;
        return numberOfFavorites / GridViewContent.PAGE_SIZE
                + (numberOfFavorites % GridViewContent.PAGE_SIZE > 0 ? 1 : 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.telekom.cldii.data.phone.IPhoneDataService#getFavoritesPageOrderedByName
     * (int)
     */
    private GridViewContent getFavoritesPageOrderedByName(int pageNumber) {
        List<GridViewContent> favoritesPages = this.getFavoritesPages();
        if (pageNumber < favoritesPages.size()) {
            return favoritesPages.get(pageNumber);
        } else {
            return null;
        }
    }

    /**
     * Returns the favorites as pages
     * 
     * @return list of favorite pages
     */
    private List<GridViewContent> getFavoritesPages() {
        return transformSimpleListToPageList(getFavorites());
    }

    /**
     * Transforms a flat List of <code>PhoneContact</code> objects into a list
     * of Pages
     * 
     * @param simpleList
     *            the flat List
     * @return a list of pages (= <code>PageContent</code> objects)
     */
    private List<GridViewContent> transformSimpleListToPageList(List<PhoneGridCellContent> simpleList) {
        List<GridViewContent> pages = new ArrayList<GridViewContent>();
        GridViewContent currPage = null;
        for (int i = 0; i < simpleList.size(); i++) {
            if (i % GridViewContent.PAGE_SIZE == 0) {
                currPage = new GridViewContent();
                pages.add(currPage);
            }
            currPage.addCellContent(simpleList.get(i));
        }

        return pages;
    }

    /**
     * Retrieves all favorites as a list
     * 
     * @return
     */
    private List<PhoneGridCellContent> getFavorites() {

        ArrayList<PhoneGridCellContent> favorites = new ArrayList<PhoneGridCellContent>();
        List<Contact> contacts = new ArrayList<Contact>(dataProviderManager.getContactDataProvider()
                .getFavoriteContacts());
        Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.contact_center_cropped);
        for (Contact contact : contacts) {
            // No phone number check here because getFavoriteContacts delivers
            // only contacts having a number.
            favorites.add(new PhoneGridCellContent(contact, dataProviderManager, defaultBitmap));
        }

        return favorites;
    }

    @Override
    public int getNumberOfCells() {
        return getFavorites().size();
    }
}
