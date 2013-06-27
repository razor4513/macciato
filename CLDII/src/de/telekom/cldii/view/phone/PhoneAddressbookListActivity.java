package de.telekom.cldii.view.phone;

import static de.telekom.cldii.ApplicationConstants.BASICINFOSYNCED;
import static de.telekom.cldii.ApplicationConstants.CONTACTS_SYNCED_ACTION;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_CONTACT_ID;
import static de.telekom.cldii.ApplicationConstants.STARTSYNCING;

import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.GestureLibraries;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.telekom.cldii.CldApplication;
import de.telekom.cldii.R;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.contact.Contact.Phone;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelPhoneAddressbook;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.phone.adapter.PhoneAddressbookListAdapter;
import de.telekom.cldii.view.util.CallSystemIntent;

/**
 * This activity displays a list of favorite/starred contacts
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 */

public class PhoneAddressbookListActivity extends AbstractActivity {
    private final String TAG = "PhoneAddressbookListActivity";
    private ListView addressListView;

    private ProgressBar syncSpinner;

    private BroadcastReceiver contactSyncReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("type")) {
                String type = intent.getExtras().getString("type");

                if (type.equals(BASICINFOSYNCED)) {
                    Log.i(TAG, "contactSyncReceiver: BASICINFOSYNCED");
                    syncSpinner.setVisibility(ProgressBar.GONE);
                } else if (type.equals(STARTSYNCING)) {
                    Log.i(TAG, "contactSyncReceiver: STARTSYNCING");
                    syncSpinner.setVisibility(ProgressBar.VISIBLE);
                }

                updateListView();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_list);
        setTopBarName(getString(R.string.phone_section_phonecontacts));
        syncSpinner = (ProgressBar) findViewById(R.id.syncSpinner);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.contactListLayout);
        addressListView = (ListView) layout.findViewById(R.id.contactlist);

        findViewById(R.id.topBarName).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                addressListView.setSelection(0);
            }
        });

        View inputView = layout.findViewById(R.id.contactListInputBar);
        final EditText filterEditText = (EditText) inputView.findViewById(R.id.contactFilterEditText);
        filterEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                ((PhoneAddressbookListAdapter) addressListView.getAdapter()).setFilterString(filterEditText.getText()
                        .toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        showBottomBar();
        initButtonListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(contactSyncReceiver, new IntentFilter(CONTACTS_SYNCED_ACTION));

        if (getDataProviderManager().getContactDataProvider().isBasicInfoSyncRunning()) {
            syncSpinner.setVisibility(ProgressBar.VISIBLE);
        } else {
            syncSpinner.setVisibility(ProgressBar.GONE);
        }

        updateListView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(contactSyncReceiver);
    }

    /**
     * Resets the input in the filter EditText. Called from layout xml, so it
     * has to be public.
     * 
     * @param view
     */
    public void clearFilter(View view) {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.contactListLayout);
        final EditText filterEditText = (EditText) layout.findViewById(R.id.contactFilterEditText);
        filterEditText.setText("");
    }

    /**
     * If an adapter is set for the smsList, notifyDataSetChanged() is called.
     * If no adapter is set, a new SmsListAdapter will be set.
     * 
     * @param force
     *            Force setting a new Adapter (don't notifyDataSetChanged)
     */
    private void updateListView() {
        if (addressListView.getAdapter() == null) {
            Log.d(TAG, "updateListView: set new adapter");
            addressListView.setAdapter(new PhoneAddressbookListAdapter(getApplicationContext(),
                    getDataProviderManager(), getLayoutInflater()));
            addressListView.setEmptyView(findViewById(R.id.nocontacts));
            addressListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long itemId) {
                    if (itemId != -1) {
                        Contact contact = ((IDataProviderManager) getApplication()).getContactDataProvider()
                                .getContactForId(itemId);
                        List<Phone> phoneList = ((IDataProviderManager) getApplication()).getContactDataProvider()
                                .getPhoneForContact(contact);

                        if (phoneList.size() > 1) {
                            Intent contactDetailsIntent = new Intent(PhoneAddressbookListActivity.this,
                                    PhoneAddressbookDetailsActivity.class);
                            contactDetailsIntent.putExtra(EXTRAS_KEY_CONTACT_ID, Long.valueOf(itemId));
                            startActivity(contactDetailsIntent);
                        } else if (phoneList.size() == 1) {
                            callPhoneNumber(phoneList.get(0).getNumber());
                        }
                    }
                }
            });

            addressListView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int index, final long itemId) {
                    if (itemId == -1)
                        return false;
                    final Contact contact = ((IDataProviderManager) getApplication()).getContactDataProvider()
                            .getContactForId(itemId);

                    CharSequence[] items = { getString(R.string.contextmenu_editcontact) };
                    AlertDialog.Builder builder = new AlertDialog.Builder(PhoneAddressbookListActivity.this);
                    builder.setCancelable(true);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            switch (item) {
                            case 0:
                                CallSystemIntent.callEditContactIntent(PhoneAddressbookListActivity.this, itemId,
                                        "tel", ((CldApplication) getApplicationContext()).getContactDataProvider()
                                                .getDefaultPhoneForContact(contact), R.id.requestCode_pickContact);
                                break;
                            }
                        }
                    });
                    builder.create().show();
                    return true;
                }
            });
        } else {
            Log.d(TAG, "updateListView: notifyDataSetChanged");
            ((BaseAdapter) addressListView.getAdapter()).notifyDataSetChanged();
        }

    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();
    }

    @Override
    public StateModel getStateModel() {
        return new StateModelPhoneAddressbook(PhoneAddressbookListActivity.this, getDataProviderManager());
    }

    private void showBottomBar() {
        setBottomView(R.layout.global_bottombar);
    }

    private void initButtonListeners() {
        View dialpadButton = findViewById(R.id.leftButtonLayout);
        dialpadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialpadButtonClicked();
            }
        });

        View favoriteButton = findViewById(R.id.rightButtonLayout);
        favoriteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                favoritesButtonClicked();
            }
        });

        TextView favoriteTextView = (TextView) findViewById(R.id.rightButton);
        favoriteTextView.setText(getString(R.string.button_favorites));

    }

    private void dialpadButtonClicked() {
        Intent dial = new Intent();
        dial.setAction("android.intent.action.DIAL");
        // dial.setData(Uri.parse("tel:"+dial_number));
        startActivity(dial);
    }

    private void favoritesButtonClicked() {
        finish();
    }

    private void callPhoneNumber(String phoneNumber) {
        if (PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == R.id.requestCode_pickContact) {
            if (getDataProviderManager().getContactDataProvider().isBasicInfoSyncRunning()) {
                syncSpinner.setVisibility(ProgressBar.VISIBLE);
            } else {
                syncSpinner.setVisibility(ProgressBar.GONE);
            }

            // updateListView();
        }
    }
}
