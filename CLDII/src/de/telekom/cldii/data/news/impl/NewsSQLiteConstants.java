/**
 * Static constant for the news SQLite database
 */
package de.telekom.cldii.data.news.impl;

/**
 * Encapsulates the constants for accessing the SQLite database of the news component
 * 
 * @author Christoph HŸbner
 * 
 */
public class NewsSQLiteConstants {

    // Table names
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_FEEDS = "feeds";
    public static final String TABLE_NEWS = "news";
    public static final String TABLE_IMAGES = "images";
    
    // Table images column names
    public static final String IMAGES_ID = "_id";
    public static final String IMAGES_URL = "imageurl";
    public static final String IMAGES_CONTENT = "imagecontent";
    public static final String IMAGES_DATE = "imagedate";

    // Table categories column names
    public static final String CATEGORIES_ID = "_id";
    public static final String CATEGORIES_NAME = "categoryname";
    public static final String CATEGORIES_INTERVAL = "interval";
    public static final String CATEGORIES_ICON = "icon";
    public static final String CATEGORIES_LASTUPDATE = "lastupdate";

    // Table feeds column names
    public static final String FEEDS_ID = "_id";
    public static final String FEEDS_NAME = "feedname";
    public static final String FEEDS_URL = "feedurl";
    public static final String FEEDS_ACTIVATED = "feedactivated";
    public static final String FEEDS_CID = "cid";

    // Table news column names
    public static final String NEWS_ID = "_id";
    public static final String NEWS_FID = "fid";
    public static final String NEWS_GUID = "guid";
    public static final String NEWS_READ = "read";
    public static final String NEWS_TITLE = "title";
    public static final String NEWS_SUMMARY = "summary";
    public static final String NEWS_DATE = "newsdate";
    public static final String NEWS_CONTENT = "newscontent";
    public static final String NEWS_URL = "newsurl";
    public static final String NEWS_IMAGE = "newsimage";

}
