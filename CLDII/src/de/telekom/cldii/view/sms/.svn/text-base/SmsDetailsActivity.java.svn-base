package de.telekom.cldii.view.sms;

import static de.telekom.cldii.ApplicationConstants.BASICINFOSYNCED;
import static de.telekom.cldii.ApplicationConstants.CONTACTS_SYNCED_ACTION;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_MAILRECIPIENT;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_SMSID;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_SMSNUMBER;
import static de.telekom.cldii.ApplicationConstants.SMSSYNCED;
import static de.telekom.cldii.ApplicationConstants.STARTSYNCING;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.gesture.GestureLibraries;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.contact.Contact.Email;
import de.telekom.cldii.data.sms.SmsItem;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelSms;
import de.telekom.cldii.statemachine.states.StateModelSms.SmsModelType;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.mail.MailComposeActivity;
import de.telekom.cldii.view.util.CallSystemIntent;
import de.telekom.cldii.view.util.FlingGestureDetector;
import de.telekom.cldii.view.util.OnFlingListener;
import de.telekom.cldii.widget.DetailSeekBar;
import de.telekom.cldii.widget.DetailSeekBar.SeekBarAdapter;

/**
 * Sms detail activity to show more detailed information of a sms
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class SmsDetailsActivity extends AbstractActivity implements RefreshListDataTaskOnFinishListener,
        OnFlingListener, OnSeekBarChangeListener {
    private final String TAG = "SmsDetailsActivity";
    private String smsContent;
    private long smsId;
    private long incomingCategoryId;
    private int currentSmsIndex;
    private DetailSeekBar seekBar;
    private Button prevButton;
    private Button nextButton;
    private boolean isTracking;

    private ProgressBar syncSpinner;
    private ProgressDialog pdContacts;

    private Contact currentContact;
    private ProgressDialog progress;

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
                getDetailSmsItem(smsId);
            }
        }
    };

    private GestureDetector flingDetector;
    private View.OnTouchListener gestureListener;
    private SeekBarAdapter seekBarAdapter = new SeekBarAdapter() {

        @Override
        public void onItemSelected(int position) {
            getDetailSmsItem(getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().get(position)
                    .getSmsId());
            // Reset scroll position
            ((ScrollView) findViewById(R.id.smsScrollView)).scrollTo(0, 0);
        }

        @Override
        public String getItem(int position) {
            if (position < getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().size()) {
                return getContactNameForPhoneNumber(getDataProviderManager().getSmsDataProvider()
                        .getSmsItemsOrderedByDate().get(position).getSenderPhoneNumber())
                        + "<br /><br /><b>"
                        + getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().get(position)
                                .getContent() + "</b>";
            } else
                return "No Sms";
        }

        @Override
        public int getCount() {
            return getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().size();
        }
    };

    public void updateSeekbarAdapter() {
        seekBar.setAdapter(seekBarAdapter);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_details);
        setTopBarName(getString(R.string.section_sms));

        syncSpinner = (ProgressBar) findViewById(R.id.syncSpinner);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRAS_KEY_SMSID)) {
            this.smsId = extras.getLong(EXTRAS_KEY_SMSID);

            Log.d(TAG, "smsId: " + smsId);
        } else {
            Log.e(TAG, "smsId doesn't exist.");
            finish();
        }

        View seekBarCombinedLayout = findViewById(R.id.seekBarCombinedLayout);
        View seekBarLayout = seekBarCombinedLayout.findViewById(R.id.seekBarLayout);
        seekBar = (DetailSeekBar) seekBarLayout.findViewById(R.id.seekBar);
        seekBar.setAdapter(seekBarAdapter);
        seekBar.setOnSeekBarChangeListener(this);

        initButtonListeners();

        // Gesture detection
        flingDetector = new GestureDetector(new FlingGestureDetector(this));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return flingDetector.onTouchEvent(event);
            }
        };

        findViewById(R.id.smsScrollView).setOnTouchListener(gestureListener);
        findViewById(R.id.contentTextView).setOnTouchListener(gestureListener);

        pdContacts = new ProgressDialog(SmsDetailsActivity.this);
        pdContacts.setMessage(getString(R.string.dialog_loading_favs));
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(contactSyncReceiver, new IntentFilter(CONTACTS_SYNCED_ACTION));

        if (getDataProviderManager().getContactDataProvider().isSmsSyncRunning()) {
            syncSpinner.setVisibility(ProgressBar.VISIBLE);
        } else {
            syncSpinner.setVisibility(ProgressBar.GONE);
        }

        getDetailSmsItem(this.smsId);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(contactSyncReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.currentContact == null) {
            MenuItem item = menu.findItem(R.id.editOrAddContact);
            item.setTitle(R.string.contextmenu_addcontact);
        }
        List<Email> emails = getDataProviderManager().getContactDataProvider().getEmailForContact(currentContact);
        if (emails == null || emails.size() < 1) {
            menu.findItem(R.id.replyViaMail).setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionMenu = menu;

        // Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sms_detail, menu);
        setMenuBackground();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.editOrAddContact:
            CallSystemIntent.callEditContactIntent(SmsDetailsActivity.this,
                    currentContact != null ? Long.valueOf(currentContact.getId()) : -1, "tel", getDataProviderManager()
                            .getSmsDataProvider().getSmsItemsOrderedByDate().get(currentSmsIndex)
                            .getSenderPhoneNumber(), R.id.requestCode_sms_showOrCreateContactResult);
            break;
        case R.id.replyViaSms:
            replyViaSms();
            break;
        case R.id.replyViaMail:
            replyViaMail();
            break;
        case R.id.smsDelete:
            deleteSms();
            break;
        default:
            showPrompt(getString(R.string.notavailable));
            break;
        }
        return true;
    }

    private void deleteSms() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SmsDetailsActivity.this);
        builder.setMessage(String.format(getString(R.string.confirm_delete_sms))).setCancelable(true)
                .setPositiveButton(getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (getDataProviderManager().getSmsDataProvider().deleteSms(smsId)) {
                            if (getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().size() - 1 == 0) {
                                finish();
                            } else {
                                progress = new ProgressDialog(SmsDetailsActivity.this);
                                progress.setMessage(getString(R.string.dialog_loading_sms));
                                progress.show();

                                if (currentSmsIndex >= getDataProviderManager().getSmsDataProvider()
                                        .getSmsItemsOrderedByDate().size() - 1)
                                    smsId = getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate()
                                            .get(currentSmsIndex - 1).getSmsId();
                                else
                                    smsId = getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate()
                                            .get(currentSmsIndex + 1).getSmsId();

                                new RefreshListDataTask(SmsDetailsActivity.this, getDataProviderManager()
                                        .getSmsDataProvider()).execute((Void) null);
                            }
                        }
                    }
                }).setNegativeButton(getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == R.id.requestCode_sms_showOrCreateContactResult) {

            if (getDataProviderManager().getContactDataProvider().isSmsSyncRunning()) {
                syncSpinner.setVisibility(ProgressBar.VISIBLE);
            } else {
                syncSpinner.setVisibility(ProgressBar.GONE);
            }

            // getDetailSmsItem(this.smsId);
        }
    }

    /* Private method to initialize buttons that are always valid */
    private void initButtonListeners() {
        Button deleteButton = (Button) findViewById(R.id.smsDelete);
        deleteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteSms();
            }
        });

        prevButton = (Button) findViewById(R.id.prevButton);
        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                prevButtonClicked();
            }
        });

        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButtonClicked();
            }
        });

        Button callButton = (Button) findViewById(R.id.smsCall);
        Button replyButton = (Button) findViewById(R.id.smsReply);
        replyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                replyViaSms();
            }
        });

        replyButton.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                List<String> itemList = new ArrayList<String>();
                itemList.add(getString(R.string.sms_replyViaSms));
                List<Email> emails = getDataProviderManager().getContactDataProvider().getEmailForContact(
                        currentContact);
                if (emails != null && emails.size() > 0) {
                    itemList.add(getString(R.string.sms_replyViaMail));
                }
                final CharSequence[] items = itemList.toArray(new CharSequence[] {});

                AlertDialog.Builder builder = new AlertDialog.Builder(SmsDetailsActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].toString().equals(getString(R.string.sms_replyViaSms))) {
                            replyViaSms();
                        } else if (items[item].toString().equals(getString(R.string.sms_replyViaMail))) {
                            replyViaMail();
                        }
                    }
                });
                builder.create().show();
                return true;
            }
        });

        callButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String number = getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate()
                        .get(currentSmsIndex).getSenderPhoneNumber();
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                startActivity(callIntent);
            }
        });

    }

    public void prevButtonClicked() {
        if (currentSmsIndex > 0) {
            getDetailSmsItem(getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate()
                    .get(currentSmsIndex - 1).getSmsId());
        }
    }

    public void nextButtonClicked() {
        if (currentSmsIndex < getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().size() - 1) {
            getDetailSmsItem(getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate()
                    .get(currentSmsIndex + 1).getSmsId());
        }
    }

    /**
     * Fills the layout with news item data for a given newsId
     * 
     * @param newsId
     *            news identifier
     */
    public void getDetailSmsItem(long smsId) {
        this.smsId = smsId;

        for (int i = 0; i < getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().size(); i++) {
            if (getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().get(i).getSmsId() == smsId) {
                this.currentSmsIndex = i;

                if (!isTracking)
                    this.seekBar.setProgress((int) (2 * currentSmsIndex + 1) / 2);

                this.prevButton.setEnabled(true);
                this.nextButton.setEnabled(true);
                if (currentSmsIndex == 0) {
                    this.prevButton.setEnabled(false);
                } else if (currentSmsIndex == getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate()
                        .size() - 1) {
                    this.nextButton.setEnabled(false);
                }
                break;
            }
        }

        // Adapt buttons whether the number is valid or not
        Button callButton = (Button) findViewById(R.id.smsCall);
        Button replyButton = (Button) findViewById(R.id.smsReply);

        String number = getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().get(currentSmsIndex)
                .getSenderPhoneNumber();
        this.currentContact = getDataProviderManager().getContactDataProvider().getContactForPhoneNumber(number);
        number = number.replaceAll("[^\\+0-9]", "");
        if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
            Log.d(TAG, number + " is valid!");

            TypedArray styled = obtainStyledAttributes(new int[] { R.attr.detail_plates_active });
            replyButton.setEnabled(true);
            replyButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_reply, 0, 0);
            replyButton.setTextColor(styled.getColor(0, 0));

            callButton.setEnabled(true);
            callButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_call_plain, 0, 0);
            callButton.setTextColor(Color.WHITE);
            styled.recycle();

        } else {
            TypedArray styled = obtainStyledAttributes(new int[] { R.attr.detail_plates_inactive });

            replyButton.setEnabled(false);
            replyButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_reply_inactive, 0, 0);
            replyButton.setTextColor(styled.getColor(0, 0));

            callButton.setEnabled(false);
            callButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_call_plain_inactive, 0, 0);
            callButton.setTextColor(styled.getColor(0, 0));
            styled.recycle();
        }

        // mark sms as read
        if (!getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().get(currentSmsIndex).isRead()
                && getDataProviderManager().getSmsDataProvider().setSmsRead(smsId)) {
            setResult(R.id.requestCode_sms_changeInDetailView_DidChange);
        }

        SmsItem smsItem = getDataProviderManager().getSmsDataProvider().getSmsItemsOrderedByDate().get(currentSmsIndex);
        smsItem.setRead(true);

        String smsDate = "";
        String smsTime = "";
        try {
            smsDate = DateFormat.getDateInstance(DateFormat.DEFAULT).format(smsItem.getDate());
            smsTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(smsItem.getDate());

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        this.smsContent = smsItem.getContent();

        TextView contactNameTextView = (TextView) findViewById(R.id.senderTextView);

        ((TextView) findViewById(R.id.dateTextView)).setText(smsDate);
        ((TextView) findViewById(R.id.timeTextView)).setText(smsTime);
        ((TextView) findViewById(R.id.contentTextView)).setText(Html.fromHtml(this.smsContent));

        if (contactNameTextView != null) {
            contactNameTextView.setText(getContactNameForPhoneNumber(smsItem.getSenderPhoneNumber()));
        }
    }

    private void replyViaSms() {
        Intent replyIntent = new Intent(SmsDetailsActivity.this, SmsComposeActivity.class);
        replyIntent.putExtra(EXTRAS_KEY_SMSNUMBER, getDataProviderManager().getSmsDataProvider()
                .getSmsItemsOrderedByDate().get(currentSmsIndex).getSenderPhoneNumber());
        startActivity(replyIntent);
    }

    private void replyViaMail() {
        List<Email> emails = getDataProviderManager().getContactDataProvider().getEmailForContact(currentContact);
        if (emails.size() > 0) {
            Intent replyIntent = new Intent(SmsDetailsActivity.this, MailComposeActivity.class);
            replyIntent.putExtra(EXTRAS_KEY_MAILRECIPIENT, emails.get(0).getAddress());
            startActivity(replyIntent);
        }
    }

    private String getContactNameForPhoneNumber(String number) {
        Contact contact = getDataProviderManager().getContactDataProvider().getContactForPhoneNumber(number);
        if (contact != null && contact.getDisplayName().length() > 0) {
            number = contact.getDisplayName();
        }

        return number;
    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();
    }

    @Override
    public StateModel getStateModel() {
        return new StateModelSms(SmsDetailsActivity.this, getDataProviderManager(), SmsModelType.DETAIL);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isTracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isTracking = false;
    }

    public long getIncomingCategoryId() {
        return incomingCategoryId;
    }

    public int getCurrentSmsIndex() {
        return currentSmsIndex;
    }

    @Override
    public void onLeftFling() {
        nextButtonClicked();
    }

    @Override
    public void onRightFling() {
        prevButtonClicked();
    }

    @Override
    public void refreshListCompleted() {
        getDetailSmsItem(smsId);
        seekBar.setAdapter(seekBarAdapter);

        if (progress != null && progress.isShowing())
            progress.dismiss();
    }

}
