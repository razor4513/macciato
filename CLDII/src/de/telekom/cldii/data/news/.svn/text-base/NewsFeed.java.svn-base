/**
 * Represents a news feed
 */
package de.telekom.cldii.data.news;

/**
 * Represents a news feed
 * 
 * @author Christoph HŸbner
 */
public class NewsFeed {

    // the identifier of this feed
    private int feedId;

    // the identifier of the parent category
    private long parentCategoryId;

    // the name of this feed
    private String name;

    // the URL of this feed
    private String url;

    // activation flag for news pulling
    private boolean activated;

    /**
     * Sets the identifier for this feed
     * 
     * @param newId
     *            new identifier
     */
    public void setFeedId(int newId) {
        feedId = newId;
    }

    /**
     * Sets the identifier of the parent category for this feed
     * 
     * @param newId
     *            new category identifier
     */
    public void setParentCategoryId(long newId) {
        parentCategoryId = newId;
    }

    /**
     * Sets the name
     * 
     * @param newName
     *            new text
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Sets the URL
     * 
     * @param newUri
     *            new URL
     */
    public void setUrl(String newUrl) {
        url = newUrl;
    }

    /**
     * Sets the activation status of the news feed
     * 
     * @param activated
     */
    public void setIsActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Returns the identifier
     * 
     * @return the identifier of the feed
     */
    public int getFeedId() {
        return feedId;
    }

    /**
     * Returns the identifier of the parent category
     */
    public long getParentCategoryId() {
        return parentCategoryId;
    }

    /**
     * Returns the name of the feed
     * 
     * @return name of the feed
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the URL of the feed
     * 
     * @return URL of the feed.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the activation status of this news feed
     * 
     * @return true if activated, false otherwise
     */
    public boolean isActivated() {
        return this.activated;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + feedId;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NewsFeed other = (NewsFeed) obj;
        if (feedId != other.feedId) {
            return false;
        }
        return true;
    }
}