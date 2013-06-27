package de.telekom.cldii.service.imagedownload;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.data.IDataProviderManager;

/**
 * Asynchronous task to retrieve images of RSS feeds
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class RSSImageRetriever extends AsyncTask<RSSImageRetrieverItem, Void, Boolean> {
    private final String TAG = "RSSImageRetriever";
    private final RSSImageRetrieverOnCompleteListener<AsyncTask<?, ?, ?>> listener;
    private final String broadcastId;
    private IDataProviderManager dataManagerProvider;
    private Context context;

    /**
     * Creates a image retriever asynchronous task with a callback via a
     * listener
     * 
     * @param listener
     *            the instance to dispatch the callback to
     */
    public RSSImageRetriever(RSSImageRetrieverOnCompleteListener<AsyncTask<?, ?, ?>> listener,
            IDataProviderManager dataManagerProvider, Context context) {
        this.listener = listener;
        this.broadcastId = null;
        this.dataManagerProvider = dataManagerProvider;
        this.context = context;
    }

    /**
     * Creates a image retriever asynchronous task with a callback via broadcast
     * 
     * @param broadcastId
     *            the broadcast identifier to be called when complete
     */
    public RSSImageRetriever(String broadcastId) {
        this.listener = null;
        this.broadcastId = broadcastId;
    }

    @Override
    protected Boolean doInBackground(RSSImageRetrieverItem... params) {
        for (int i = 0; i < params.length; i++) {
            try {
                InputStream in = null;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                new HttpGet(params[i].getUrl());
                try {
                    URL url = new URL(params[i].getUrl());
                    if (url != null)
                        in = (InputStream) url.getContent();

                    if (in != null) {
                        // Scale icon/photo to the desired dimensions
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                                .getMetrics(displayMetrics);
                        float reqWidth = ApplicationConstants.THUMBNAIL_WIDTH * displayMetrics.density;
                        float reqHeight = ApplicationConstants.THUMBNAIL_HEIGHT * displayMetrics.density;

                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(in, null, opts);
                        int height = opts.outHeight;
                        int width = opts.outWidth;
                        Log.i(TAG, "Image size: " + width + "x" + height);

                        int inSampleSize = 1;
                        if (height > reqHeight || width > reqWidth) {
                            if (width > height) {
                                inSampleSize = Math.round((float) height / reqHeight);
                            } else {
                                inSampleSize = Math.round((float) width / reqWidth);
                            }
                        }
                        in = (InputStream) url.getContent();
                        opts.inJustDecodeBounds = false;
                        opts.inSampleSize = inSampleSize;
                        Bitmap imageBitmap = BitmapFactory.decodeStream(in, null, opts);
                        width = imageBitmap.getWidth();
                        height = imageBitmap.getHeight();
                        byte[] imageByte = null;
                        if (imageBitmap != null) {
                            if (params[i].isThumbnail() || width > reqWidth || height > reqHeight) {
                                float factor = (reqHeight / height);
                                if (width > reqWidth)
                                    factor = (reqWidth / width);
                                // create a matrix for scaling
                                Matrix matrix = new Matrix();
                                matrix.postScale(factor, factor);

                                // recreate the new Bitmap
                                Bitmap resizedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, height, matrix,
                                        true);
                                // also compress it in file size
                                if (params[i].isThumbnail())
                                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG,
                                            ApplicationConstants.THUMBNAIL_QUALITY, out);
                                else
                                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG,
                                            ApplicationConstants.OTHERIMAGES_QUALITY, out);
                                imageByte = out.toByteArray();
                                imageBitmap.recycle();
                                resizedBitmap.recycle();
                            } else {
                                imageBitmap.compress(Bitmap.CompressFormat.JPEG,
                                        ApplicationConstants.OTHERIMAGES_QUALITY, out);
                                imageByte = out.toByteArray();
                                imageBitmap.recycle();
                            }

                            if (params[i].isThumbnail())
                                dataManagerProvider.getNewsDataProvider().storeNewsItemThumbnail(params[i].getUrl(),
                                        imageByte);
                            else
                                dataManagerProvider.getNewsDataProvider().storeNewsItemContentImage(params[i].getUrl(),
                                        imageByte);

                            Intent intent = new Intent();
                            intent.setAction(ApplicationConstants.IMAGE_LOADED);
                            intent.putExtra(ApplicationConstants.EXTRAS_KEY_BROADCASTID, params[i].getBroadcastId());
                            intent.putExtra(ApplicationConstants.EXTRAS_KEY_BROADCASTINFO, params[i].getBroadcastInfo());
                            context.sendBroadcast(intent);
                        }

                    }
                } catch (IllegalStateException e) {
                    Log.w(TAG, "Database closed!");
                } catch (UnknownHostException e) {
                    Log.v(TAG, "Connectivity Problem: " + e.getMessage());
                } catch (FileNotFoundException e) {
                    Log.v(TAG, "Image URL invalid: " + e.getMessage());
                } catch (MalformedURLException e) {
                    Log.v(TAG, "Image URL " + params[i].getUrl() + " invalid:\n" + e.getMessage());
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.v(TAG, "Connection Problem: Image URL couldn't be opened! " + e.getMessage());
                    e.printStackTrace();
                }
                publishProgress();

                if (in != null)
                    in.close();
                out.close();
            } catch (IllegalArgumentException e1) {
                Log.w(TAG, params[i].getUrl() + " invalid!");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
        if (listener != null)
            listener.onRssImageRetrieverProgress();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (listener != null)
            // Callback
            listener.onRssImageRetrieverExecutionComplete(this);
        else {
            // Broadcast
            Intent intent = new Intent();
            intent.setAction(this.broadcastId);
            context.sendBroadcast(intent);
        }
    }

}
