package de.telekom.cldii.data.mail.impl;

import android.os.AsyncTask;
import android.util.Log;

import com.fsck.k9.Account;
import com.fsck.k9.mail.FetchProfile;
import com.fsck.k9.mail.Folder.OpenMode;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.store.LocalStore;
import com.fsck.k9.mail.store.LocalStore.LocalFolder;
import com.fsck.k9.mail.store.LocalStore.LocalMessage;

/**
 * AsyncTask to load content of a message.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class MailLoadingTask extends AsyncTask<Void, Void, LocalMessage>{
	
	private static final String TAG = "MessageLoadingTask";
	
	private MailLoadingTaskListener listener;
	private Message message;
	
	public MailLoadingTask(MailLoadingTaskListener listener, Message message) {
		this.listener = listener;
		this.message = message;
	}

	@Override
	protected LocalMessage doInBackground(Void... params) {
		try {
			Account account = message.getFolder().getAccount();
			LocalStore localStore = account.getLocalStore();
			LocalFolder localFolder = localStore.getFolder(message.getFolder().getName());
			localFolder.open(OpenMode.READ_WRITE);
			LocalMessage localMessage = (LocalMessage)localFolder.getMessage(message.getUid());
			
			FetchProfile fp = new FetchProfile();
			fp.add(FetchProfile.Item.ENVELOPE);
			fp.add(FetchProfile.Item.BODY);
			localFolder.fetch(new Message[] {localMessage}, fp, null);
			localFolder.close();
			return localMessage;
		} catch (MessagingException e) {
			Log.e(TAG, "Failed to load message", e);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(LocalMessage result) {
		super.onPostExecute(result);
		listener.onMailLoadingFinished(result);
	}

	/**
	 * Listener to be notified if message loading finished.
	 * 
	 * @author Anton Wolf, jambit GmbH
	 */
	public interface MailLoadingTaskListener {
		
		public void onMailLoadingFinished(LocalMessage message);
	}
}
