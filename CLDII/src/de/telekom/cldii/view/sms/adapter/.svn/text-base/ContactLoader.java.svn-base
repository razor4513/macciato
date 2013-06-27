package de.telekom.cldii.view.sms.adapter;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;

/**
 * A lazy loader class for contact image and name.
 * 
 * @author sstallen
 * 
 */
public class ContactLoader {

    private Map<ImageView, Contact> imageViewMap = Collections.synchronizedMap(new WeakHashMap<ImageView, Contact>());
    private LruCache<Contact, Bitmap> imageCacheReference;
    private ExecutorService executorService;

    private IDataProviderManager dataProviderManager;
    final int stub_id = R.drawable.contact_center_cropped;

    /**
     * Constructor for {@link ContactLoader}.
     * 
     * @param dataProviderManager
     * @param isTypePhoneNumber
     */
    public ContactLoader(IDataProviderManager dataProviderManager, LruCache<Contact, Bitmap> imageCacheReference) {
        executorService = Executors.newFixedThreadPool(1);
        this.dataProviderManager = dataProviderManager;
        this.imageCacheReference = imageCacheReference;
    }

    /**
     * 
     * @param phoneNumber
     * @param imageView
     * @param contactNameTextView
     */
    public void displayImage(Contact contact, ImageView imageView) {
        imageViewMap.put(imageView, contact);

        queuePhoto(contact, imageView);
        imageView.setImageResource(stub_id);
    }

    private void queuePhoto(Contact contact, ImageView imageView) {
        DataToLoad p = new DataToLoad(contact, imageView);
        executorService.submit(new DataLoader(p));
    }

    /**
     * Method for getting an image from a {@link Contact}.
     * 
     * @param contact
     * @return Contact image as {@link Bitmap}
     */
    private Bitmap getBitmap(Contact contact) {
        if (contact != null) {
            Bitmap contactImage = dataProviderManager.getContactDataProvider().getContactPhoto(contact);
            return contactImage;
        }
        return null;
    }

    /**
     * Task for the lazy load queue.
     * 
     * @author sstallen
     * 
     */
    private class DataToLoad {
        public Contact id;
        public ImageView imageView;

        public DataToLoad(Contact u, ImageView i) {
            id = u;
            imageView = i;
        }
    }

    /**
     * Class for asynchronous loading data.
     * 
     * @author sstallen
     * 
     */
    class DataLoader implements Runnable {
        DataToLoad dataToLoad;

        /**
         * Constructor for {@link DataLoader}.
         * 
         * @param p
         *            the {@link DataToLoad} object
         */
        DataLoader(DataToLoad p) {
            this.dataToLoad = p;
        }

        @Override
        public void run() {
            if (imageViewReused(dataToLoad))
                return;
            Bitmap bmp = getBitmap(dataToLoad.id);
            if (imageViewReused(dataToLoad))
                return;
            DataDisplayer bd = new DataDisplayer(bmp, dataToLoad);
            Activity a = (Activity) dataToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    boolean imageViewReused(DataToLoad photoToLoad) {
        Contact tag = imageViewMap.get(photoToLoad.imageView);
        if (tag == null
                || !(tag.getId().equals(photoToLoad.id.getId()) && tag.getLookupKey().equals(
                        photoToLoad.id.getLookupKey())))
            return true;
        return false;
    }

    /**
     * Used to display bitmap in the UI thread.
     * 
     * @author sstallen
     * 
     */
    class DataDisplayer implements Runnable {
        Bitmap bitmap;
        DataToLoad dataToLoad;

        /**
         * Constructor for {@link DataDisplayer}.
         * 
         * @param b
         *            the {@link Bitmap} to set
         * @param t
         *            the {@link String} to set
         * @param p
         *            the {@link DataToLoad} object
         */
        public DataDisplayer(Bitmap b, DataToLoad p) {
            bitmap = b;
            dataToLoad = p;
        }

        /**
         * Sets the loaded image and name to the {@link ImageView} and
         * {@link TextView}.
         */
        public void run() {
            if (imageViewReused(dataToLoad))
                return;
            if (bitmap != null) {
                if (imageCacheReference != null)
                    imageCacheReference.put(dataToLoad.id, bitmap);
                dataToLoad.imageView.setImageBitmap(bitmap);
            } else {
                dataToLoad.imageView.setImageResource(stub_id);
            }
        }
    }

}
