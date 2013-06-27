package de.telekom.cldii.view.sms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.view.sms.adapter.SmsDialogSelectNumberAdapter;

/**
 * 
 * @author Jun Chen, jambit GmbH
 *
 */
public class SmsDialogSelectNumber extends LinearLayout {
    private View layout;
    private Context context;
    private IDataProviderManager dataProviderManager;

    public SmsDialogSelectNumber(Context context, IDataProviderManager dataProviderManager) {
        super(context);
        this.context = context;
        this.dataProviderManager = dataProviderManager;

        inflateLayout();
    }

    public SmsDialogSelectNumber(Context context, IDataProviderManager dataProviderManager, Long categoryId) {
        super(context);
        this.context = context;
        this.dataProviderManager = dataProviderManager;
        inflateLayout();
    }

    /* Private method to inflate the xml */
    private final void inflateLayout() {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.layout = inflater.inflate(R.layout.sms_dialog_selectnumber, null, false);
        this.addView(layout);

        initListeners();
    }

    private void initListeners() {
        final ListView listView = (ListView) this.layout.findViewById(R.id.phoneNumberList);

        listView.setAdapter(new SmsDialogSelectNumberAdapter(dataProviderManager.getContactDataProvider(),
                (Activity) context));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                ((SmsDialogSelectNumberAdapter) arg0.getAdapter()).setChecked(index);
            }
        });

    }
}
