/**
 * Class represents the content of a single page for a 4-grid-gui-widget
 */
package de.telekom.cldii.widget.grid;

import java.util.ArrayList;
import java.util.List;

/**
 * Class represents the content of a single page for a 4-grid-gui-widget
 * 
 * 
 * @author Christoph Huebner
 */
public class GridViewContent {

    /**
     * Maximum no. of items that a page can contain.
     */
    public static int PAGE_SIZE = 4;

    // Contains the content items for a single page
    private List<IGridCellContent> cellContents = new ArrayList<IGridCellContent>();

    /**
     * Transforms a flat List of <code>IPageContentItem</code> objects into a list of Pages
     * 
     * @param simpleList
     *            the flat List
     * @return a list of pages (= <code>PageContent</code> objects)
     */
    public static List<GridViewContent> transformSimpleListToPageList(
            List<IGridCellContent> simpleList) {
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
     * Returns the number of items, this page contains
     * 
     * @return no of containing items
     */
    public int getNoOfCells() {
        return cellContents.size();
    }

    /**
     * Adds content for a new cell to the page
     * 
     * @param newCellContent
     *            content for a new cell
     * @throws IllegalAccessException
     *             if the page is full, a exception is thrown
     */
    public void addCellContent(IGridCellContent newCellContent) throws IndexOutOfBoundsException {
        // if this page is full, throw exception
        if (getNoOfCells() >= PAGE_SIZE) {
            throw new IndexOutOfBoundsException();
        }

        cellContents.add(newCellContent);
    }
    
    /**
     * Gets the content item at the given location position
     * @param location position of the requested item
     * @return the content item at the given location position
     * @throws IndexOutOfBoundsException
     */
    public IGridCellContent getCellContent(int location) throws IndexOutOfBoundsException {
        if (location >= PAGE_SIZE) {
            throw new IndexOutOfBoundsException();
        }

        return cellContents.get(location);
    }
    
    /**
     * Deletes all existing content items
     */
    public void deleteContent() {
        cellContents = new ArrayList<IGridCellContent>();
    }

}
