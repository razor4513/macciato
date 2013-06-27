package de.telekom.cldii.service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Asynchronous task to check if a given RSS URL is valid.
 * 
 * @author Jun Chen, jambit GmbH
 */
public class NewsRssUrlCheckTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "NewsRssUrlCheckTask";

    private NewsRssUrlCheckTaskListener listener;

    private final int NEWSRSSURLCHECK_TIMEOUT = 10000;

    public NewsRssUrlCheckTask(NewsRssUrlCheckTaskListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        for (int i = 0; i < urls.length; i++) {
            Log.v(TAG, "Checking " + urls[i] + "... ");
            try {
                final HttpGet httpget = new HttpGet(urls[i]);

                final HttpClient httpClient = new DefaultHttpClient();
                HttpParams httpParams = httpClient.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, NEWSRSSURLCHECK_TIMEOUT);
                HttpConnectionParams.setSoTimeout(httpParams, NEWSRSSURLCHECK_TIMEOUT);

                // Send GET request to URI
                final HttpResponse response = httpClient.execute(httpget);

                // Check if server response is valid
                final StatusLine status = response.getStatusLine();
                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    return false;
                }

                // Extract content stream from HTTP response
                HttpEntity entity = response.getEntity();

                HeaderElement[] elements = entity.getContentType().getElements();

                if (elements != null && elements.length > 0) {
                    // Check if content type is atom or rss
                    String contentType = response.getEntity().getContentType().getValue();
                    int end = contentType.indexOf(';');
                    String mime;
                    if (end > -1)
                        mime = contentType.substring(0, end);
                    else
                        mime = contentType;
                    Log.v(TAG, "Mime: " + mime);
//                    if (mime.equals("application/rss+xml") || mime.equals("application/atom+xml")
//                            || mime.equals("text/xml") || mime.equals("application/xml")) {
                    if (mime.contains("xml")) {
                        Log.i(TAG, urls[i] + ": URL valid!");
                        return true;
                    }
                }
                Log.w(TAG, urls[i] + ": URL invalid!");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, urls[i] + ": URL invalid!");
            } catch (ClientProtocolException e) {
                Log.w(TAG, urls[i] + ": Protocol unknown!");
            } catch (UnknownHostException e) {
                Log.w(TAG, urls[i] + ": Host unknown!");
            } catch (SocketTimeoutException e) {
                Log.w(TAG, urls[i] + ": Connection Timeout!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        listener.taskFinished(this, result);
    }

    /**
     * Callback interface with the result of the RSS URL check
     * 
     * @author Jun Chen, jambit GmbH
     */
    public interface NewsRssUrlCheckTaskListener {

        /**
         * Called when the task is finished.
         * 
         * @param finishedTask
         *            the finished task
         * @param result
         *            true if it was a valid RSS URL, false otherwise
         */
        public void taskFinished(NewsRssUrlCheckTask finishedTask, boolean result);

    }
}
