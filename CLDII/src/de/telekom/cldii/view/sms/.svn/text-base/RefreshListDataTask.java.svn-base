package de.telekom.cldii.view.sms;

import android.os.AsyncTask;
import de.telekom.cldii.data.sms.ISmsDataProvider;

/**
 * AsynTask responsible of clearing and refreshing Sms data cache
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class RefreshListDataTask extends AsyncTask<Void, Void, Void> {
    private RefreshListDataTaskOnFinishListener listener;
    private ISmsDataProvider smsDataProvider;

    public RefreshListDataTask(RefreshListDataTaskOnFinishListener listener, ISmsDataProvider smsDataProvider) {
        this.listener = listener;
        this.smsDataProvider = smsDataProvider;
    }

    @Override
    protected Void doInBackground(Void... param) {
        smsDataProvider.clearCache();
        smsDataProvider.getSmsItemsOrderedByDate();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.refreshListCompleted();
    }
}

/**
 * Callback interface on completed RefreshListDataTask
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
interface RefreshListDataTaskOnFinishListener {

    /**
     * Callback triggered when RefreshListDataTask is finished
     */
    public void refreshListCompleted();
}