/**
 * Represents a single news item
 */
package de.telekom.cldii.data.news;

import java.util.Date;

import android.graphics.drawable.Drawable;

/**
 * Represents a single news item
 * 
 * @author Christoph HŸbner
 */
public class NewsItem {

    // the identifier of this news item
    private int newsId;

    // the name of the parent feed
    private String parentFeedName;

    // the title of this news item
    private String title;

    // the read status of this news item
    private boolean isRead; 
    
    // the summary text of this news item
    private String summary;

    // the thumbnail of this news item
    private Drawable image;

    // the creation date of this news item
    private Date creationDate;

    // the URL of this news item
    private String url;

    // the content of this news item (still encoded)
    private String content;
    
    
    // / Setter:

    /**
     * Sets the identifier for this news item
     * 
     * @param newId
     *            new identifier
     */
    public void setNewsId(int newId) {
        newsId = newId;
    }

    /**
     * Sets the name of the parent feed for this news item
     * 
     * @param newId
     *            new feed name
     */
    public void setParentFeedName(String newFeedName) {
        parentFeedName = newFeedName;
    }

    /**
     * Sets the title
     * 
     * @param newTitle
     *            the new title
     */
    public void setTitle(String newTitle) {
        title = newTitle;
    }

    /**
     * Sets the read status
     * 
     * @param newIsReadStatus
     *            the new read status (<code>true</code>, if the item was read by the user)
     */
    public void setIsRead(boolean newIsReadStatus) {
        isRead = newIsReadStatus;
    }

    /**
     * Sets the summary
     * 
     * @param newSummary
     *            the new summary
     */
    public void setSummary(String newSummary) {
        summary = newSummary;
    }

    /**
     * Sets the thumbnail image of this news item
     * 
     * @param newImage
     *            the new thumbnail image
     */
    public void setImage(Drawable newImage) {
        image = newImage;
    }

    /**
     * Sets the creation date of this news item
     * 
     * @param newDate
     *            the new creation date
     */
    public void setCreationDate(Date newDate) {
        creationDate = newDate;
    }

    /**
     * Sets the URL
     * 
     * @param newUrl
     *            new URL
     */
    public void setUrl(String newUrl) {
        url = newUrl;
    }
    
    /**
     * Sets the content
     * 
     * @param newContent
     *            the new content
     */
    public void setContent(String newContent) {
        content = newContent;
    }


    // / Getter:

    /**
     * Returns the identifier
     * 
     * @return the identifier of the news item
     */
    public int getNewsId() {
        return newsId;
    }

    /**
     * Returns the name of the parent feed
     */
    public String getParentFeedName() {
        return parentFeedName;
    }

    /**
     * Returns the title of the news item
     * 
     * @return title of the news item
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the read status of the news item
     * 
     * @return <code>true</code>, if the item was read by the user, <code>false</code> otherwise
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Returns the summary of the news item
     * 
     * @return summary of the news item
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Returns the thumbnail image of the news item
     * 
     * @return thumbnail image of the news item
     */
    public Drawable getImage() {
        return image;
    }

    /**
     * Returns the creation time of the news item
     * 
     * @return creation time of the news item
     */
    public Date getCreationDate() {
        return creationDate;
    }
    
    /**
     * Returns the URL of the news item
     * 
     * @return URL of the news item.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the content of the news item. The content has the original encoding.
     * 
     * @return content of the news item
     */
    public String getContent() {
        return content;
    }
    
}
