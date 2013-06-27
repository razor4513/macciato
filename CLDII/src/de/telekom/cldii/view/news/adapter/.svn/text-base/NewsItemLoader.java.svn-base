package de.telekom.cldii.view.news.adapter;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.news.impl.NewsSQLiteConstants;

public class NewsItemLoader {

    private Map<ImageView, Long> imageViewMap = Collections.synchronizedMap(new WeakHashMap<ImageView, Long>());
    ExecutorService executorService;

    private IDataProviderManager dataProviderManager;
    private LruCache<Long, Bitmap> imageCacheReference;

    public NewsItemLoader(IDataProviderManager dataProviderManager) {
        this.executorService = Executors.newFixedThreadPool(1);
        this.dataProviderManager = dataProviderManager;
        this.imageCacheReference = null;
    }
    
    public NewsItemLoader(IDataProviderManager dataProviderManager, LruCache<Long, Bitmap> imageCacheReference) {
        this.executorService = Executors.newFixedThreadPool(1);
        this.dataProviderManager = dataProviderManager;
        this.imageCacheReference = imageCacheReference;
    }

    final int stub_id = R.drawable.d_background_rss;
    final int thumbnailbg_id = R.drawable.d_background_thumbnail;

    public void displayImage(Long newsId, View view) {
        imageViewMap.put((ImageView) view.findViewById(R.id.newsThumbnail), newsId);

        view.setBackgroundResource(stub_id);
        ((ImageView) view.findViewById(R.id.newsThumbnail)).setImageResource(0);
        queuePhoto(newsId, view);
    }

    private void queuePhoto(Long newsId, View view) {
        DataToLoad p = new DataToLoad(newsId, view);
        this.executorService.submit(new DataLoader(p));
    }

    private Bitmap getBitmap(Long id) {
        Bitmap contactImage = (Bitmap) dataProviderManager.getNewsDataProvider().getNewsItemColumnForNewsId(id,
                NewsSQLiteConstants.NEWS_IMAGE);
        return contactImage;
    }

    // Task for the queue
    private class DataToLoad {
        public Long id;
        public View imageView;

        public DataToLoad(Long id, View image) {
            this.id = id;
            this.imageView = image;
        }
    }

    class DataLoader implements Runnable {
        DataToLoad dataToLoad;

        DataLoader(DataToLoad p) {
            this.dataToLoad = p;
        }

        @Override
        public void run() {
            if (imageViewReused(dataToLoad))
                return;
            Bitmap bmp = getBitmap(dataToLoad.id);

            DataDisplayer bd = new DataDisplayer(bmp, dataToLoad);
            Activity a = (Activity) dataToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    boolean imageViewReused(DataToLoad photoToLoad) {
        Long tag = imageViewMap.get((ImageView) photoToLoad.imageView.findViewById(R.id.newsThumbnail));
        if (tag == null || !tag.equals(photoToLoad.id)) {
            return true;
        }
        return false;
    }

    // Used to display bitmap in the UI thread
    class DataDisplayer implements Runnable {
        Bitmap bitmap;
        DataToLoad photoToLoad;

        public DataDisplayer(Bitmap b, DataToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null) {
                if (imageCacheReference != null)
                    imageCacheReference.put(photoToLoad.id, bitmap);
                photoToLoad.imageView.setBackgroundResource(thumbnailbg_id);
                ((ImageView) photoToLoad.imageView.findViewById(R.id.newsThumbnail)).setImageBitmap(bitmap);
                
            } else {
                photoToLoad.imageView.setBackgroundResource(stub_id);
                ((ImageView) photoToLoad.imageView.findViewById(R.id.newsThumbnail)).setImageBitmap(null);
            }
        }
    }

}
