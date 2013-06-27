package de.telekom.cldii.controller.mail.impl;

import java.util.List;

import android.app.Application;
import android.util.Log;

import com.fsck.k9.Account;
import com.fsck.k9.Preferences;
import com.fsck.k9.controller.MessagingController;
import com.fsck.k9.mail.Address;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.Message.RecipientType;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.internet.MimeMessage;
import com.fsck.k9.mail.internet.TextBody;

import de.telekom.cldii.config.IPreferenceManager;
import de.telekom.cldii.controller.mail.IMailController;
import de.telekom.cldii.data.mail.ICompactMail;
import de.telekom.cldii.data.mail.impl.CompactMailWrapper;
import de.telekom.cldii.data.mail.impl.MessageIdentifier;

/**
 * Implementation for {@link IMailController} delegating all calls to K9
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class MailController implements IMailController {

	private static final String TAG = "MailController";
	
	private Application app;
	
	public MailController(Application app) {
		this.app = app;
	}
	
	@Override
	public void checkMails() {
	    ((IPreferenceManager) app).getApplicationPreferences().setMailLastUpdateMillis();
		MessagingController.getInstance(app).checkMail(app, null, true, true, null);
	}
	
	@Override
	public void sendMail(String addressTo, List<String> addressesCC, List<String> addressesBCC, String subject, String text) {
		Account[] accounts = Preferences.getPreferences(app.getApplicationContext()).getAccounts();
		if(accounts.length > 0) {
			sendMail(addressTo, addressesCC, addressesBCC, subject, text, accounts[0]);
		}
	}
	
	@Override
	public void sendMail(String addressTo, List<String> addressesCC, List<String> addressesBCC, String subject, String text,
			String useAccountFromMailId) {
		Message message = MessageIdentifier.getMessage(useAccountFromMailId, app.getApplicationContext());
		if(message != null) {
			sendMail(addressTo, addressesCC, addressesBCC, subject, text, message.getFolder().getAccount());
		}
	}
	
	private void sendMail(String addressTo, List<String> addressesCC, List<String> addressesBCC, String subject, String text, Account outgoingAccount) {
		try {
			MimeMessage sendMessage = new MimeMessage();
			//set recipient
			sendMessage.setRecipient(RecipientType.TO, new Address(addressTo));
			
			//set cc
			if (addressesCC != null && addressesCC.size() > 0) {
				Address[] addressesCCArray = new Address[addressesCC.size()];
				for (int addressCCCount = 0; addressCCCount < addressesCC.size(); addressCCCount++) {
					addressesCCArray[addressCCCount] = new Address(addressesCC.get(addressCCCount));
				}
				sendMessage.setRecipients(RecipientType.CC, addressesCCArray);
			}
			
			//set bcc
			if (addressesBCC != null && addressesBCC.size() > 0) {
				Address[] addressesBCCArray = new Address[addressesBCC.size()];
				for (int addressBCCCount = 0; addressBCCCount < addressesBCC.size(); addressBCCCount++) {
					addressesBCCArray[addressBCCCount] = new Address(addressesBCC.get(addressBCCCount));
				}
				sendMessage.setRecipients(RecipientType.BCC, addressesBCCArray);
			}
			//set from
			sendMessage.setFrom(new Address(outgoingAccount.getEmail()));
			
			//set subject
			sendMessage.setSubject(subject);
			
			//set text
			sendMessage.setBody(new TextBody(text));
			
			//send message
			MessagingController.getInstance(app).sendMessage(outgoingAccount, sendMessage, null);
		} catch (MessagingException e) {
			Log.e(TAG, "Failed to send message", e);
		}
	}

	@Override
	public void deleteMail(ICompactMail mailToDelete) {
		if(!(mailToDelete instanceof CompactMailWrapper)) {
			Log.e(TAG, "K9 MailController only supported with K9 data model");
			return;
		}
		try {
			Message originalMessageToDelete = ((CompactMailWrapper)mailToDelete).getMessage();
			originalMessageToDelete.setFlag(Flag.DELETED, true);
			MessagingController.getInstance(app).deleteMessages(new Message[] {originalMessageToDelete}, null);
		} catch (MessagingException e) {
			Log.e(TAG, "Failed to delete message", e);
		}
	}
	
	@Override
	public void changeReadState(ICompactMail mail, boolean isRead) {
		if (!(mail instanceof CompactMailWrapper)) {
			Log.e(TAG, "K9 MailController only supported with K9 data model");
			return;
		}
		Message originalMail = ((CompactMailWrapper) mail).getMessage();
		MessagingController.getInstance(app).setFlag(new Message[] {originalMail}, Flag.SEEN, isRead);
	}
	
	@Override
	public void changeAnsweredState(ICompactMail mail, boolean isAnswered) {
		if (!(mail instanceof CompactMailWrapper)) {
			Log.e(TAG, "K9 MailController only supported with K9 data model");
			return;
		}
		Message originalMail = ((CompactMailWrapper) mail).getMessage();
		MessagingController.getInstance(app).setFlag(new Message[] {originalMail}, Flag.ANSWERED, isAnswered);
	}
}
