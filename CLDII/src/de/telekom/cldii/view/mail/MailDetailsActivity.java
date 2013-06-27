package de.telekom.cldii.view.mail;

import static de.telekom.cldii.ApplicationConstants.STARTSYNCING;
import static de.telekom.cldii.ApplicationConstants.BASICINFOSYNCED;
import static de.telekom.cldii.ApplicationConstants.CONTACTS_SYNCED_ACTION;
import static de.telekom.cldii.ApplicationConstants.EMAILSSYNCED;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_MAILID;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_SMSNUMBER;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
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
import android.text.method.LinkMovementMethod;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.telekom.cldii.R;
import de.telekom.cldii.controller.IControllerManager;
import de.telekom.cldii.controller.mail.IMailController;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.mail.ICompactMail;
import de.telekom.cldii.data.mail.IFullMail;
import de.telekom.cldii.data.mail.IMailDataProvider.IMailLoadingListener;
import de.telekom.cldii.data.mail.impl.ReceivedDateComparator;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelMailDetails;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.sms.SmsComposeActivity;
import de.telekom.cldii.view.util.CallSystemIntent;
import de.telekom.cldii.view.util.FlingGestureDetector;
import de.telekom.cldii.view.util.OnFlingListener;
import de.telekom.cldii.widget.DetailSeekBar;
import de.telekom.cldii.widget.DetailSeekBar.SeekBarAdapter;

/**
 * Mail detail activity to show more detailed information of a mail
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class MailDetailsActivity extends AbstractActivity implements OnFlingListener, OnSeekBarChangeListener,
        IMailLoadingListener {
    private final String TAG = "MailDetailsActivity";
    private int currentIndex;
    private DetailSeekBar seekBar;
    private Button prevButton;
    private Button nextButton;
    private List<ICompactMail> mailList;
    private Contact currentContact;
    private String mailId;
    private boolean isTracking;

    private ProgressBar syncSpinner;

    private GestureDetector flingDetector;
    private View.OnTouchListener gestureListener;

    private BroadcastReceiver contactSyncReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("type")) {
                String type = intent.getExtras().getString("type");

                if (type.equals(BASICINFOSYNCED)) {
                    Log.i(TAG, "contactSyncReceiver: BASICINFOSYNCED");
                } else if (type.equals(EMAILSSYNCED)) {
                    Log.i(TAG, "contactSyncReceiver: EMAILSSYNCED");
                    syncSpinner.setVisibility(ProgressBar.GONE);
                } else if (type.equals(STARTSYNCING)) {
                    Log.i(TAG, "contactSyncReceiver: STARTSYNCING");
                    syncSpinner.setVisibility(ProgressBar.VISIBLE);
                }

                getDetailMailItem(mailId);
            }
        }
    };

    private SeekBarAdapter seekBarAdapter = new SeekBarAdapter() {

        @Override
        public void onItemSelected(int position) {
            getDetailMailItem(mailList.get(position).getId());
            // Reset scroll position
            ((ScrollView) findViewById(R.id.mailScrollView)).scrollTo(0, 0);
        }

        @Override
        public String getItem(int position) {

            if (position < mailList.size()) {
                String fromAddress = mailList.get(position).getFromAdress();
                Contact contact = getDataProviderManager().getContactDataProvider().getContactForEmail(fromAddress);
                if (contact != null)
                    fromAddress = contact.getDisplayName();
                return fromAddress + "<br /><br /><b>" + mailList.get(position).getSubject() + "</b>";
            } else
                return "No Mails";
        }

        @Override
        public int getCount() {
            return mailList.size();
        }
    };

    public SeekBarAdapter getSeekBarAdapter() {
        return seekBarAdapter;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_details);
        setTopBarName(getString(R.string.section_email));
        syncSpinner = (ProgressBar) findViewById(R.id.syncSpinner);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRAS_KEY_MAILID)) {
            this.mailId = extras.getString(EXTRAS_KEY_MAILID);
        } else {
            Log.e(TAG, "No mail id given.");
            finish();
        }

        this.mailList = new ArrayList<ICompactMail>();
        this.mailList.addAll(getDataProviderManager().getMailDataProvider().getIncommingMails(
                new ReceivedDateComparator()));

        LinearLayout seekBarCombinedLayout = (LinearLayout) findViewById(R.id.seekBarCombinedLayout);
        LinearLayout seekBarLayout = (LinearLayout) seekBarCombinedLayout.findViewById(R.id.seekBarLayout);
        seekBar = (DetailSeekBar) seekBarLayout.findViewById(R.id.seekBar);
        seekBar.setAdapter(seekBarAdapter);
        seekBar.setOnSeekBarChangeListener(this);

        initButtonListeners();

        getDetailMailItem(mailId);

        // Gesture detection
        flingDetector = new GestureDetector(new FlingGestureDetector(this));
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return flingDetector.onTouchEvent(event);
            }
        };

        findViewById(R.id.mailScrollView).setOnTouchListener(gestureListener);
        findViewById(R.id.contentTextView).setOnTouchListener(gestureListener);

    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(contactSyncReceiver, new IntentFilter(CONTACTS_SYNCED_ACTION));

        if (getDataProviderManager().getContactDataProvider().isEmailSyncRunning()) {
            syncSpinner.setVisibility(ProgressBar.VISIBLE);
        } else {
            syncSpinner.setVisibility(ProgressBar.GONE);
        }

        getDetailMailItem(this.mailId);
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
        if (currentContact == null || !currentContact.hasPhone()) {
            menu.findItem(R.id.replyViaSms).setEnabled(false);
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
            CallSystemIntent.callEditContactIntent(MailDetailsActivity.this,
                    currentContact != null ? Long.valueOf(currentContact.getId()) : -1, "mailto",
                    mailList.get(this.currentIndex).getFromAdress(), R.id.requestCode_pickContact);
            break;
        case R.id.replyViaSms:
            replyViaSms();
            break;
        case R.id.replyViaMail:
            replyViaMail();
            break;
        case R.id.smsDelete:
            deleteMail();
            break;
        default:
            showPrompt(getString(R.string.notavailable));
            break;
        }
        return true;
    }

    public IMailController getMailController() {
        return ((IControllerManager) getApplication()).getMailController();
    }

    public void updateSeekbarAdapter() {
        seekBar.setAdapter(seekBarAdapter);
    }

    private void deleteMail() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MailDetailsActivity.this);
        builder.setMessage(String.format(getString(R.string.confirm_delete_mail))).setCancelable(true)
                .setPositiveButton(getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getMailController().deleteMail(mailList.get(currentIndex));

                        if ((mailList.size() - 1) == 0) {
                            finish();
                        } else if (currentIndex >= mailList.size() - 1)
                            prevButtonClicked();
                        else
                            nextButtonClicked();

                        mailList.remove(currentIndex);
                        seekBar.setAdapter(seekBarAdapter);

                        dialog.dismiss();
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

        if (requestCode == R.id.requestCode_pickContact) {
            if (getDataProviderManager().getContactDataProvider().isEmailSyncRunning()) {
                syncSpinner.setVisibility(ProgressBar.VISIBLE);
            } else {
                syncSpinner.setVisibility(ProgressBar.GONE);
            }

            // getDetailMailItem(this.mailId);
        }
    }

    /* Private method to initialize buttons that are always valid */
    private void initButtonListeners() {
        Button deleteButton = (Button) findViewById(R.id.mailDelete);
        deleteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteMail();
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

        Button callButton = (Button) findViewById(R.id.mailCall);
        Button replyButton = (Button) findViewById(R.id.mailReply);
        replyButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                replyViaMail();
            }
        });

        replyButton.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                List<String> itemList = new ArrayList<String>();
                itemList.add(getString(R.string.sms_replyViaMail));
                if (currentContact != null && currentContact.hasPhone()) {
                    itemList.add(getString(R.string.sms_replyViaSms));
                }
                final CharSequence[] items = itemList.toArray(new CharSequence[] {});
                AlertDialog.Builder builder = new AlertDialog.Builder(MailDetailsActivity.this);
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
                String number = getDataProviderManager().getContactDataProvider().getDefaultPhoneForContact(
                        currentContact);
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                startActivity(callIntent);
            }
        });

    }

    public void prevButtonClicked() {
        if (currentIndex > 0) {
            getDetailMailItem(mailList.get(currentIndex - 1).getId());
        }
    }

    public void nextButtonClicked() {
        if (currentIndex < mailList.size() - 1) {
            getDetailMailItem(mailList.get(currentIndex + 1).getId());
        }
    }

    /**
     * Fills the layout with mail data for a given identifier
     * 
     * @param id
     *            identifier
     */
    public void getDetailMailItem(String id) {
        this.mailId = id;
        getDataProviderManager().getMailDataProvider().getFullMail(mailId, this);
        ((TextView) findViewById(R.id.contentTextView)).setText("");

        for (int i = 0; i < mailList.size(); i++) {
            if (mailList.get(i).getId().equals(mailId)) {
                this.currentIndex = i;

                if (!isTracking)
                    this.seekBar.setProgress((int) (2 * currentIndex + 1) / 2);

                this.prevButton.setEnabled(true);
                this.nextButton.setEnabled(true);
                if (currentIndex == 0) {
                    this.prevButton.setEnabled(false);
                } else if (currentIndex == mailList.size() - 1) {
                    this.nextButton.setEnabled(false);
                }
                break;
            }
        }

        // Adapt buttons whether the number is valid or not
        Button callButton = (Button) findViewById(R.id.mailCall);

        this.currentContact = getDataProviderManager().getContactDataProvider().getContactForEmail(
                mailList.get(this.currentIndex).getFromAdress());
        String number = "";
        if (currentContact != null)
            number = getDataProviderManager().getContactDataProvider().getDefaultPhoneForContact(this.currentContact);

        if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
            Log.d(TAG, number + " is valid!");

            callButton.setEnabled(true);
            callButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_call_plain, 0, 0);
            callButton.setTextColor(Color.WHITE);
        } else {
            TypedArray styled = obtainStyledAttributes(new int[] { R.attr.detail_plates_inactive });

            callButton.setEnabled(false);
            callButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_call_plain_inactive, 0, 0);
            callButton.setTextColor(styled.getColor(0, 0));
            styled.recycle();
        }

        ICompactMail mailItem = mailList.get(currentIndex);
        getMailController().changeReadState(mailItem, true);

        String mailDate = "";
        String mailTime = "";
        try {
            mailDate = DateFormat.getDateInstance(DateFormat.DEFAULT).format(mailItem.getRecievedDate());
            mailTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(mailItem.getRecievedDate());

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        View layout = findViewById(R.id.mailTitleLayout);
        TextView contactNameTextView = (TextView) layout.findViewById(R.id.senderTextView);

        ((TextView) findViewById(R.id.dateTextView)).setText(mailDate);
        ((TextView) findViewById(R.id.timeTextView)).setText(mailTime);
        ((TextView) findViewById(R.id.subjectTextView)).setText(mailItem.getSubject());

        if (mailItem.answered())
            findViewById(R.id.detailReplyImage).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.detailReplyImage).setVisibility(View.GONE);

        if (contactNameTextView != null) {
            if (this.currentContact != null)
                contactNameTextView.setText(this.currentContact.getDisplayName());
            else
                contactNameTextView.setText(mailItem.getFromAdress());
        }
    }

    public List<ICompactMail> getMailList() {
        return mailList;
    }

    private void replyViaSms() {
        if (this.currentContact != null) {
            Intent replyIntent = new Intent(MailDetailsActivity.this, SmsComposeActivity.class);
            replyIntent.putExtra(EXTRAS_KEY_SMSNUMBER, getDataProviderManager().getContactDataProvider()
                    .getDefaultPhoneForContact(this.currentContact));
            startActivity(replyIntent);
        }
    }

    private void replyViaMail() {
        Intent replyIntent = new Intent(MailDetailsActivity.this, MailComposeActivity.class);
        replyIntent.putExtra(EXTRAS_KEY_MAILID, mailList.get(currentIndex).getId());
        startActivity(replyIntent);
    }

    @Override
    protected void initGestureLibrary() {
        gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.cldii_gestures);
        gestureLibrary.load();
    }

    @Override
    public StateModel getStateModel() {
        return new StateModelMailDetails(MailDetailsActivity.this, getDataProviderManager());
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

    public int getCurrentIndex() {
        return currentIndex;
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
    public void onMailLoaded(IFullMail loadedMail) {
        TextView content = ((TextView) findViewById(R.id.contentTextView));
        String mailText = loadedMail.getText();
        if (mailText.length() > 0) {
            content.setText(Html.fromHtml(mailText));
            content.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

}
