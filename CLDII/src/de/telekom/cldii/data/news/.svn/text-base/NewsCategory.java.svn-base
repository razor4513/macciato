/**
 * Represents the news category for news feeds
 */
package de.telekom.cldii.data.news;

import android.graphics.Bitmap;
import de.telekom.cldii.R;
import de.telekom.cldii.service.UpdateInterval;
import de.telekom.cldii.widget.grid.AbstractGridCellContent;
import de.telekom.cldii.widget.grid.IGridCellContent;

/**
 * Represents the news category for news feeds
 * 
 * @author Christoph HŸbner
 */
public class NewsCategory extends AbstractGridCellContent implements IGridCellContent {

    // the identifier of this category
    private long categoryId;

    // the text describing this category
    private String name;

    // the thumbnail of this category
    private Bitmap image;

    // update interval of this category
    private UpdateInterval updateInterval;

    // the number of unread news in this category
    private int numberOfUnreadNews;

    /**
     * Creates a new category
     * 
     * @param categoryId
     *            The identifier of the category
     * @param text
     *            A describing text
     * @param image
     *            a thumbnail image
     */
    public NewsCategory(int categoryId, String text, Bitmap image, UpdateInterval updateInterval, int numberOfUnreadNews) {
        this.categoryId = categoryId;
        this.name = text;
        this.image = image;
        this.updateInterval = updateInterval;
        this.numberOfUnreadNews = numberOfUnreadNews;
    }

    /**
     * Sets the identifier for this category
     * 
     * @param newId
     *            new identifier
     */
    public void setCategoryId(int newId) {
        categoryId = newId;
    }

    /**
     * Sets the text
     * 
     * @param newText
     *            new text
     */
    public void setText(String newText) {
        name = newText;
    }

    /**
     * Sets the image
     * 
     * @param newImage
     *            new image
     */
    public void setImage(Bitmap newImage) {
        image = newImage;
    }

    /**
     * Sets the update interval
     * 
     * @param updateInterval
     *            the updateInterval to be set
     */
    public void setUpdateInterval(UpdateInterval updateInterval) {
        this.updateInterval = updateInterval;
    }

    /**
     * Returns the identifier
     * 
     * @return the identifier of the category as long
     */
    public long getId() {
        return categoryId;
    }

    /**
     * Returns the name
     * 
     * @return the name of the category as String
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the name
     * 
     * @return the name of the category as String
     */
    @Override
    public String getText() {
        return name;
    }

    /**
     * Returns the image
     * 
     * @return the image of the category as Bitmap
     */
    @Override
    public Bitmap getImage() {
        return image;
    }

    /**
     * Returns the category's update interval
     * 
     * @return the category's update interval
     */
    public UpdateInterval getUpdateInterval() {
        return this.updateInterval;
    }

    @Override
    public Integer getIndicatorCount() {
        return numberOfUnreadNews;
    }

    @Override
    public int getLayoutId() {
        return R.layout.news_categories_row;
    }

}
