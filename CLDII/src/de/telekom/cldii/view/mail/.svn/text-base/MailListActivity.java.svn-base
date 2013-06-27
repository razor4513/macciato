package de.telekom.cldii.view.mail;

import static de.telekom.cldii.ApplicationConstants.STARTSYNCING;
import static de.telekom.cldii.ApplicationConstants.BASICINFOSYNCED;
import static de.telekom.cldii.ApplicationConstants.CONTACTS_SYNCED_ACTION;
import static de.telekom.cldii.ApplicationConstants.EMAILSSYNCED;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_FLAG_FORWARD;
import static de.telekom.cldii.ApplicationConstants.EXTRAS_KEY_MAILID;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.gesture.GestureLibraries;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.fsck.k9.activity.setup.AccountSetupBasics;

import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.R;
import de.telekom.cldii.config.IPreferenceManager;
import de.telekom.cldii.controller.IControllerManager;
import de.telekom.cldii.controller.mail.IMailController;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.mail.ICompactMail;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelMailList;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.mail.adapter.MailListAdapter;
import de.telekom.cldii.view.util.CallSystemIntent;
import de.telekom.cldii.view.util.NetworkCheck;

/**
 * Mail list activity to display the list of all mails
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class MailListActivity extends AbstractActivity {
    private static final String TAG = "MailListActivity";
    private ListView mailListView;
    private AlertDialog alertDialog;
    private ProgressBar syncSpinner;

    private final BroadcastReceiver newMailReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateListView();
        }
    };

    private final BroadcastReceiver remoteMailsDeleted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateListView();
        }
    };

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

                updateListView();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_list);
        setTopBarName(getString(R.string.section_email));
        new ProgressDialog(getApplicationContext());
        syncSpinner = (ProgressBar) findViewById(R.id.syncSpinner);

        this.mailListView = (ListView) findViewById(R.id.mailList);

        findViewById(R.id.topBarName).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mailListView.setSelection(0);
            }
        });

        // notificate user if an internet connection is not available
        if (!NetworkCheck.isOnline(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_you_need_a_internet_connection).setCancelable(true)
                    .setPositiveButton(getString(R.string.confirm_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            alertDialog = null;

                            // Offer user to add a mail account
                            if (getDataProviderManager().getMailDataProvider().getAccountNames().size() == 0) {
                                displayNoAccountsDialog();
                            }
                        }
                    });
            alertDialog = builder.create();
            alertDialog.show();
        } else {
            // Offer user to add a mail account
            if (getDataProviderManager().getMailDataProvider().getAccountNames().size() == 0) {
                displayNoAccountsDialog();
            }
        }
    }

    private void displayNoAccountsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_add_mail_account).setCancelable(false)
                .setPositiveButton(getString(R.string.confirm_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AccountSetupBasics.actionNewAccount(MailListActivity.this);
                        alertDialog = null;
                    }
                }).setNegativeButton(getString(R.string.confirm_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        alertDialog = null;
                    }
                });
        builder.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH)
                    return true;
                return false;
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(contactSyncReceiver, new IntentFilter(CONTACTS_SYNCED_ACTION));

        if (!(alertDialog != null && alertDialog.isShowing())
                && getDataProviderManager().getMailDataProvider().getAccountNames().size() == 0)
            finish();

        // check for new mails
        if ((System.currentTimeMillis() - ((IPreferenceManager) getApplication()).getApplicationPreferences()
                .getMailLastUpdateMillis()) > ApplicationConstants.MAIL_MIN_LASTUPDATE) {
            getMailController().checkMails();
        }

        IntentFilter newMailReceiveFilter = new IntentFilter(getDataProviderManager().getMailDataProvider()
                .getNewMailIntentAction());
        this.registerReceiver(newMailReceiver, newMailReceiveFilter);
        IntentFilter remoteMailDeleteFilter = new IntentFilter(getDataProviderManager().getMailDataProvider()
                .getRemovedMailIntentAction());
        this.registerReceiver(remoteMailsDeleted, remoteMailDeleteFilter);

        if (getDataProviderManager().getContactDataProvider().isEmailSyncRunning()) {
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
        this.unregisterReceiver(newMailReceiver);
        this.unregisterReceiver(remoteMailsDeleted);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mail_list, menu);
        setMenuBackground();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean hasAccounts = getDataProviderManager().getMailDataProvider().getAccountNames().size() > 0;
        menu.getItem(0).setEnabled(hasAccounts); // new mail
        menu.getItem(1).setEnabled(hasAccounts); // check mail

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.checkMails:
            getMailController().checkMails();
            break;
        case R.id.manageAccounts:
            MailAccountListActivity.actionShowAccountList(this);
            break;
        case R.id.newMail:
            Intent intent = new Intent(MailListActivity.this, MailComposeActivity.class);
            startActivity(intent);
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private IMailController getMailController() {
        return ((IControllerManager) getApplication()).getMailController();
    }

    private void updateListView() {
        if (this.mailListView.getAdapter() == null) {
            this.mailListView.setAdapter(new MailListAdapter(MailListActivity.this, getDataProviderManager(),
                    getLayoutInflater()));
            this.mailListView.setEmptyView(findViewById(R.id.noMail));
            this.mailListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    String mailId = ((MailListAdapter) arg0.getAdapter()).getEmailId(arg2);
                    if (mailId != null) {
                        Intent intent = new Intent(MailListActivity.this, MailDetailsActivity.class);
                        intent.putExtra(EXTRAS_KEY_MAILID, mailId);
                        startActivity(intent);
                    }
                }
            });
            this.mailListView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(final AdapterView<?> arg0, View arg1, final int arg2, final long arg3) {
                    if (!(arg0.getItemAtPosition(arg2) instanceof ICompactMail))
                        return false;
                    final ICompactMail mailItem = (ICompactMail) arg0.getItemAtPosition(arg2);
                    final Contact contact = getDataProviderManager().getContactDataProvider().getContactForEmail(
                            mailItem.getFromAdress());
                    String contactItem = getString(R.string.contextmenu_addcontact);
                    if (contact != null)
                        contactItem = getString(R.string.contextmenu_editcontact);

                    String readItem = getString(R.string.contextmenu_markunread);
                    if (!mailItem.read())
                        readItem = getString(R.string.contextmenu_markread);

                    final CharSequence[] items = { contactItem, getString(R.string.dialog_delete), readItem,
                            getString(R.string.contextmenu_forward), getString(R.string.contextmenu_reply) };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MailListActivity.this);
                    builder.setTitle(getString(R.string.dialog_options));
                    builder.setCancelable(true);

                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            switch (item) {
                            case 0:
                                CallSystemIntent.callEditContactIntent(MailListActivity.this,
                                        contact != null ? Long.valueOf(contact.getId()) : -1, "mailto",
                                        ((ICompactMail) mailListView.getItemAtPosition(arg2)).getFromAdress(),
                                        R.id.requestCode_pickContact);
                                break;
                            case 1:
                                AlertDialog.Builder builder = new AlertDialog.Builder(MailListActivity.this);
                                builder.setMessage(String.format(getString(R.string.confirm_delete_mail)))
                                        .setCancelable(true)
                                        .setPositiveButton(getString(R.string.confirm_yes),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        getMailController().deleteMail(mailItem);
                                                        updateListView();
                                                        dialog.dismiss();
                                                    }
                                                })
                                        .setNegativeButton(getString(R.string.confirm_no),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                builder.create().show();
                                break;
                            case 2:
                                getMailController().changeReadState(mailItem, !mailItem.read());
                                ((BaseAdapter) mailListView.getAdapter()).notifyDataSetChanged();
                                break;
                            case 3:
                                Intent forwardIntent = new Intent(MailListActivity.this, MailComposeActivity.class);
                                forwardIntent.putExtra(EXTRAS_KEY_MAILID, mailItem.getId());
                                forwardIntent.setFlags(EXTRAS_FLAG_FORWARD);
                                startActivity(forwardIntent);
                                break;
                            case 4:
                                Intent replyIntent = new Intent(MailListActivity.this, MailComposeActivity.class);
                                replyIntent.putExtra(EXTRAS_KEY_MAILID, mailItem.getId());
                                startActivity(replyIntent);
                                break;
                            default:
                                showPrompt(getString(R.string.notavailable));
                                break;
                            }
                        }

                    });
                    builder.create().show();

                    return true;
                }
            });
        } else {
            Log.d(TAG, "updateListView");
            ((MailListAdapter) mailListView.getAdapter()).clearImageCache();
            ((MailListAdapter) this.mailListView.getAdapter()).refreshListView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == R.id.requestCode_pickContact) {
            ((MailListAdapter) mailListView.getAdapter()).clearImageCache();
            if (getDataProviderManager().getContactDataProvider().isEmailSyncRunning()) {
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
        return new StateModelMailList(MailListActivity.this, getDataProviderManager());
    }

}
