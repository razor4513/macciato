package de.telekom.cldii.view.sms;

import static de.telekom.cldii.ApplicationConstants.STARTSYNCING;
import static de.telekom.cldii.ApplicationConstants.BASICINFOSYNCED;
import static de.telekom.cldii.ApplicationConstants.CONTACTS_SYNCED_ACTION;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_SMSID;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_SMSNUMBER;
import static de.telekom.cldii.ApplicationConstants.SMSSYNCED;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.gesture.GestureLibraries;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import de.telekom.cldii.R;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.sms.SmsItem;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelSms;
import de.telekom.cldii.statemachine.states.StateModelSms.SmsModelType;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.sms.adapter.SmsListAdapter;
import de.telekom.cldii.view.util.CallSystemIntent;

/**
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 */

public class SmsListActivity extends AbstractActivity implements RefreshListDataTaskOnFinishListener {
    private static final String TAG = "SmsListActivity";
    private static final String SMS_CONTENTURI = "content://sms/";

    SmsContentObserver contentObserver = new SmsContentObserver();
    private boolean smsReceiverRegistered;

    private ProgressDialog progress;

    private ProgressBar syncSpinner;

    /**
     * Enum for order in sms list element context menu.
     */
    private static enum ContextMenu {
        ADDEDITCONTACT, DELETE, MARKUNREAD, FORWARD, REPLY
    }

    private ListView smsListView;

    private BroadcastReceiver contactSyncReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("type")) {
                String type = intent.getExtras().getString("type");

                if (type.equals(BASICINFOSYNCED)) {
                    Log.i(TAG, "contactSyncReceiver: BASICINFOSYNCED");
                } else if (type.equals(SMSSYNCED)) {
                    Log.i(TAG, "contactSyncReceiver: SMSSYNCED");
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_list);
        setTopBarName(getString(R.string.section_sms));

        syncSpinner = (ProgressBar) findViewById(R.id.syncSpinner);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.smsListLayout);
        smsListView = (ListView) layout.findViewById(R.id.smslist);

        findViewById(R.id.topBarName).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                smsListView.setSelection(0);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(contactSyncReceiver, new IntentFilter(CONTACTS_SYNCED_ACTION));
        Uri uri = Uri.parse(SMS_CONTENTURI);
        getContentResolver().registerContentObserver(uri, true, contentObserver);
        this.smsReceiverRegistered = true;

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }

        if (getDataProviderManager().getContactDataProvider().isSmsSyncRunning()) {
            syncSpinner.setVisibility(ProgressBar.VISIBLE);
        } else {
            syncSpinner.setVisibility(ProgressBar.GONE);
        }

        refreshListData();
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(contactSyncReceiver);

        if (this.smsReceiverRegistered) {
            // unregisterReceiver(smsReceiver);
            getContentResolver().unregisterContentObserver(contentObserver);
            smsReceiverRegistered = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sms_list, menu);
        setMenuBackground();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.newSms:
            Intent intent = new Intent(SmsListActivity.this, SmsComposeActivity.class);
            startActivity(intent);
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * If an adapter is set for the smsList, notifyDataSetChanged() is called.
     * If no adapter is set, a new SmsListAdapter will be set.
     * 
     * @param force
     *            Force setting a new Adapter (don't notifyDataSetChanged)
     */
    private void updateListView() {
        if (smsListView.getAdapter() == null) {
            Log.d(TAG, "updateListView: set new adapter");
            smsListView.setAdapter(new SmsListAdapter(SmsListActivity.this, getDataProviderManager(),
                    getLayoutInflater()));
            smsListView.setEmptyView(findViewById(R.id.nosms));
            smsListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int index, long itemId) {
                    if (itemId != -1) {
                        Intent smsDetails = new Intent(SmsListActivity.this, SmsDetailsActivity.class);
                        smsDetails.putExtra(EXTRAS_KEY_SMSID, Long.valueOf(itemId));
                        startActivityForResult(smsDetails, R.id.requestCode_sms_changeInDetailView);
                    }
                }
            });

            smsListView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int index, long itemId) {
                    return showItemLongClickMenu(arg0, itemId, index);
                }
            });
        } else {
            Log.d(TAG, "updateListView: notifyDataSetChanged");
            ((SmsListAdapter) smsListView.getAdapter()).clearImageCache();
            ((SmsListAdapter) smsListView.getAdapter()).notifyDataSetChanged();
        }

    }

    private boolean showItemLongClickMenu(AdapterView<?> adapter, final long smsId, final int position) {
        if (!(adapter.getItemAtPosition(position) instanceof SmsItem))
            return false;
        final SmsItem smsItem = ((SmsItem) smsListView.getAdapter().getItem(position));
        final Contact contact = getDataProviderManager().getContactDataProvider().getContactForPhoneNumber(
                smsItem.getSenderPhoneNumber());
        final CharSequence[] items = new CharSequence[ContextMenu.values().length];
        if (contact == null) {
            items[ContextMenu.ADDEDITCONTACT.ordinal()] = getString(R.string.contextmenu_addcontact);
        } else {
            items[ContextMenu.ADDEDITCONTACT.ordinal()] = getString(R.string.contextmenu_editcontact);
        }

        String readItem = getString(R.string.contextmenu_markunread);
        if (!smsItem.isRead())
            readItem = getString(R.string.contextmenu_markread);

        items[ContextMenu.DELETE.ordinal()] = getString(R.string.contextmenu_delete);
        items[ContextMenu.MARKUNREAD.ordinal()] = readItem;
        items[ContextMenu.FORWARD.ordinal()] = getString(R.string.contextmenu_forward);
        items[ContextMenu.REPLY.ordinal()] = getString(R.string.contextmenu_reply);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_options));
        builder.setCancelable(true);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                ContextMenu selectedMenu = ContextMenu.values()[item];

                switch (selectedMenu) {
                case ADDEDITCONTACT:
                    CallSystemIntent.callEditContactIntent(SmsListActivity.this,
                            contact != null ? Long.valueOf(contact.getId()) : -1, "tel",
                            ((SmsItem) smsListView.getItemAtPosition(position)).getSenderPhoneNumber(),
                            R.id.requestCode_sms_showOrCreateContactResult);
                    break;

                case DELETE:
                    AlertDialog.Builder builder = new AlertDialog.Builder(SmsListActivity.this);
                    builder.setMessage(String.format(getString(R.string.confirm_delete_sms))).setCancelable(true)
                            .setPositiveButton(getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    deleteSms(smsId, position);
                                    // dialog.cancel();
                                }
                            }).setNegativeButton(getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    break;

                case MARKUNREAD:
                    if (smsItem.isRead()) {
                        getDataProviderManager().getSmsDataProvider().setSmsUnread(smsId);
                        refreshListData();
                    } else {
                        getDataProviderManager().getSmsDataProvider().setSmsRead(smsId);
                        refreshListData();
                    }
                    break;

                case FORWARD:
                    Intent forwardIntent = new Intent(SmsListActivity.this, SmsComposeActivity.class);
                    forwardIntent.putExtra(de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_SMSCONTENT,
                            ((SmsItem) smsListView.getItemAtPosition(position)).getContent());
                    startActivity(forwardIntent);
                    break;

                case REPLY:
                    Intent replyIntent = new Intent(SmsListActivity.this, SmsComposeActivity.class);
                    replyIntent.putExtra(EXTRAS_KEY_SMSNUMBER,
                            ((SmsItem) smsListView.getItemAtPosition(position)).getSenderPhoneNumber());
                    startActivity(replyIntent);
                    break;

                default:
                    showPrompt(getString(R.string.notavailable));
                    break;
                }
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
        return true;
    }

    private void deleteSms(long smsId, int position) {
        getDataProviderManager().getSmsDataProvider().deleteSms(smsId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == R.id.requestCode_sms_showOrCreateContactResult) {
            ((SmsListAdapter) smsListView.getAdapter()).clearImageCache();
            if (getDataProviderManager().getContactDataProvider().isSmsSyncRunning()) {
                syncSpinner.setVisibility(ProgressBar.VISIBLE);
            } else {
                syncSpinner.setVisibility(ProgressBar.GONE);
            }
        }
    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();
    }

    @Override
    public StateModel getStateModel() {
        return new StateModelSms(SmsListActivity.this, getDataProviderManager(), SmsModelType.LIST);
    }

    public ListView getSmsListView() {
        return smsListView;
    }

    private void refreshListData() {
        progress = new ProgressDialog(SmsListActivity.this);
        progress.setMessage(getString(R.string.dialog_loading_sms));
        progress.show();
        new RefreshListDataTask(SmsListActivity.this, getDataProviderManager().getSmsDataProvider())
                .execute((Void) null);
    }

    private class SmsContentObserver extends ContentObserver {

        public SmsContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(TAG, "Sms content changed.");

            refreshListData();
        }
    }

    @Override
    public void refreshListCompleted() {
        updateListView();
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }
}