/**
 * Defines the interface for a data service that delivers the data for a Grid-Page
 */
package de.telekom.cldii.data;

import de.telekom.cldii.widget.grid.GridViewContent;

/**
 * Defines the interface for a data service that delivers the data for a Grid-Page
 * 
 * @author Christoph Huebner
 *
 */
public interface IGridDataService {

    /**
     * Returns the number of Grid-View pages depending of the current configuration of the service
     * @return number of pages
     */
    public int getNumberOfPages();
    
    /**
     * Returns the content for a certain page identified by the page number
     * @param context the application context
     * @param pageNumber the page number of the requested page
     * @return the page content of the requested page
     */
    public GridViewContent getPageContentForPageNumber(int pageNumber);
    
    /**
     * Called by the Application when the memory is running low
     */
    public void onLowMemoryWarning();
}
