package de.telekom.cldii.view.phone.adapter;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;

public class ContactLoader {

    private Map<ImageView, Long> imageViewMap = Collections.synchronizedMap(new WeakHashMap<ImageView, Long>());
    private Map<TextView, Long> textViewMap = Collections.synchronizedMap(new WeakHashMap<TextView, Long>());
    ExecutorService executorService;

    private IDataProviderManager dataProviderManager;

    public ContactLoader(IDataProviderManager dataProviderManager, boolean isTypePhoneNumber) {
        executorService = Executors.newFixedThreadPool(1);
        this.dataProviderManager = dataProviderManager;
    }

    final int stub_id = R.drawable.contact_center_cropped;

    public void displayImage(Long string, ImageView imageView, TextView contactNumberView) {
        imageViewMap.put(imageView, string);
        textViewMap.put(contactNumberView, string);

        queuePhoto(string, imageView, contactNumberView);
        imageView.setImageResource(stub_id);
    }

    private void queuePhoto(Long contactId, ImageView imageView, TextView contactNumberView) {
        DataToLoad p = new DataToLoad(contactId, imageView, contactNumberView);
        executorService.submit(new DataLoader(p));
    }

    private Bitmap getBitmap(Contact contact) {
        if (contact != null) {
            Bitmap contactImage = dataProviderManager.getContactDataProvider().getContactPhoto(contact);
            return contactImage;
        }
        return null;
    }

    // Task for the queue
    private class DataToLoad {
        public Long id;
        public ImageView imageView;
        public TextView contactNameTextView;

        public DataToLoad(Long u, ImageView i, TextView contactNumberView) {
            id = u;
            imageView = i;
            contactNameTextView = contactNumberView;
        }
    }

    class DataLoader implements Runnable {
        DataToLoad dataToLoad;

        DataLoader(DataToLoad p) {
            this.dataToLoad = p;
        }

        @Override
        public void run() {
            Contact contact = null;
            contact = dataProviderManager.getContactDataProvider().getContactForId(dataToLoad.id);

            if (imageViewReused(dataToLoad))
                return;
            Bitmap bmp = getBitmap(contact);
            if (imageViewReused(dataToLoad))
                return;
            String text = contact.getDisplayName();
            DataDisplayer bd = new DataDisplayer(bmp, text, dataToLoad);
            Activity a = (Activity) dataToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    boolean imageViewReused(DataToLoad photoToLoad) {
        Long tag = imageViewMap.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.id))
            return true;
        return false;
    }

    // Used to display bitmap in the UI thread
    class DataDisplayer implements Runnable {
        Bitmap bitmap;
        String text;
        DataToLoad dataToLoad;

        public DataDisplayer(Bitmap b, String t, DataToLoad p) {
            bitmap = b;
            dataToLoad = p;
            text = t;
        }

        public void run() {
            if (text != null && text.length() != 0 && dataToLoad.contactNameTextView != null) {
                dataToLoad.contactNameTextView.setText(text);
            }

            if (imageViewReused(dataToLoad))
                return;
            if (bitmap != null)
                dataToLoad.imageView.setImageBitmap(bitmap);
            else
                dataToLoad.imageView.setImageResource(stub_id);
        }
    }

}
