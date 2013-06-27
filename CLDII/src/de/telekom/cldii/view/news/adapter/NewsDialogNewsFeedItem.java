package de.telekom.cldii.view.news.adapter;

import java.util.List;

import de.telekom.cldii.data.news.NewsCategory;
import de.telekom.cldii.data.news.NewsFeed;

public class NewsDialogNewsFeedItem {

    private NewsCategory category;

    private List<NewsFeed> newsFeeds;

    public NewsDialogNewsFeedItem(NewsCategory category, List<NewsFeed> newsFeeds) {
        this.category = category;
        this.newsFeeds = newsFeeds;
    }
    
    public NewsCategory getCategory() {
        return this.category;
    }
    
    public List<NewsFeed> getNewsFeeds() {
        return this.newsFeeds;
    }
}
