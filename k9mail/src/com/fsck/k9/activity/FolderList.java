package com.fsck.k9.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Config;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.fsck.k9.*;
import com.fsck.k9.Account.FolderMode;
import com.fsck.k9.activity.FolderInfoHolder;
import com.fsck.k9.activity.FolderList.FolderListAdapter.FolderListFilter;
import com.fsck.k9.activity.setup.Prefs;
import com.fsck.k9.activity.setup.AccountSettings;
import com.fsck.k9.activity.setup.FolderSettings;
import com.fsck.k9.controller.MessagingController;
import com.fsck.k9.controller.MessagingListener;
import com.fsck.k9.helper.SizeFormatter;
import com.fsck.k9.helper.power.TracingPowerManager;
import com.fsck.k9.helper.power.TracingPowerManager.TracingWakeLock;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.store.LocalStore.LocalFolder;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.service.MailService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * FolderList is the primary user interface for the program. This
 * Activity shows list of the Account's folders
 */

public class FolderList extends K9ListActivity {
    /*
     * Constants for showDialog() etc.
     */
    private static final int DIALOG_MARK_ALL_AS_READ = 1;
    private static final int DIALOG_FIND_FOLDER = 2;

    private static final String EXTRA_ACCOUNT = "account";

    private static final String EXTRA_INITIAL_FOLDER = "initialFolder";
    private static final String EXTRA_FROM_NOTIFICATION = "fromNotification";
    private static final String EXTRA_FROM_SHORTCUT = "fromShortcut";

    private static final boolean REFRESH_REMOTE = true;

    private ListView mListView;

    private FolderListAdapter mAdapter;

    private LayoutInflater mInflater;

    private Account mAccount;

    private FolderListHandler mHandler = new FolderListHandler();

    private int mUnreadMessageCount;

    private FontSizes mFontSizes = K9.getFontSizes();
    private Context context;

    class FolderListHandler extends Handler {

        public void refreshTitle() {
            runOnUiThread(new Runnable() {
                public void run() {
                    String dispString = mAdapter.mListener.formatHeader(FolderList.this,
                                        getString(R.string.folder_list_title, mAccount.getDescription()), mUnreadMessageCount, getTimeFormat());


                    setTitle(dispString);
                }
            });
        }


        public void newFolders(final List<FolderInfoHolder> newFolders) {
            runOnUiThread(new Runnable() {
                public void run() {
                    mAdapter.mFolders.clear();
                    mAdapter.mFolders.addAll(newFolders);
                    mAdapter.mFilteredFolders = mAdapter.mFolders;
                    mHandler.dataChanged();
                }
            });
        }

        public void workingAccount(final int res) {
            runOnUiThread(new Runnable() {
                public void run() {
                    String toastText = getString(res, mAccount.getDescription());
                    Toast toast = Toast.makeText(getApplication(), toastText, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

        public void accountSizeChanged(final long oldSize, final long newSize) {
            runOnUiThread(new Runnable() {
                public void run() {
                    String toastText = getString(R.string.account_size_changed, mAccount.getDescription(), SizeFormatter.formatSize(getApplication(), oldSize), SizeFormatter.formatSize(getApplication(), newSize));

                    Toast toast = Toast.makeText(getApplication(), toastText, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        public void folderLoading(final String folder, final boolean loading) {
            runOnUiThread(new Runnable() {
                public void run() {
                    FolderInfoHolder folderHolder = mAdapter.getFolder(folder);


                    if (folderHolder != null) {
                        folderHolder.loading = loading;
                    }

                }
            });
        }

        public void progress(final boolean progress) {
            runOnUiThread(new Runnable() {
                public void run() {
                    setProgressBarIndeterminateVisibility(progress);
                }
            });

        }

        public void dataChanged() {
            runOnUiThread(new Runnable() {
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
    * This class is responsible for reloading the list of local messages for a
    * given folder, notifying the adapter that the message have been loaded and
    * queueing up a remote update of the folder.
     */

    private void checkMail(FolderInfoHolder folder) {
        TracingPowerManager pm = TracingPowerManager.getPowerManager(this);
        final TracingWakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FolderList checkMail");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire(K9.WAKE_LOCK_TIMEOUT);
        MessagingListener listener = new MessagingListener() {
            @Override
            public void synchronizeMailboxFinished(Account account, String folder, int totalMessagesInMailbox, int numNewMessages) {
                if (!account.equals(mAccount)) {
                    return;
                }
                wakeLock.release();
            }

            @Override
            public void synchronizeMailboxFailed(Account account, String folder,
            String message) {
                if (!account.equals(mAccount)) {
                    return;
                }
                wakeLock.release();
            }
        };
        MessagingController.getInstance(getApplication()).synchronizeMailbox(mAccount, folder.name, listener, null);
        sendMail(mAccount);
    }

    public static Intent actionHandleAccountIntent(Context context, Account account) {
        return actionHandleAccountIntent(context, account, null, false);
    }

    public static Intent actionHandleAccountIntent(Context context, Account account, String initialFolder) {
        return actionHandleAccountIntent(context, account, initialFolder, false);
    }

    public static Intent actionHandleAccountIntent(Context context, Account account, String initialFolder, boolean fromShortcut) {
        Intent intent = new Intent(context, FolderList.class);
        intent.putExtra(EXTRA_ACCOUNT, account.getUuid());

        if (initialFolder != null) {
            intent.putExtra(EXTRA_INITIAL_FOLDER, initialFolder);
        }

        if (fromShortcut) {
            intent.putExtra(EXTRA_FROM_SHORTCUT, true);
        }

        return intent;
    }

    private static void actionHandleAccount(Context context, Account account, String initialFolder) {
        Intent intent = actionHandleAccountIntent(context, account, initialFolder);
        context.startActivity(intent);
    }

    public static void actionHandleAccount(Context context, Account account) {
        actionHandleAccount(context, account, null);
    }

    public static Intent actionHandleNotification(Context context, Account account, String initialFolder) {
        Intent intent = new Intent(
            Intent.ACTION_VIEW,
            Uri.parse("email://accounts/" + account.getAccountNumber()),
            context,
            FolderList.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
        intent.putExtra(EXTRA_FROM_NOTIFICATION, true);

        if (initialFolder != null) {
            intent.putExtra(EXTRA_INITIAL_FOLDER, initialFolder);
        }
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.folder_list);
        mListView = getListView();
        mListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        mListView.setLongClickable(true);
        mListView.setFastScrollEnabled(true);
        mListView.setScrollingCacheEnabled(false);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onOpenFolder(((FolderInfoHolder)mAdapter.getItem(position)).name);
            }
        });
        registerForContextMenu(mListView);

        mListView.setSaveEnabled(true);

        mInflater = getLayoutInflater();

        onNewIntent(getIntent());

        context = this;
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent); // onNewIntent doesn't autoset our "internal" intent

        String initialFolder;

        mUnreadMessageCount = 0;
        String accountUuid = intent.getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

        if (mAccount == null) {
            // This shouldn't normally happen. But apparently it does. See issue 2261.
            finish();
            return;
        }

        initialFolder = intent.getStringExtra(EXTRA_INITIAL_FOLDER);
        boolean fromNotification = intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false);
        if (fromNotification && mAccount.goToUnreadMessageSearch()) {
            MessagingController.getInstance(getApplication()).notifyAccountCancel(this, mAccount);
            openUnreadSearch(this, mAccount);
            finish();
        } else if (initialFolder != null && !K9.FOLDER_NONE.equals(initialFolder)) {
            onOpenFolder(initialFolder);
            finish();
        } else if (intent.getBooleanExtra(EXTRA_FROM_SHORTCUT, false) &&
                   !K9.FOLDER_NONE.equals(mAccount.getAutoExpandFolderName())) {
            onOpenFolder(mAccount.getAutoExpandFolderName());
            finish();
        } else {

            initializeActivityView();
        }
    }

    private void initializeActivityView() {
        mAdapter = new FolderListAdapter();
        restorePreviousData();

        setListAdapter(mAdapter);
        getListView().setTextFilterEnabled(mAdapter.getFilter() != null); // should never be false but better safe then sorry

        setTitle(mAccount.getDescription());

    }

    @SuppressWarnings("unchecked")
    private void restorePreviousData() {
        final Object previousData = getLastNonConfigurationInstance();

        if (previousData != null) {
            mAdapter.mFolders = (ArrayList<FolderInfoHolder>) previousData;
            mAdapter.mFilteredFolders = Collections.unmodifiableList(mAdapter.mFolders);
        }
    }


    @Override public Object onRetainNonConfigurationInstance() {
        return (mAdapter == null) ? null : mAdapter.mFolders;
    }

    @Override public void onPause() {
        super.onPause();
        MessagingController.getInstance(getApplication()).removeListener(mAdapter.mListener);
    }

    /**
    * On resume we refresh the folder list (in the background) and we refresh the
    * messages for any folder that is currently open. This guarantees that things
    * like unread message count and read status are updated.
     */
    @Override public void onResume() {
        super.onResume();

        if (!mAccount.isAvailable(this)) {
            Log.i(K9.LOG_TAG, "account unavaliabale, not showing folder-list but account-list");
            startActivity(new Intent(this, Accounts.class));
            finish();
            return;
        }
        if (mAdapter == null)
            initializeActivityView();

        MessagingController.getInstance(getApplication()).addListener(mAdapter.mListener);
        //mAccount.refresh(Preferences.getPreferences(this));
        MessagingController.getInstance(getApplication()).getAccountStats(this, mAccount, mAdapter.mListener);

        onRefresh(!REFRESH_REMOTE);

        MessagingController.getInstance(getApplication()).notifyAccountCancel(this, mAccount);
    }


    @Override
    public void onBackPressed() {
        if (K9.manageBack()) {
            onAccounts();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Shortcuts that work no matter what is selected
        switch (keyCode) {
        case KeyEvent.KEYCODE_Q:
        {
            onAccounts();
            return true;
        }

        case KeyEvent.KEYCODE_S: {
            onEditAccount();
            return true;
        }

        case KeyEvent.KEYCODE_H: {
            Toast toast = Toast.makeText(this, R.string.folder_list_help_key, Toast.LENGTH_LONG);
            toast.show();
            return true;
        }

        case KeyEvent.KEYCODE_1: {
            setDisplayMode(FolderMode.FIRST_CLASS);
            return true;
        }
        case KeyEvent.KEYCODE_2: {
            setDisplayMode(FolderMode.FIRST_AND_SECOND_CLASS);
            return true;
        }
        case KeyEvent.KEYCODE_3: {
            setDisplayMode(FolderMode.NOT_SECOND_CLASS);
            return true;
        }
        case KeyEvent.KEYCODE_4: {
            setDisplayMode(FolderMode.ALL);
            return true;
        }
        }//switch


        return super.onKeyDown(keyCode, event);
    }//onKeyDown

    private void setDisplayMode(FolderMode newMode) {
        mAccount.setFolderDisplayMode(newMode);
        mAccount.save(Preferences.getPreferences(this));
        if (mAccount.getFolderPushMode() != FolderMode.NONE) {
            MailService.actionRestartPushers(this, null);
        }
        mAdapter.getFilter().filter(null);
        onRefresh(false);
    }


    private void onRefresh(final boolean forceRemote) {

        MessagingController.getInstance(getApplication()).listFolders(mAccount, forceRemote, mAdapter.mListener);

    }

    /**
     * Show an alert with an input-field for a filter-expression.
     * Filter {@link #mAdapter} with the user-input.
     */
    private void onEnterFilter() {
        showDialog(DIALOG_FIND_FOLDER);
    }

    private void onEditPrefs() {
        Prefs.actionPrefs(this);
    }
    private void onEditAccount() {
        AccountSettings.actionSettings(this, mAccount);
    }

    private void onEditFolder(Account account, String folderName) {
        FolderSettings.actionSettings(this, account, folderName);
    }

    private void onAccounts() {
        Accounts.listAccounts(this);
        finish();
    }

    private void onEmptyTrash(final Account account) {
        mHandler.dataChanged();

        MessagingController.getInstance(getApplication()).emptyTrash(account, null);
    }

    private void onExpunge(final Account account, String folderName) {
        MessagingController.getInstance(getApplication()).expunge(account, folderName, null);
    }


    private void onClearFolder(Account account, String folderName) {
        // There has to be a cheaper way to get at the localFolder object than this
        LocalFolder localFolder = null;
        try {
            if (account == null || folderName == null || !account.isAvailable(FolderList.this)) {
                Log.i(K9.LOG_TAG, "not clear folder of unavailable account");
                return;
            }
            localFolder = account.getLocalStore().getFolder(folderName);
            localFolder.open(Folder.OpenMode.READ_WRITE);
            localFolder.clearAllMessages();
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Exception while clearing folder", e);
        } finally {
            if (localFolder != null) {
                localFolder.close();
            }
        }

        onRefresh(!REFRESH_REMOTE);
    }





    private void sendMail(Account account) {
        MessagingController.getInstance(getApplication()).sendPendingMessages(account, mAdapter.mListener);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose) {
			MessageCompose.actionCompose(this, mAccount);
			return true;
		} else if (item.getItemId() == R.id.check_mail) {
			MessagingController.getInstance(getApplication()).checkMail(this, mAccount, true, true, mAdapter.mListener);
			return true;
		} else if (item.getItemId() == R.id.send_messages) {
			MessagingController.getInstance(getApplication()).sendPendingMessages(mAccount, null);
			return true;
		} else if (item.getItemId() == R.id.accounts) {
			onAccounts();
			return true;
		} else if (item.getItemId() == R.id.list_folders) {
			onRefresh(REFRESH_REMOTE);
			return true;
		} else if (item.getItemId() == R.id.filter_folders) {
			onEnterFilter();
			return true;
		} else if (item.getItemId() == R.id.account_settings) {
			onEditAccount();
			return true;
		} else if (item.getItemId() == R.id.app_settings) {
			onEditPrefs();
			return true;
		} else if (item.getItemId() == R.id.empty_trash) {
			onEmptyTrash(mAccount);
			return true;
		} else if (item.getItemId() == R.id.compact) {
			onCompact(mAccount);
			return true;
		} else if (item.getItemId() == R.id.display_1st_class) {
			setDisplayMode(FolderMode.FIRST_CLASS);
			return true;
		} else if (item.getItemId() == R.id.display_1st_and_2nd_class) {
			setDisplayMode(FolderMode.FIRST_AND_SECOND_CLASS);
			return true;
		} else if (item.getItemId() == R.id.display_not_second_class) {
			setDisplayMode(FolderMode.NOT_SECOND_CLASS);
			return true;
		} else if (item.getItemId() == R.id.display_all) {
			setDisplayMode(FolderMode.ALL);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }

    private void onOpenFolder(String folder) {
        MessageList.actionHandleFolder(this, mAccount, folder);
        if (K9.manageBack()) {
            finish();
        }
    }

    private void onCompact(Account account) {
        mHandler.workingAccount(R.string.compacting_account);
        MessagingController.getInstance(getApplication()).compact(account, null);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.folder_list_option, menu);
        return true;
    }

    @Override public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item .getMenuInfo();
        FolderInfoHolder folder = (FolderInfoHolder) mAdapter.getItem(info.position);

        if (item.getItemId() == R.id.open_folder) {
			onOpenFolder(folder.name);
		} else if (item.getItemId() == R.id.mark_all_as_read) {
			onMarkAllAsRead(mAccount, folder.name);
		} else if (item.getItemId() == R.id.send_messages) {
			sendMail(mAccount);
		} else if (item.getItemId() == R.id.check_mail) {
			checkMail(folder);
		} else if (item.getItemId() == R.id.folder_settings) {
			onEditFolder(mAccount, folder.name);
		} else if (item.getItemId() == R.id.empty_trash) {
			onEmptyTrash(mAccount);
		} else if (item.getItemId() == R.id.expunge) {
			onExpunge(mAccount, folder.name);
		} else if (item.getItemId() == R.id.clear_local_folder) {
			onClearFolder(mAccount, folder.name);
		}

        return super.onContextItemSelected(item);
    }

    private FolderInfoHolder mSelectedContextFolder = null;


    private void onMarkAllAsRead(final Account account, final String folder) {
        mSelectedContextFolder = mAdapter.getFolder(folder);
        if (K9.confirmMarkAllAsRead()) {
            showDialog(DIALOG_MARK_ALL_AS_READ);
        } else {
            markAllAsRead();
        }
    }

    private void markAllAsRead() {
        try {
            MessagingController.getInstance(getApplication())
            .markAllMessagesRead(mAccount, mSelectedContextFolder.name);
            mSelectedContextFolder.unreadMessageCount = 0;
            mHandler.dataChanged();
        } catch (Exception e) {
            /* Ignore */
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_MARK_ALL_AS_READ:
                return ConfirmationDialog.create(this, id, R.string.mark_all_as_read_dlg_title,
                        getString(R.string.mark_all_as_read_dlg_instructions_fmt,
                            mSelectedContextFolder.displayName),
                        R.string.okay_action, R.string.cancel_action,
                        new Runnable() {
                            @Override
                            public void run() {
                                markAllAsRead();
                            }
                        });
            case DIALOG_FIND_FOLDER: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.filter_folders_action);

                final EditText input = new EditText(this);
                input.setId(R.id.filter_folders);
                input.setHint(R.string.folder_list_filter_hint);
                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        mAdapter.getFilter().filter(input.getText());
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                            int after) { /* not used */ }

                    @Override
                    public void afterTextChanged(Editable s) { /* not used */ }
                });

                builder.setView(input);

                builder.setPositiveButton(getString(R.string.okay_action),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = input.getText().toString();
                                mAdapter.getFilter().filter(value);
                            }
                        });

                builder.setNegativeButton(getString(R.string.cancel_action),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mAdapter.getFilter().filter(null);
                            }
                        });

                return builder.create();
            }
        }

        return super.onCreateDialog(id);
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_MARK_ALL_AS_READ: {
                AlertDialog alertDialog = (AlertDialog) dialog;
                alertDialog.setMessage(getString(R.string.mark_all_as_read_dlg_instructions_fmt,
                        mSelectedContextFolder.displayName));
                break;
            }
            case DIALOG_FIND_FOLDER: {
                AlertDialog alertDialog = (AlertDialog) dialog;
                EditText input = (EditText) alertDialog.findViewById(R.id.filter_folders);

                // Populate the EditText with the current search term
                FolderListFilter filter = (FolderListFilter) mAdapter.getFilter();
                input.setText(filter.getSearchTerm());

                // Place the cursor at the end of the text
                input.setSelection(input.getText().length());
                break;
            }
            default: {
                super.onPrepareDialog(id, dialog);
            }
        }
    }

    @Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        getMenuInflater().inflate(R.menu.folder_context, menu);

        FolderInfoHolder folder = (FolderInfoHolder) mAdapter.getItem(info.position);

        menu.setHeaderTitle(folder.displayName);

        if (!folder.name.equals(mAccount.getTrashFolderName()))
            menu.findItem(R.id.empty_trash).setVisible(false);

        if (folder.name.equals(mAccount.getOutboxFolderName())) {
            menu.findItem(R.id.check_mail).setVisible(false);
        } else {
            menu.findItem(R.id.send_messages).setVisible(false);
        }
        if (K9.ERROR_FOLDER_NAME.equals(folder.name)) {
            menu.findItem(R.id.expunge).setVisible(false);
        }

        if (!MessagingController.getInstance(getApplication()).isMoveCapable(mAccount)) {
            // FIXME: Really we want to do this for all local-only folders
            if (!mAccount.getInboxFolderName().equals(folder.name)) {
                menu.findItem(R.id.check_mail).setVisible(false);
            }

            menu.findItem(R.id.expunge).setVisible(false);
        }

        menu.setHeaderTitle(folder.displayName);
    }

    class FolderListAdapter extends BaseAdapter implements Filterable {
        private ArrayList<FolderInfoHolder> mFolders = new ArrayList<FolderInfoHolder>();
        private List<FolderInfoHolder> mFilteredFolders = Collections.unmodifiableList(mFolders);
        private Filter mFilter = new FolderListFilter();

        public Object getItem(long position) {
            return getItem((int)position);
        }

        public Object getItem(int position) {
            return mFilteredFolders.get(position);
        }


        public long getItemId(int position) {
            return mFilteredFolders.get(position).folder.getName().hashCode() ;
        }

        public int getCount() {
            return mFilteredFolders.size();
        }

        @Override
        public boolean isEnabled(int item) {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        private ActivityListener mListener = new ActivityListener() {
            @Override
            public void informUserOfStatus() {
                mHandler.refreshTitle();
            }
            @Override
            public void accountStatusChanged(BaseAccount account, AccountStats stats) {
                if (!account.equals(mAccount)) {
                    return;
                }
                if (stats == null) {
                    return;
                }
                mUnreadMessageCount = stats.unreadMessageCount;
                super.accountStatusChanged(account, stats);
            }

            @Override
            public void listFoldersStarted(Account account) {
                if (account.equals(mAccount)) {
                    mHandler.progress(true);
                }
                super.listFoldersStarted(account);

            }

            @Override
            public void listFoldersFailed(Account account, String message) {
                if (account.equals(mAccount)) {

                    mHandler.progress(false);

                    if (Config.LOGV) {
                        Log.v(K9.LOG_TAG, "listFoldersFailed " + message);
                    }
                }
                super.listFoldersFailed(account, message);
            }

            @Override
            public void listFoldersFinished(Account account) {
                if (account.equals(mAccount)) {

                    mHandler.progress(false);
                    MessagingController.getInstance(getApplication()).refreshListener(mAdapter.mListener);
                    mHandler.dataChanged();
                }
                super.listFoldersFinished(account);

            }

            @Override
            public void listFolders(Account account, Folder[] folders) {
                if (account.equals(mAccount)) {

                    List<FolderInfoHolder> newFolders = new LinkedList<FolderInfoHolder>();
                    List<FolderInfoHolder> topFolders = new LinkedList<FolderInfoHolder>();

                    Account.FolderMode aMode = account.getFolderDisplayMode();
                    Preferences prefs = Preferences.getPreferences(getApplication().getApplicationContext());
                    for (Folder folder : folders) {
                        try {
                            folder.refresh(prefs);

                            Folder.FolderClass fMode = folder.getDisplayClass();

                            if ((aMode == Account.FolderMode.FIRST_CLASS && fMode != Folder.FolderClass.FIRST_CLASS)
                                    || (aMode == Account.FolderMode.FIRST_AND_SECOND_CLASS &&
                                        fMode != Folder.FolderClass.FIRST_CLASS &&
                                        fMode != Folder.FolderClass.SECOND_CLASS)
                            || (aMode == Account.FolderMode.NOT_SECOND_CLASS && fMode == Folder.FolderClass.SECOND_CLASS)) {
                                continue;
                            }
                        } catch (MessagingException me) {
                            Log.e(K9.LOG_TAG, "Couldn't get prefs to check for displayability of folder " + folder.getName(), me);
                        }

                        FolderInfoHolder holder = null;

                        int folderIndex = getFolderIndex(folder.getName());
                        if (folderIndex >= 0) {
                            holder = (FolderInfoHolder) getItem(folderIndex);
                        }
                        int unreadMessageCount = 0;
                        try {
                            unreadMessageCount  = folder.getUnreadMessageCount();
                        } catch (Exception e) {
                            Log.e(K9.LOG_TAG, "Unable to get unreadMessageCount for " + mAccount.getDescription() + ":"
                                  + folder.getName());
                        }

                        if (holder == null) {
                            holder = new FolderInfoHolder(context, folder, mAccount, unreadMessageCount);
                        } else {
                            holder.populate(context, folder, mAccount, unreadMessageCount);

                        }
                        if (folder.isInTopGroup()) {
                            topFolders.add(holder);
                        } else {
                            newFolders.add(holder);
                        }
                    }
                    Collections.sort(newFolders);
                    Collections.sort(topFolders);
                    topFolders.addAll(newFolders);
                    mHandler.newFolders(topFolders);
                }
                super.listFolders(account, folders);
            }

            @Override
            public void synchronizeMailboxStarted(Account account, String folder) {
                super.synchronizeMailboxStarted(account, folder);
                if (account.equals(mAccount)) {

                    mHandler.progress(true);
                    mHandler.folderLoading(folder, true);
                    mHandler.dataChanged();
                }

            }

            @Override
            public void synchronizeMailboxFinished(Account account, String folder, int totalMessagesInMailbox, int numNewMessages) {
                super.synchronizeMailboxFinished(account, folder, totalMessagesInMailbox, numNewMessages);
                if (account.equals(mAccount)) {
                    mHandler.progress(false);
                    mHandler.folderLoading(folder, false);

                    refreshFolder(account, folder);
                }

            }

            private void refreshFolder(Account account, String folderName) {
                // There has to be a cheaper way to get at the localFolder object than this
                Folder localFolder = null;
                try {
                    if (account != null && folderName != null) {
                        if (!account.isAvailable(FolderList.this)) {
                            Log.i(K9.LOG_TAG, "not refreshing folder of unavailable account");
                            return;
                        }
                        localFolder = account.getLocalStore().getFolder(folderName);
                        int unreadMessageCount = localFolder.getUnreadMessageCount();
                        FolderInfoHolder folderHolder = getFolder(folderName);
                        if (folderHolder != null) {
                            int oldUnreadMessageCount = folderHolder.unreadMessageCount;
                            mUnreadMessageCount += unreadMessageCount - oldUnreadMessageCount;
                            folderHolder.populate(context, localFolder, mAccount, unreadMessageCount);
                            mHandler.dataChanged();
                        }
                    }
                } catch (Exception e) {
                    Log.e(K9.LOG_TAG, "Exception while populating folder", e);
                } finally {
                    if (localFolder != null) {
                        localFolder.close();
                    }
                }

            }

            @Override
            public void synchronizeMailboxFailed(Account account, String folder, String message) {
                super.synchronizeMailboxFailed(account, folder, message);
                if (!account.equals(mAccount)) {
                    return;
                }


                mHandler.progress(false);

                mHandler.folderLoading(folder, false);

                //   String mess = truncateStatus(message);

                //   mHandler.folderStatus(folder, mess);
                FolderInfoHolder holder = getFolder(folder);

                if (holder != null) {
                    holder.lastChecked = 0;
                }

                mHandler.dataChanged();

            }

            @Override
            public void setPushActive(Account account, String folderName, boolean enabled) {
                if (!account.equals(mAccount)) {
                    return;
                }
                FolderInfoHolder holder = getFolder(folderName);

                if (holder != null) {
                    holder.pushActive = enabled;

                    mHandler.dataChanged();
                }
            }


            @Override
            public void messageDeleted(Account account, String folder, Message message) {
                synchronizeMailboxRemovedMessage(account, folder, message);
            }

            @Override
            public void emptyTrashCompleted(Account account) {
                if (account.equals(mAccount)) {
                    refreshFolder(account, mAccount.getTrashFolderName());
                }
            }

            @Override
            public void folderStatusChanged(Account account, String folderName, int unreadMessageCount) {
                if (account.equals(mAccount)) {
                    refreshFolder(account, folderName);
                    informUserOfStatus();
                }
            }

            @Override
            public void sendPendingMessagesCompleted(Account account) {
                super.sendPendingMessagesCompleted(account);
                if (account.equals(mAccount)) {
                    refreshFolder(account, mAccount.getOutboxFolderName());
                }
            }

            @Override
            public void sendPendingMessagesStarted(Account account) {
                super.sendPendingMessagesStarted(account);

                if (account.equals(mAccount)) {
                    mHandler.dataChanged();
                }
            }

            @Override
            public void sendPendingMessagesFailed(Account account) {
                super.sendPendingMessagesFailed(account);
                if (account.equals(mAccount)) {
                    refreshFolder(account, mAccount.getOutboxFolderName());
                }
            }

            @Override
            public void accountSizeChanged(Account account, long oldSize, long newSize) {
                if (account.equals(mAccount)) {
                    mHandler.accountSizeChanged(oldSize, newSize);
                }
            }
        };


        public int getFolderIndex(String folder) {
            FolderInfoHolder searchHolder = new FolderInfoHolder();
            searchHolder.name = folder;
            return   mFilteredFolders.indexOf(searchHolder);
        }

        public FolderInfoHolder getFolder(String folder) {
            FolderInfoHolder holder = null;

            int index = getFolderIndex(folder);
            if (index >= 0) {
                holder = (FolderInfoHolder) getItem(index);
                if (holder != null) {
                    return holder;
                }
            }
            return null;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position <= getCount()) {
                return  getItemView(position, convertView, parent);
            } else {
                Log.e(K9.LOG_TAG, "getView with illegal positon=" + position
                      + " called! count is only " + getCount());
                return null;
            }
        }

        public View getItemView(int itemPosition, View convertView, ViewGroup parent) {
            FolderInfoHolder folder = (FolderInfoHolder) getItem(itemPosition);
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.folder_list_item, parent, false);
            }

            FolderViewHolder holder = (FolderViewHolder) view.getTag();

            if (holder == null) {
                holder = new FolderViewHolder();
                holder.folderName = (TextView) view.findViewById(R.id.folder_name);
                holder.newMessageCount = (TextView) view.findViewById(R.id.folder_unread_message_count);
                holder.flaggedMessageCount = (TextView) view.findViewById(R.id.folder_flagged_message_count);
                holder.folderStatus = (TextView) view.findViewById(R.id.folder_status);
                holder.activeIcons = (RelativeLayout) view.findViewById(R.id.active_icons);
                holder.chip = view.findViewById(R.id.chip);
                holder.folderListItemLayout = (LinearLayout)view.findViewById(R.id.folder_list_item_layout);
                holder.rawFolderName = folder.name;

                view.setTag(holder);
            }

            if (folder == null) {
                return view;
            }

            holder.folderName.setText(folder.displayName);
            String statusText = "";

            if (folder.loading) {
                statusText = getString(R.string.status_loading);
            } else if (folder.status != null) {
                statusText = folder.status;
            } else if (folder.lastChecked != 0) {
                Date lastCheckedDate = new Date(folder.lastChecked);

                statusText = getTimeFormat().format(lastCheckedDate) + " " +
                             getDateFormat().format(lastCheckedDate);
            }

            if (folder.pushActive) {
                statusText = getString(R.string.folder_push_active_symbol) + " " + statusText;
            }

            if (statusText != null) {
                holder.folderStatus.setText(statusText);
                holder.folderStatus.setVisibility(View.VISIBLE);
            } else {
                holder.folderStatus.setText(null);
                holder.folderStatus.setVisibility(View.GONE);
            }

            if (folder.unreadMessageCount != 0) {
                holder.newMessageCount.setText(Integer
                                               .toString(folder.unreadMessageCount));
                holder.newMessageCount.setOnClickListener(new FolderClickListener(mAccount, folder.name, folder.displayName, SearchModifier.UNREAD));
                holder.newMessageCount.setVisibility(View.VISIBLE);
            } else {
                holder.newMessageCount.setVisibility(View.GONE);
            }

            if (K9.messageListStars() && folder.flaggedMessageCount > 0) {
                holder.flaggedMessageCount.setText(Integer
                                                   .toString(folder.flaggedMessageCount));
                holder.flaggedMessageCount.setOnClickListener(new FolderClickListener(mAccount, folder.name, folder.displayName, SearchModifier.FLAGGED));
                holder.flaggedMessageCount.setVisibility(View.VISIBLE);
            } else {
                holder.flaggedMessageCount.setVisibility(View.GONE);
            }
            if (K9.useCompactLayouts() && holder.folderListItemLayout != null) {
                holder.folderListItemLayout.setMinimumHeight(0);
            }
            holder.activeIcons.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Toast toast = Toast.makeText(getApplication(), getString(R.string.tap_hint), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
                                                 );

            holder.chip.setBackgroundDrawable(mAccount.generateColorChip().drawable());
            holder.chip.getBackground().setAlpha(folder.unreadMessageCount == 0 ? 127 : 255);

            holder.folderName.setTextSize(TypedValue.COMPLEX_UNIT_SP, mFontSizes.getFolderName());
            holder.folderStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, mFontSizes.getFolderStatus());


            return view;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public boolean isItemSelectable(int position) {
            return true;
        }

        public void setFilter(final Filter filter) {
            this.mFilter = filter;
        }

        public Filter getFilter() {
            return mFilter;
        }

        /**
         * Filter to search for occurences of the search-expression in any place of the
         * folder-name instead of doing jsut a prefix-search.
         *
         * @author Marcus@Wolschon.biz
         */
        public class FolderListFilter extends Filter {
            private CharSequence mSearchTerm;

            public CharSequence getSearchTerm() {
                return mSearchTerm;
            }

            /**
             * Do the actual search.
             * {@inheritDoc}
             *
             * @see #publishResults(CharSequence, FilterResults)
             */
            @Override
            protected FilterResults performFiltering(CharSequence searchTerm) {
                mSearchTerm = searchTerm;
                FilterResults results = new FilterResults();

                if ((searchTerm == null) || (searchTerm.length() == 0)) {
                    ArrayList<FolderInfoHolder> list = new ArrayList<FolderInfoHolder>(mFolders);
                    results.values = list;
                    results.count = list.size();
                } else {
                    final String searchTermString = searchTerm.toString().toLowerCase();
                    final String[] words = searchTermString.split(" ");
                    final int wordCount = words.length;

                    final ArrayList<FolderInfoHolder> newValues = new ArrayList<FolderInfoHolder>();

                    for (final FolderInfoHolder value : mFolders) {
                        if (value.displayName == null) {
                            continue;
                        }
                        final String valueText = value.displayName.toLowerCase();

                        for (int k = 0; k < wordCount; k++) {
                            if (valueText.contains(words[k])) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            /**
             * Publish the results to the user-interface.
             * {@inheritDoc}
             */
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mFilteredFolders = Collections.unmodifiableList((ArrayList<FolderInfoHolder>) results.values);
                // Send notification that the data set changed now
                notifyDataSetChanged();
            }
        }
    }

    static class FolderViewHolder {
        public TextView folderName;

        public TextView folderStatus;

        public TextView newMessageCount;
        public TextView flaggedMessageCount;

        public RelativeLayout activeIcons;
        public String rawFolderName;
        public View chip;
        public LinearLayout folderListItemLayout;
    }

    private class FolderClickListener implements OnClickListener {

        final BaseAccount account;
        final String folderName;
        final String displayName;
        final SearchModifier searchModifier;
        FolderClickListener(BaseAccount nAccount, String folderName, String displayName, SearchModifier nSearchModifier) {
            account = nAccount;
            this.folderName = folderName;
            searchModifier = nSearchModifier;
            this.displayName = displayName;
        }
        @Override
        public void onClick(View v) {
            String description = getString(R.string.search_title,
                                           getString(R.string.message_list_title, account.getDescription(), displayName),
                                           getString(searchModifier.resId));

            SearchSpecification searchSpec = new SearchSpecification() {
                @Override
                public String[] getAccountUuids() {
                    return new String[] { account.getUuid() };
                }

                @Override
                public Flag[] getForbiddenFlags() {
                    return searchModifier.forbiddenFlags;
                }

                @Override
                public String getQuery() {
                    return "";
                }

                @Override
                public Flag[] getRequiredFlags() {
                    return searchModifier.requiredFlags;
                }

                @Override
                public boolean isIntegrate() {
                    return false;
                }

                @Override
                public String[] getFolderNames() {
                    return new String[] { folderName };
                }

            };
            MessageList.actionHandle(FolderList.this, description, searchSpec);

        }

    }

    private static Flag[] UNREAD_FLAG_ARRAY = { Flag.SEEN };

    private void openUnreadSearch(Context context, final Account account) {
        String description = getString(R.string.search_title, mAccount.getDescription(), getString(R.string.unread_modifier));

        SearchSpecification searchSpec = new SearchSpecification() {
            //interface has no override            @Override
            public String[] getAccountUuids() {
                return new String[] { account.getUuid() };
            }

            //interface has no override            @Override
            public Flag[] getForbiddenFlags() {
                return UNREAD_FLAG_ARRAY;
            }

            //interface has no override            @Override
            public String getQuery() {
                return "";
            }

            @Override
            public Flag[] getRequiredFlags() {
                return null;
            }

            @Override
            public boolean isIntegrate() {
                return false;
            }

            @Override
            public String[] getFolderNames() {
                return null;
            }

        };
        MessageList.actionHandle(context, description, searchSpec);
    }

}
