/**
 * Implementation of the MailDataProvider.
 */
package de.telekom.cldii.data.mail.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.fsck.k9.Account;
import com.fsck.k9.Preferences;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.store.LocalStore.LocalMessage;

import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.data.mail.ICompactMail;
import de.telekom.cldii.data.mail.IMailDataProvider;
import de.telekom.cldii.data.mail.impl.MailLoadingTask.MailLoadingTaskListener;

/**
 * Implementation of the MailDataProvider.
 * 
 * @author Anton Wolf, jambit GmbH
 * 
 */
public class MailDataProvider implements IMailDataProvider {

    private static final String TAG = "MailDataProvider";

    private Context context;

    @Override
    public void onCreate(Context context, IConfigurationManager configurationManager) {
        this.context = context;
    }

    @Override
    public void onLowMemoryWarning() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public List<ICompactMail> getIncommingMails() {
        List<ICompactMail> allMails = new ArrayList<ICompactMail>();
        Account[] accounts = Preferences.getPreferences(context).getAccounts();
        for (Account account : accounts) {
            String incommingFolderName = account.getInboxFolderName();
            if (incommingFolderName != null) {
                try {
                    Folder incommingFolder = account.getLocalStore().getFolder(incommingFolderName);
                    Message[] messages = incommingFolder.getMessages(null);
                    for (Message message : messages) {
                        if (!message.isSet(Flag.DELETED)) {
                            allMails.add(new CompactMailWrapper(message));
                        }
                    }
                } catch (MessagingException e) {
                    Log.e(TAG, "Faild to open incomming mails folder", e);
                }
            }
        }
        return allMails;
    }

    public List<ICompactMail> getIncommingMails(Comparator<ICompactMail> comparator) {
        List<ICompactMail> returnList = getIncommingMails();
        Collections.sort(returnList, comparator);
        return returnList;

    }

    @Override
    public int getUnreadMails() {
        int unreadCount = 0;
        List<ICompactMail> incommingMails = getIncommingMails(new ReceivedDateComparator());
        for (ICompactMail mail : incommingMails) {
            if (!mail.read()) {
                unreadCount++;
            }
        }
        return unreadCount;
    }

    @Override
    public ICompactMail getCompactMail(String mailId) {
        Message message = MessageIdentifier.getMessage(mailId, context);
        if (message != null) {
            return new CompactMailWrapper(message);
        }
        return null;
    }

    @Override
    public void getFullMail(String mailId, final IMailLoadingListener listener) {
        Message message = MessageIdentifier.getMessage(mailId, context);
        if (message == null) {
            listener.onMailLoaded(null);
            return;
        }
        MailLoadingTask mailLoadingTask = new MailLoadingTask(new MailLoadingTaskListener() {

            @Override
            public void onMailLoadingFinished(LocalMessage localMessage) {
                if (localMessage != null) {
                    listener.onMailLoaded(new FullMailWrapper(localMessage));
                } else {
                    listener.onMailLoaded(null);
                }
            }
        }, message);

        mailLoadingTask.execute();
    }

    @Override
    public String getNewMailIntentAction() {
        return "com.fsck.k9.NEW_MAILS_ARRIVED";
    }

    @Override
    public String getRemovedMailIntentAction() {
        return "com.fsck.k9.REMOTE_MAILS_DELETED";
    }

    @Override
    public List<String> getAccountNames() {
        Account[] accounts = Preferences.getPreferences(context).getAccounts();
        List<String> accountNames = new ArrayList<String>();
        for (Account account : accounts) {
            accountNames.add(account.getName());
        }
        return accountNames;
    }
}