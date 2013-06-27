/**
 * Defines the methods of the item of a PageContent object
 */
package de.telekom.cldii.widget.grid;

import android.graphics.Bitmap;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * Defines the methods of the item of a PageContent object
 * 
 * @author Christoph Huebner
 */
public interface IGridCellContent {

    /**
     * Returns the text to be displayed for this item
     * 
     * @return text
     */
    public String getText();

    /**
     * Returns the image to be displayed for this item
     * 
     * @return item as byte array
     */
    public Bitmap getImage();

    /**
     * Returns the id for this item
     * 
     * @return item id as long
     */
    public long getId();

    /**
     * Returns the layout resource id for this item
     * 
     * @return layout resource id
     */
    public int getLayoutId();
    
    /**
     * Returns the indicator count for this item.
     * 
     * @return the indicator count. Returns <code>null</code> if no count
     *         exists.
     */
    public Integer getIndicatorCount();

    /**
     * Returns the {@link OnItemClickListener} for the grid cell content
     * 
     * @return the onItemClickListener
     */
    public OnItemClickListener getOnItemClickListener();

    /**
     * Returns the {@link OnItemLongClickListener} for the grid cell content
     * 
     * @return the onItemLongClickListener
     */
    public OnItemLongClickListener getOnItemLongClickListener();

    /**
     * Sets the {@link OnItemClickListener} for the grid cell content
     * 
     * @return the onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onClickListener);

    /**
     * Sets the {@link OnItemLongClickListener} for the grid cell content
     * 
     * @return the OnItemLongClickListener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener onLongClickListener);
}
