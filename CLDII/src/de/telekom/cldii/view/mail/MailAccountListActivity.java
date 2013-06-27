package de.telekom.cldii.view.mail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.fsck.k9.Account;
import com.fsck.k9.BaseAccount;
import com.fsck.k9.K9;
import com.fsck.k9.Preferences;
import com.fsck.k9.activity.AccountList;
import com.fsck.k9.activity.ConfirmationDialog;
import com.fsck.k9.activity.setup.AccountSetupBasics;
import com.fsck.k9.activity.setup.AccountSetupIncoming;
import com.fsck.k9.activity.setup.AccountSetupOptions;
import com.fsck.k9.activity.setup.AccountSetupOutgoing;
import com.fsck.k9.controller.MessagingController;

import de.telekom.cldii.R;
import de.telekom.cldii.config.IPreferenceManager;
import de.telekom.cldii.config.ISchedulerManager;

/**
 * Displays a list of mail accounts and provides an option for deleting and
 * managing the accounts.
 * 
 * The code was partially copied from {@link com.fsck.k9.activity.Accounts}.
 * 
 * @author Marco Pfattner, jambit GmbH
 */
public class MailAccountListActivity extends AccountList {

    private static final int DIALOG_REMOVE_ACCOUNT = 1;

    private BaseAccount mSelectedContextAccount;

    /**
     * Starts this activity using the provided context.
     * 
     * @param context
     */
    public static void actionShowAccountList(Context context) {
        context.startActivity(new Intent(context, MailAccountListActivity.class));
    }

    @Override
    public void onCreate(android.os.Bundle icicle) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	super.onCreate(icicle);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        TextView noAccountsText = (TextView) findViewById(com.fsck.k9.R.id.no_mails_text);
        ListView accountList = (ListView) findViewById(android.R.id.list);
        accountList.setEmptyView(noAccountsText);
    }

    @Override
    protected boolean displaySpecialAccounts() {
        return false;
    }

    @Override
    protected void onAccountSelected(final BaseAccount account) {
        // display the options (delete/manage account)

        final String items[] = { getString(R.string.mail_manage_accounts_incoming),
                getString(R.string.mail_manage_accounts_outgoing), getString(R.string.mail_manage_accounts_extended),
                getString(R.string.mail_manage_accounts_delete) };

        new AlertDialog.Builder(this).setTitle(account.getDescription())
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int choice) {
                        if (choice == 0) {
                            onEditIncoming(account);
                        } else if (choice == 1) {
                            onEditOutgoing(account);
                        } else if (choice == 2) {
                            onExtendedOptions(account);
                        } else if (choice == 3) {
                            onDeleteAccount(account);
                        }
                    }
                }).show();
    }

    private void onEditIncoming(BaseAccount account) {
        if (account instanceof Account) {
            AccountSetupIncoming.actionEditIncomingSettings(this, (Account) account);
        }
    }

    private void onEditOutgoing(BaseAccount account) {
        if (account instanceof Account) {
            AccountSetupOutgoing.actionEditOutgoingSettings(this, (Account) account);
        }
    }

    private void onDeleteAccount(BaseAccount account) {
        mSelectedContextAccount = account;
        showDialog(DIALOG_REMOVE_ACCOUNT);
    }

    private void onExtendedOptions(BaseAccount account) {
        if (account instanceof Account) {
            AccountSetupOptions.actionOptions(this, (Account) account, false);
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        // Android recreates our dialogs on configuration changes even when they
        // have been
        // dismissed. Make sure we have all information necessary before
        // creating a new dialog.
        switch (id) {
        case DIALOG_REMOVE_ACCOUNT: {
            if (mSelectedContextAccount == null) {
                return null;
            }

            return ConfirmationDialog.create(this, id, R.string.account_delete_dlg_title,
                    getString(R.string.account_delete_dlg_instructions_fmt, mSelectedContextAccount.getEmail()),
                    R.string.okay_action, R.string.cancel_action, new Runnable() {
                        @Override
                        public void run() {
                            if (mSelectedContextAccount instanceof Account) {
                                Account realAccount = (Account) mSelectedContextAccount;
                                try {
                                    realAccount.getLocalStore().delete();
                                } catch (Exception e) {
                                    // Ignore, this may lead to localStores
                                    // on sd-cards that
                                    // are currently not inserted to be left
                                }
                                MessagingController.getInstance(getApplication()).notifyAccountCancel(
                                        MailAccountListActivity.this, realAccount);
                                Preferences.getPreferences(MailAccountListActivity.this).deleteAccount(realAccount);
                                K9.setServicesEnabled(MailAccountListActivity.this);
                                refresh();
                            }
                        }
                    });
        }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.createAccount:
            AccountSetupBasics.actionNewAccount(this);
            break;
        case R.id.mailUpdateInterval:
            onChangeUpdateIntervalOptionSelected();
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onChangeUpdateIntervalOptionSelected() {

        final String items[] = { getString(R.string.mail_update_interval_onstart),
                getString(R.string.mail_update_interval_15_min), getString(R.string.mail_update_interval_30_min),
                getString(R.string.mail_update_interval_60_min), getString(R.string.mail_update_interval_120_min) };
        final long millisecondsInMinute = 60000;

        int updateIntervallInMinutes = (int) (getPreferenceManager().getApplicationPreferences()
                .getMailUpdateInterval() / millisecondsInMinute);
        int checked;
        switch (updateIntervallInMinutes) {
        case 15:
            checked = 1;
            break;
        case 30:
            checked = 2;
            break;
        case 60:
            checked = 3;
            break;
        case 120:
            checked = 4;
            break;
        default:
            checked = 0;
            break;
        }
        new AlertDialog.Builder(this).setTitle(getString(R.string.mail_update_interval))
                .setSingleChoiceItems(items, checked, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int choice) {
                        getSchedulerManager().getMailUpdateScheduler().unscheduleMailUpdate();

                        if (choice == 0) { // onStart
                            getPreferenceManager().getApplicationPreferences().setMailUpdateInterval(0);
                        } else if (choice == 1) { // 15min
                            getPreferenceManager().getApplicationPreferences().setMailUpdateInterval(
                                    15 * millisecondsInMinute);
                        } else if (choice == 2) { // 30min
                            getPreferenceManager().getApplicationPreferences().setMailUpdateInterval(
                                    30 * millisecondsInMinute);
                        } else if (choice == 3) { // 60min
                            getPreferenceManager().getApplicationPreferences().setMailUpdateInterval(
                                    60 * millisecondsInMinute);
                        } else if (choice == 4) { // 120min
                            getPreferenceManager().getApplicationPreferences().setMailUpdateInterval(
                                    120 * millisecondsInMinute);
                        }
                        getSchedulerManager().getMailUpdateScheduler().scheduleMailUpdate();
                        d.dismiss();
                    }
                }).show();
    }

    /**
     * Refresh the list view in case an account was modified or deleted.
     */
    private void refresh() {
        refreshAccountList();
    }

    private ISchedulerManager getSchedulerManager() {
        return (ISchedulerManager) getApplication();
    }

    private IPreferenceManager getPreferenceManager() {
        return (IPreferenceManager) getApplication();
    }
}
