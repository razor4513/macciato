package de.telekom.cldii.data.news.impl;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import de.telekom.cldii.R;
import de.telekom.cldii.data.news.INewsDataProvider;
import de.telekom.cldii.service.UpdateInterval;

/**
 * Inserts initial categories and news feeds into the database
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class InitialNewsDataFactory {

    /**
     * Creates the initial news database data
     * 
     * @param context
     *            the context
     * @param newsDataProvider
     *            the {@link INewsDataProvider} where to create
     */
    public static void createInitialNewsData(Context context, INewsDataProvider newsDataProvider) {
        String[] names = context.getResources().getStringArray(R.array.initialCategories);
        long[] catIds = new long[names.length];

        HashMap<String, Integer> iconDrawables = new HashMap<String, Integer>();
        int[] iconRes = { R.drawable.icon_cars, R.drawable.icon_culture, R.drawable.icon_politics, R.drawable.icon_rss,
                R.drawable.icon_sport, R.drawable.icon_computer, R.drawable.icon_knowledge };
        
        for (int i = 0; i < names.length; i++)
            iconDrawables.put(names[i], iconRes[i]);

        for (int i = 0; i < names.length; i++) {
            Integer drawableId = iconDrawables.get(names[i]);
            Bitmap iconBitmap = null;
            if (drawableId != null) {
                iconBitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);
            }
            catIds[i] = newsDataProvider.createNewCategory(names[i], UpdateInterval.TEN_MINUTES, iconBitmap);
        }

        String[] automotive = { "http://www.welt.de/DTAG/motor" };
        String[] culture = { "http://www.welt.de/DTAG/kultur" };
        String[] politics = { "http://www.welt.de/DTAG/politik" };
        String[] headlines = { "http://www.welt.de/DTAG" };
        String[] sports = { "http://www.welt.de/DTAG/sport" };
        String[] web = { "http://www.welt.de/DTAG/webwelt" };        
        String[] science = { "http://www.welt.de/DTAG/wissen" };

        String[][] feeds = { automotive, culture, politics, headlines, sports, web, science };

        for (int i = 0; i < feeds.length; i++) {
            for (String url : feeds[i]) {
                newsDataProvider.createNewNewsFeed(url, catIds[i]);
            }
        }

    }
}
